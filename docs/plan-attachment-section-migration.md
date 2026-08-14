# Plan: Migrate Attachment Section (AttachmentCore + AttachmentUnion) to IntelligentUI

> **Status:** Planning document. No code changes yet.
> **Created:** 2026-07-04
> **Companion docs:** [`plan-bootstrap-integration.md`](./plan-bootstrap-integration.md), [`plan-right-bar-migration.md`](./plan-right-bar-migration.md), [`MINTON_ICON_REFERENCE.md`](./MINTON_ICON_REFERENCE.md), [`MINTON_CUSTOMIZATION_GUIDE.md`](./MINTON_CUSTOMIZATION_GUIDE.md)

---

## 1. What is the Attachment Section?

The attachment section on every document editor page in the legacy UI provides:

1. **Gallery of existing attachments** — thumbnails per file type (PDF/DOC/XLS/XML/image), each with title, description, and a Delete button
2. **Preview / download** — click a thumbnail to open the file (browser-native for images/PDFs, forced download for xls/xlsx/xml via Blob URL)
3. **Upload form** — title text input + description text input + a dropzone (Dropzone.js) supporting drag-drop of multiple files
4. **Two-step upload flow** — POST the text (title+description) first to get a UUID, then POST the file to that UUID
5. **Delete** — DELETE by attachment UUID
6. **Portlet chrome** — matches Minton's portlet visual (icon + title + collapse/close widgets)

Two legacy files implement it:
- **`AttachmentUnion.js`** (100 lines) — outer portlet wrapper (portlet-heading + collapse container)
- **`AttachmentCore.js`** (391 lines) — the actual gallery + upload form + Dropzone integration

## 2. Legacy behavior in detail

### 2.1 Props & config

`AttachmentUnion` receives:
- `configMeta` — object carrying **backend URLs** (`loadAttachmentURL`, `uploadAttachmentURL`, `uploadAttachmentTextURL`, `deleteAttachmentURL`), `acceptedFiles` (MIME allowlist), `skipDropzone`, `classAttachHeader` (icon class), etc.
- `baseUid` — the parent document's UUID (goes into the upload URL as `?uuid=<baseUid>`)
- `attachmentList` — the existing attachments from the parent record
- `labelObject` — pre-resolved i18n labels
- `refreshEditView`, `errorHandle` — callbacks

### 2.2 The 4 backend endpoints (already exist in IntelligentPlatform)

Verified in `PurchaseContractEditorController.java:305-343` (same pattern for all doc types):

| Endpoint | Method | Purpose |
|---|---|---|
| `GET  /<module>/loadAttachment?uuid=<attUuid>` | GET | Fetch attachment file bytes for display / download |
| `POST /<module>/uploadAttachmentText` | POST JSON `{baseUUID, attachmentTitle, attachmentDescription}` | Create the attachment metadata record, returns `{uuid}` |
| `POST /<module>/uploadAttachment?uuid=<attUuidFromText>` | POST multipart | Upload the file bytes to the attachment record created above |
| `POST /<module>/deleteAttachment` | POST JSON `{uuid}` | Delete the attachment (both metadata + file) |

The underlying implementation is `ServiceBasicUtilityController` (in `IntelligentPlatform`) — every doc type's editor controller delegates there.

### 2.3 attachmentModel shape (returned from backend + assembled locally)

`AttachmentCore.js:223-263` — for each entry in `attachmentList`, the frontend derives:

```ts
attachmentModel = {
  uuid: string,              // attachment UUID
  url: `${loadAttachmentURL}?uuid=${uuid}`,   // preview / download URL
  title: string,             // attachmentTitle
  description: string,       // attachmentDescription
  fileType: string,          // 'pdf' | 'xml' | 'jrxml' | 'doc' | 'docx' | 'xls' | 'xlsx' | 'jpg' | 'png' | …
  imgSrc: string,            // for non-image types: static asset (PDF_BIG, XML_BIG, DOC_BIG, XLS_BIG); for images: same as url
}
```

