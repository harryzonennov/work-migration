import java.util.*;

/**
 * Central registry of auto-import rules for generated TypeScript controller files.
 *
 * Two rule types:
 *
 *   AppendRule   — when `marker` appears in generated code and `sentinel` is absent,
 *                  append `line` after the last existing import statement.
 *
 *   ReplaceRule  — when `marker` appears in generated code and `sentinel` is absent,
 *                  replace `oldFragment` inside an existing import line with `newFragment`.
 *                  Use this when a symbol must be added to an already-present import,
 *                  e.g. adding ProcessButtonConstants to the DocumentConstants import.
 *
 * Usage:
 *   // Static rules only (universal — fire whenever the marker appears in content):
 *   String patched = ImportRegistry.injectAll(content);
 *
 *   // Static rules + caller-supplied conditional rules accumulated during conversion:
 *   String patched = ImportRegistry.injectAll(content, conversionResult.imports());
 */
public class ImportRegistry {

    // ── conditional import rules (not universal — only added when a conversion fires) ──

    /** Import for USE_CASE alias — added when DocumentItemMultiSelectFactory.USE_CASE is replaced. */
    public static final AppendRule RULE_USE_CASE = appendRule(
        "DocumentItemMultiSelectFactory_USE_CASE",
        "DocItemMultiSelectConstants",
        "import { USE_CASE as DocumentItemMultiSelectFactory_USE_CASE } from '@/components/doc/DocItemMultiSelectConstants';"
    );

    /** Import for RefControlIds — added when RefControl.Ids.xxx is replaced with RefControlIds.xxx. */
    public static final AppendRule RULE_REF_CONTROL_IDS = appendRule(
        "RefControlIds.",
        "from '@/components/control/RefControl'",
        "import { RefControlIds } from '@/components/control/RefControl';"
    );

    /** Import for i18n — added when vm.label.xxx is replaced with i18n.t('entityKey:xxx'). */
    public static final AppendRule RULE_I18N = appendRule(
        "i18n.t(",
        "from '@/i18n'",
        "import i18n from '@/i18n';"
    );

    /**
     * Import for WarehouseStoreManager — added when genInitSearchModel() is generated
     * (only in controllers that reference warehouse store item searches, e.g. newModuleFromStore).
     */
    public static final AppendRule RULE_WAREHOUSE_STORE_MANAGER = appendRule(
        "WarehouseStoreManager.genInitSearchModel",
        "WarehouseStoreManager",
        "import { WarehouseStoreManager } from '@/services/logistics/WarehouseStoreManager';"
    );

    // ── static rules (universal — always checked against every generated file) ─────────

    static final Rule[] RULES = {

        // ProcessButtonConstants is co-imported with DocumentConstants on the same line.
        // Templates ship with only DocumentConstants; edit controllers that reference
        // ProcessButtonConstants need the line expanded in-place.
        replaceRule(
            "ProcessButtonConstants",
            "ProcessButtonConstants",
            "{ DocumentConstants }",
            "{ DocumentConstants, ProcessButtonConstants }"
        ),

        // ── page / section infrastructure ───────────────────────────────────────

        appendRule(
            "RefControlIds.",
            "from '@/components/control/RefControl'",
            "import { RefControlIds } from '@/components/control/RefControl';"
        ),

        appendRule(
            "SectionCategory.",
            "from '@/components/page/AsyncSection'",
            "import { SectionCategory } from '@/components/page/AsyncSection';"
        ),

        // ── shared service helpers ───────────────────────────────────────────────

        appendRule(
            "getDefaultPrirityCodeIconArray",
            "SystemStandrdMetadataProxy",
            "import { getDefaultPrirityCodeIconArray } from '@/services/SystemStandrdMetadataProxy';"
        ),

        appendRule(
            "hiddenItemColumnSerialIdWrapper",
            "ServiceUtilityHelper",
            "import { hiddenItemColumnSerialIdWrapper } from '@/services/ServiceUtilityHelper';"
        ),
    };

