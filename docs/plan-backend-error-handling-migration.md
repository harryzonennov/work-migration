# Backend Error Handling Migration Plan

**Date:** 2026-08-11
**Status:** Plan — awaiting approval

---

## 1. Problem

When the backend returns an error on document load (or list/save), the new UI **silently swallows it**:

- `useServiceEntityEditController` `.catch` only calls `console.error` — no user-facing message. The page mounts with `serviceUIModel === undefined` (blank form), giving no indication anything went wrong.
- `apiClient.unwrap` decides success on the **`result`** string field and reads error text from **`message`**. But the real backend envelope (per legacy contract) uses **`errorCode`** (200 = OK) with error text in **`errorMessage`**. So an error response like `{ errorCode: 500, errorMessage: "..." }` (no `content`) does **not** throw — `unwrap` returns `json.content ?? json`, i.e. the whole error object becomes the "record".

## 2. Legacy contract (authoritative)

From `ServiceHttpRequestHelper.js` / `Commons.js`:

| Aspect | Legacy rule |
|---|---|
| **Success field** | `errorCode` |
| **"OK" value** | `200` (`ERROR_CODE_OK`), or any `200–299` (`checkHTTPResponseCode`) |
| **Tie-breaker** | if code is bad but `content` **is present** → still treated as success (fall through to postHandle) |
| **Error trigger** | bad code **and** `!content` → route to `errorHandle(oData)` |
| **Error text** | `oData.errorMessage` (fallback: i18n `msgUnknowSystemFailure`) |
| **Error title** | `oData.errorTitle` (fallback: i18n `msgSystemFailure`) |
| **Content extraction** | `postSet(oData.content)` / `setModuleToUI(oData.content)` |

Legacy display: an inline **message bar** (red ERROR, `MSG_CATEGORY.ERROR`) prepended into `.main.message-container` — NOT a toast.

## 3. Existing new-UI infrastructure to reuse (no new components needed)

| Piece | Path | Role |
|---|---|---|
| `messageBarStore` | `controllers/messageBarStore.ts` | `pushMessageBarEntry`, `clearAllMessageBars`, `MSG_CATEGORY`, `DEFAULT_MESSAGE_CONTAINER` |
| `MessageBar` | `components/control/MessageBar.tsx` | Renders entries; already mounted by `AsyncEditorPage.tsx:101` |
| `handleErrorUIDefault` | `services/ServiceUtilityHelper.ts:1166` | Ported legacy error→message-bar path |

## 4. Design — a single content-extraction + error-decision helper

Introduce **one shared function** that encapsulates the legacy `checkResponseError` contract, so every load/save path uses the same decision. This is the faithful port of legacy `checkResponseError` + `content` extraction.

### 4.1 New helper — `extractContentOrThrow` (in `api/apiClient.ts`)

```ts
/** Backend error carrying the legacy envelope fields, thrown by extractContentOrThrow. */
export class BackendError extends Error {
	errorCode?: number;
	errorTitle?: string;
	raw: Record<string, unknown>;
	constructor(raw: Record<string, unknown>) {
		super((raw.errorMessage as string) || 'Unknown system failure');
		this.name = 'BackendError';
		this.errorCode = raw.errorCode as number | undefined;
		this.errorTitle = raw.errorTitle as string | undefined;
		this.raw = raw;
	}
}

/**
 * Legacy checkResponseError contract: success = errorCode in [200,299];
 * tie-breaker = presence of `content`. On failure (bad code AND no content)
 * throws BackendError carrying errorMessage/errorTitle.
 */
function extractContentOrThrow<T>(json: Record<string, unknown>): T {
	const code = Number(json.errorCode);
	const codeOk = !Number.isNaN(code) && code >= 200 && code <= 299;
	if (!codeOk && json.content == null) {
		throw new BackendError(json);
	}
	return (json.content ?? json) as T;
}
```

### 4.2 Reconcile `unwrap` — support BOTH envelopes