The type-to-imgSrc mapping is hard-coded in `AttachmentCore.js:238-260` and references `BootStrapImageAttach.IMAGE_PATH.*` (static PNG assets bundled with the legacy UI).

### 2.4 Upload flow (2-step)

`AttachmentCore.uploadAttachment()`:

```
1. User fills in title + description → clicks upload
2. POST /uploadAttachmentText  { baseUUID, attachmentTitle, attachmentDescription }
   → server creates a record, returns { uuid: <newAttachmentUuid> }
3. Set `attachmentDropzone.options.url = /uploadAttachment?baseUUID=<baseUid>&uuid=<newAttachmentUuid>`
4. Call `attachmentDropzone.processQueue()` → Dropzone.js POSTs the file(s) to that URL
5. On success → parent's `refreshEditView()` re-loads the whole record (including the new attachment)
```

**Why two-step?** Legacy design — text metadata + file blob are separate DB records / storage paths on the backend.

### 2.5 Delete flow

`AttachmentCore.deleteAttachmentUnion(uuid)`:

```
1. User clicks Delete on a thumbnail
2. POST /deleteAttachment { uuid }
3. On success → parent's `refreshEditView()` re-loads (attachment gone from list)
```

No confirmation dialog in legacy — just fires the request.

### 2.6 Preview flow

`AttachmentCore.openAttachmentUnion(model, event)`:

- For **xml / jrxml / xls / xlsx**: preventDefault the anchor click, GET the URL, wrap the response body in a `new Blob([...], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})`, create `URL.createObjectURL(blob)`, and trigger a `<a download>` click to force a browser download.
- For **pdf / images / doc**: no preventDefault — the `<a href={url} target="_blank">` opens natively in a new tab (browser handles PDF preview, image display).

Downloading xls/xlsx via Blob is a legacy workaround for browsers that don't natively "download" Excel from a URL — most modern browsers handle this via server `Content-Disposition: attachment` header instead, so this may not be necessary in the migrated version.

## 3. What's in IntelligentUI today

Investigation confirmed:

| Location | State |
|---|---|
| `src/components/page/AsyncAttachmentSection.tsx` | ✅ Stub exists (~65 lines). Renders a `<div data-component="attachment-union">` placeholder with `TODO: attachment-union — not migrated` comment. |
| `src/components/EditPageShell.tsx:121-130` | ✅ `case 'upload'` renders a minimal antd `<Upload>` inside `<ProForm.Item>` — no upload wiring, no gallery, no delete. |
| `src/types/logistics/PurchaseContractContent.ts:130` | ✅ Payload carries `purchaseContractAttachmentUIModelList: unknown[]` end-to-end. Shape is `unknown` — needs typing. |
| `src/pages/logistics/purchaseContract/PurchaseContractEditController.tsx:141-149` | ✅ Comment reads `attachments are not yet implemented`, sends empty array in `buildPayload()`. |
| `src/services/logistics/PurchaseContractManager.ts` | ❌ No attachment URLs defined. |
| Backend | ✅ 4 endpoints exist per doc type (verified in `PurchaseContractEditorController.java:305-343`). |
| Minton `.portlet` styles | ✅ Available — used by `AsyncEditSection.tsx`'s Bootstrap portlet render branch. |
| antd `<Upload.Dragger>` component | ✅ Available in antd 5. Native drag-drop equivalent to Dropzone.js. |

## 4. Migration strategy

### 4.1 Overall direction

**Don't port `AttachmentCore.js` verbatim.** The legacy is jQuery + Vue 2 + Dropzone.js + hand-rolled two-step form. The new UI already has:

- **antd `<Upload.Dragger>`** — native drag-drop, replaces Dropzone.js
- **antd `<Card.Meta>`** / Minton `.gal-detail.thumb` — either can render the thumbnail gallery
- **`ProForm.Item`** — already handles form-based inputs
- **`fetchClient` / `apiGet` / `apiPost`** — already handle HTTP with envelope unwrapping
- **`useMintonTheme` + Minton portlet CSS** — already provides the portlet chrome