    // ── public API ─────────────────────────────────────────────────────────────

    /**
     * Apply the static RULES against content.
     * Use when no conversion-stage rules have been accumulated.
     */
    public static String injectAll(String content) {
        return injectAll(content, Collections.emptyList());
    }

    /**
     * Apply the static RULES plus any caller-supplied `extraRules` against content.
     * `extraRules` are rules returned by converters that only apply when they actually
     * fired a substitution (e.g. RULE_I18N, RULE_USE_CASE).
     * Duplicates (same sentinel already present) are silently skipped.
     */
    public static String injectAll(String content, List<Rule> extraRules) {
        // Merge static + extra into one working list (extra may overlap static — dedup by sentinel)
        List<Rule> allRules = new ArrayList<>(Arrays.asList(RULES));
        for (Rule r : extraRules) {
            if (!alreadyPresent(allRules, r)) allRules.add(r);
        }

        // 1. replace-in-place rules first
        for (Rule rule : allRules) {
            if (rule instanceof ReplaceRule) {
                ReplaceRule rr = (ReplaceRule) rule;
                if (content.contains(rr.marker) && !content.contains(rr.sentinel)) {
                    content = content.replace(rr.oldFragment, rr.newFragment);
                }
            }
        }

        // 2. append rules — collect, then insert after last import in one operation
        StringBuilder toAppend = new StringBuilder();
        for (Rule rule : allRules) {
            if (rule instanceof AppendRule) {
                AppendRule ar = (AppendRule) rule;
                if (content.contains(ar.marker) && !content.contains(ar.sentinel)) {
                    toAppend.append(ar.line).append("\n");
                }
            }
        }
        if (toAppend.length() == 0) return content;

        int lastImportEnd = findLastImportEnd(content);
        if (lastImportEnd < 0) return content;

        return content.substring(0, lastImportEnd + 1)
                + toAppend
                + content.substring(lastImportEnd + 1);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private static boolean alreadyPresent(List<Rule> rules, Rule candidate) {
        String candidateSentinel = sentinelOf(candidate);
        if (candidateSentinel == null) return false;
        for (Rule r : rules) {
            if (candidateSentinel.equals(sentinelOf(r))) return true;
        }
        return false;
    }

    private static String sentinelOf(Rule r) {
        if (r instanceof AppendRule) return ((AppendRule) r).sentinel;
        if (r instanceof ReplaceRule) return ((ReplaceRule) r).sentinel;
        return null;
    }

    static int findLastImportEnd(String content) {
        int lastImportEnd = -1;
        int searchFrom = 0;
        while (true) {
            int idx = content.indexOf("import ", searchFrom);
            if (idx < 0) break;
            int lineEnd = content.indexOf('\n', idx);
            if (lineEnd < 0) lineEnd = content.length();
            lastImportEnd = lineEnd;
            searchFrom = lineEnd + 1;
        }
        return lastImportEnd;
    }

    static AppendRule appendRule(String marker, String sentinel, String line) {
        return new AppendRule(marker, sentinel, line);
    }

    static ReplaceRule replaceRule(String marker, String sentinel,
                                   String oldFragment, String newFragment) {
        return new ReplaceRule(marker, sentinel, oldFragment, newFragment);
    }

    // ── rule types ─────────────────────────────────────────────────────────────

    interface Rule {}

    static class AppendRule implements Rule {
        final String marker;
        final String sentinel;
        final String line;
        AppendRule(String marker, String sentinel, String line) {
            this.marker = marker;
            this.sentinel = sentinel;
            this.line = line;
        }
    }

    static class ReplaceRule implements Rule {
        final String marker;
        final String sentinel;
        final String oldFragment;
        final String newFragment;
        ReplaceRule(String marker, String sentinel, String oldFragment, String newFragment) {
            this.marker = marker;
            this.sentinel = sentinel;
            this.oldFragment = oldFragment;
            this.newFragment = newFragment;
        }
    }
}