The current `unwrap` keys off `result`/`message`. Extend it to also honour the legacy `errorCode`/`errorMessage` contract, without breaking the login path (which genuinely uses `result`/`message`):

```ts
async function unwrap<T>(res: Response): Promise<T> {
	if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText} — ${res.url}`);
	const json = await res.json();
	// Login-style envelope: { result: 'success' | 'error', message }
	if (json.result && json.result !== 'success') {
		throw new BackendError({ errorMessage: json.message ?? json.result, ...json });
	}
	// Document-style envelope: { errorCode, errorMessage, content }
	return extractContentOrThrow<T>(json);
}
```

This keeps `apiGet`/`apiPost` behaviour for the happy path, but now correctly **throws `BackendError`** on the `errorCode`/`errorMessage` envelope too.

## 5. Wire the error into the UI — `useServiceEntityEditController` `.catch`

Replace the console-only `.catch` (lines 102–104) with a call that surfaces a message bar via the existing infrastructure:

```ts
.catch(err => {
	console.error('[useServiceEntityEditController] load failed:', err);
	pushMessageBarEntry({
		container: DEFAULT_MESSAGE_CONTAINER,
		context: 'load-error',
		msgCategory: MSG_CATEGORY.ERROR,
		message: err instanceof Error ? err.message : String(err),
	});
})
```

- On load error the page still mounts (loading clears in `.finally`), but now a red error bar appears in the editor's message container instead of a silent blank form.
- `context: 'load-error'` dedupes repeated failures.

`AsyncEditorPage` already calls `clearAllMessageBars()` on mount, so a stale error bar is cleared when a fresh load starts.

## 6. Optional — also surface save/action errors consistently

`ServiceEditController.handleFinish` already `message.error(...)`s on save failure (antd toast). For **visual consistency** with the legacy message-bar style, optionally route it through the same `pushMessageBarEntry` path. **Out of scope for this change** unless you want the toast→bar switch — flag only.

## 7. Files to change

| # | File | Change |
|---|---|---|
| 1 | `src/api/apiClient.ts` | Add `BackendError` class + `extractContentOrThrow`; update `unwrap` to honour `errorCode`/`errorMessage` envelope and throw `BackendError` |
| 2 | `src/composables/useServiceEntityEditController.ts` | Replace console-only `.catch` with `pushMessageBarEntry(... MSG_CATEGORY.ERROR ...)`; import from `messageBarStore` |

**2 files.** No new components — reuses `messageBarStore` + `MessageBar` (already mounted) + `MSG_CATEGORY`.

## 8. Why the `onLoaded` line (L149-152) is NOT where the fix goes

The user pointed at `useDocumentEditController.onLoaded` (L149-152). That callback runs **only on the success path** (inside `.then`, after `Promise.all` resolves). By the time `onLoaded` fires, the content has already been extracted successfully. The error must be caught **earlier** — at the `unwrap`/`extractContentOrThrow` boundary (so a bad response throws) and in the `.catch` of `useServiceEntityEditController` (so the throw surfaces as a message bar). Fixing `onLoaded` itself would be too late and would only cover the document editor, not list/item/save paths. Putting the decision in `apiClient` + the shared core `.catch` covers **every** load path (document, item, list-via-loadModuleEdit) uniformly.

## 9. Risks / notes

1. **Envelope ambiguity** — some endpoints may return `errorCode` as an HTTP-style 200 inside the body, others may omit it entirely (a bare `{ content }`). `extractContentOrThrow` treats "no `errorCode` field" as: `codeOk` is false, but if `content` is present it still returns `content` — matching legacy's content tie-breaker. Verify against a real error response during testing.
2. **`listDocuments`** uses the raw DataTables protocol (no envelope) — a backend error inside a 200 there still passes unflagged. Separate concern; not addressed here (legacy had the same gap for the table protocol).
3. **i18n fallback** — legacy falls back to a localized "unknown system failure". `BackendError` uses a plain English default; wire `i18n.t('commonElements:msgUnknowSystemFailure')` if a localized fallback is required.