The React implementation is ~150-200 lines of TSX using these building blocks. The 391-line legacy is 60% Dropzone glue and DOM manipulation that antd's Upload replaces.

### 4.2 Component structure

Propose a single new file `src/components/doc/AttachmentSection.tsx` (no need to split Union/Core — that split was a Vue convention). Structure:

```
<AttachmentSection
  moduleUrl="purchaseContract"    // used to build endpoint URLs
  baseUid={contractUuid}
  attachmentList={record.purchaseContractAttachmentUIModelList}
  onRefresh={() => reloadContract()}
  disabled={isReadonly}
/>
```

Internal state:
- `uploading: boolean`
- `pendingTitle: string`
- `pendingDescription: string`

Renders a Minton portlet card containing:
- **Header** — `.portlet-heading` with `<i class="fa fa-paperclip content-portlet-title"/>` + section title
- **Gallery grid** — Bootstrap `.row` with `.col-md-4 col-lg-3` cards for each attachment
- **Upload dropzone card** — antd `<Upload.Dragger>` with title + description text inputs above it

### 4.3 File-type icons — use Minton MDI (not legacy PNG assets)

Legacy shipped 4 PNG icons (`PDF_BIG`, `XML_BIG`, `DOC_BIG`, `XLS_BIG`). Minton has MDI equivalents:

| File type | Legacy asset | MDI equivalent |
|---|---|---|
| pdf | PDF_BIG.png | `mdi mdi-file-pdf-box` |
| doc, docx | DOC_BIG.png | `mdi mdi-file-word-box` |
| xls, xlsx | XLS_BIG.png | `mdi mdi-file-excel-box` |
| xml, jrxml | XML_BIG.png | `mdi mdi-file-code` or `mdi mdi-xml` |
| jpg, png, gif, jpeg | (used image directly) | Show the actual image (loaded from `/loadAttachment?uuid=…`) |
| other | (fell through to image URL) | `mdi mdi-file-outline` fallback |

Advantage of MDI: no PNG asset bundling, colors follow the theme, matches the icon migration we already completed.

### 4.4 Backend URL wiring

Each doc-type manager needs to expose attachment URLs. Match the pattern already established with `getStatusURL`:

```ts
// PurchaseContractManager.ts (new methods)
static readonly loadAttachmentURL         = 'purchaseContract/loadAttachment';
static readonly uploadAttachmentURL       = 'purchaseContract/uploadAttachment';
static readonly uploadAttachmentTextURL   = 'purchaseContract/uploadAttachmentText';
static readonly deleteAttachmentURL       = 'purchaseContract/deleteAttachment';
```

Or bundle them into a single config object:

```ts
static getAttachmentConfig() {
  return {
    module: 'purchaseContract',
    loadURL: 'purchaseContract/loadAttachment',
    uploadURL: 'purchaseContract/uploadAttachment',
    uploadTextURL: 'purchaseContract/uploadAttachmentText',
    deleteURL: 'purchaseContract/deleteAttachment',
  };
}
```

The **config-object** approach is cleaner — the `<AttachmentSection>` component takes one prop instead of four.

### 4.5 Two-step upload with antd Upload.Dragger

antd's `<Upload.Dragger>` fires `beforeUpload(file)` per file. We can:

1. Intercept `beforeUpload` (return `false` to prevent the default antd upload).
2. Show the title/description inputs above the dropzone.
3. On submit: POST `/uploadAttachmentText` → get `uuid`. Then POST `/uploadAttachment?uuid=<uuid>` with the file via `FormData`.

Alternative: use `customRequest` prop on Upload.Dragger which lets us fully control the upload flow (matches legacy behavior 1:1).

I recommend **`customRequest`** approach — cleaner, keeps the two-step atomic.

### 4.6 Preview / download

Since IntelligentPlatform serves attachments with proper `Content-Disposition` (verified — the endpoint returns `ResponseEntity<byte[]>`), the browser will handle downloads natively for xls/pdf/etc. No need for the legacy Blob URL trick.

For **image preview**: use antd `<Image preview>` — supports zoom, gallery navigation, one-line implementation.
For **PDF preview**: open `<a href={url} target="_blank">` — browser's native PDF viewer handles it.

## 5. Phased plan

### Phase A — Chrome + read-only gallery ✅ DONE (2026-07-04)

**Goal:** replace the placeholder stub with a working component that displays existing attachments.

**Deliverables:**

- `src/components/doc/AttachmentSection.tsx` — new file, ~120 lines
- `src/types/logistics/AttachmentUIModel.ts` — new file, types
- `src/services/api/attachmentApi.ts` — new file, 4 endpoint wrappers
- `src/services/logistics/PurchaseContractManager.ts` — add `getAttachmentConfig()` method
- `src/components/page/AsyncAttachmentSection.tsx` — replace the placeholder with `<AttachmentSection>` mounting
- `src/pages/logistics/purchaseContract/PurchaseContractEditController.tsx` — remove the "attachments not yet implemented" comment; wire the attachment list through

**What renders:**
- Minton portlet card with paperclip icon in header
- Grid of existing attachments (MDI file-type icons or actual image thumbnails)
- Each card shows title + description
- **NO upload UI, NO delete button** in Phase A — just display

**Verification:** open a PurchaseContract that has attachments in the DB → see them rendered.

### Phase B — Delete [~30 min]

**Goal:** add a Delete button per attachment with confirmation.

**Deliverables:**
- `AttachmentSection.tsx` — add antd `<Popconfirm>` + `<Button danger>` per card
- Wire to `deleteAttachment()` from the service
- On success → call `onRefresh` (parent reloads)

**Verification:** click Delete → confirm → attachment removed after refresh.

### Phase C — Upload [~2 hours]

**Goal:** two-step upload with title + description.

**Deliverables:**
- `AttachmentSection.tsx` — add title/description inputs + `<Upload.Dragger customRequest>`
- Handle the 2-step flow: uploadText → upload
- Handle multi-file (each file gets the same title/description or a per-file variant — user decision)
- Progress indication (antd Upload provides this)
- On success → `onRefresh`

**Verification:** drag a PDF onto the dropzone → title/description filled → submit → new attachment appears.

### Phase D — Preview / download [~30 min]

**Goal:** click a thumbnail to preview or download.

**Deliverables:**
- Image types: wrap in antd `<Image preview>`
- PDF / all other types: `<a href={url} target="_blank">` — browser handles it

**Verification:** click on a JPG → antd image preview overlay opens with zoom/rotate. Click on a PDF → new tab with browser's PDF viewer. Click on an XLS → downloads.

### Phase E — Polish (optional)

- File-type filter chip (show only PDFs, etc.)
- Sort by upload date / title
- Bulk delete via checkboxes
- Copy attachment URL to clipboard
- Drag-to-reorder (if the backend supports ordering)

All deferrable — not needed for Phase A-D to be useful.

## 6. Risk assessment

| Risk | Likelihood | Mitigation |
|---|---|---|
| Backend endpoint returns different envelope than expected | Low | Test against real backend early. Adjust `attachmentApi.ts` per-response. |
| `attachmentList` items have different shape than we assume | Medium | Add TS type from actual server response. Log the first response to console during Phase A to verify. |
| antd Upload.Dragger's `customRequest` semantics differ from Dropzone.js | Low | Well-documented in antd. Two-step flow is a standard use case. |
| Large files hitting size limits | Medium | Backend has upload limits. Show error via antd `message.error()` on 413. |
| CORS on `loadAttachment` URLs when fetched from `<img src>` | Low | Same origin as the app (via Vite proxy). No cross-origin issue. |
| PurchaseContract's `PurchaseContractAttachmentUIModel` shape differs from other doc types | Medium | Type each doc's attachment separately, share the common fields via a base interface. |

## 7. Open questions

1. **Attachment upload metadata semantics** — legacy allows one title+description per **upload session** which then applies to every file dragged. Is that OK? Or should each file get its own title/description? (Legacy behavior seems suboptimal — you can't upload 3 unrelated files at once. Recommend per-file title/description in the new UI.)
2. **Where does the section render?** — inside the edit form (as a tab or bottom section) or as a floating section? Legacy renders it as a section (visible on every editor page). Confirmed from legacy `getDefaultPageMeta()` — section type `ATTACHMENT`. Same for the new UI: keep it as a section in the tab layout.
3. **Read-only mode** — should the section be visible but disabled when the doc is read-only (e.g. APPROVED status)? Legacy just hides the upload button in that case, keeps delete visible for admins. **Recommend**: hide upload but keep delete when `readonly`; hide both when explicitly disabled.
4. **First doc type for Phase A worked example** — PurchaseContract (matches everything else we've done). Roll out to other doc types via a simple pattern: each doc's manager exposes `getAttachmentConfig()`, and the page mounts `<AttachmentSection config={manager.getAttachmentConfig()} baseUid={docUuid} attachmentList={record[...]}>` with the appropriate list path.

## 8. Estimated total effort

| Phase | Effort | Priority |
|---|---|---|
| A — Read-only gallery | ~2 hours | ⭐ High (unlocks visibility of existing attachments) |
| B — Delete | ~30 min | ⭐ High (small increment on A) |
| C — Upload | ~2 hours | ⭐ High (the real feature) |
| D — Preview/download | ~30 min | 🟡 Medium (native behavior works for most types even without this) |
| E — Polish | Variable | 🟢 Low (deferrable) |

**Total for full feature (Phase A-D): ~5 hours.**

**Recommended slice:** Phase A + B + C in one round (~4.5 hours), then verify visually, then D.

## 9. What Phase A won't include

- **Any change to backend** — all 4 endpoints exist already
- **New Minton SCSS** — reuse existing `.portlet`, `.gal-detail`, `.thumb`, `.text-muted`
- **New icon fonts** — reuse MDI (already loaded)
- **Changes to any file field of AsyncField / SelectField / EditPageShell** — attachment section is a separate section type, its own component
- **DocumentItemMultiSelect changes** — completely orthogonal
- **Changes to `useMintonTheme` or MainLayout** — attachment doesn't interact with the layout chrome

## 10. Reference: legacy → new mapping

| Legacy | New UI equivalent |
|---|---|
| `Vue.extend({ name: "attachment-union" })` — outer portlet | `<AttachmentSection>` — outer function component |
| `Vue.extend({ name: "attachment-core" })` — inner gallery + form | Merged into `<AttachmentSection>` |
| `BootStrapAttachProxy` — Dropzone.js glue | antd `<Upload.Dragger customRequest>` |
| `Vue $set`, `Vue watch` | React `useState`, `useEffect` |
| `jQuery.i18n.prop()` | react-i18next `t()` |
| `configMeta.loadAttachmentURL` (per-doc URL) | `attachmentConfig` prop (per-doc config object) |
| `BootStrapImageAttach.IMAGE_PATH.PDF_BIG` (PNG asset) | `mdi mdi-file-pdf-box` (MDI font glyph) |
| `refreshEditView()` prop | `onRefresh` callback prop |
| `errorHandle` prop | antd `message.error()` |
| Bootstrap `<div class="dropzone">` | antd `<Upload.Dragger>` |
| Manual thumbnail grid via `col-md-4` | Same — reuse Bootstrap grid classes |
| `.portlet-heading` + `.portlet-widgets` | Same — Minton SCSS still ships these |

## 11. Recommendation

**Proceed with Phase A first, then B, then C.** Each is small enough (~30 min to 2 hours) to be independently reviewable. Verifying Phase A against real attachments in the DB establishes the shape assumptions, then B/C build on it.

The two most important decisions before starting:
1. Confirm **per-file title/description** (recommended) vs. one-shared metadata for a batch upload (legacy).
2. Confirm the **first doc type** — PurchaseContract if you want to match the rest of the migration pattern.

Once those are answered, I'll execute Phase A end-to-end.
