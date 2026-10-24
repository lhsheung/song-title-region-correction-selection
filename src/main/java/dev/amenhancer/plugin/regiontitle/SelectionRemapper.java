package dev.amenhancer.plugin.regiontitle;

/** Maps either built-in fixed-region profile to the profile selected in this plugin. */
final class SelectionRemapper {
    private SelectionRemapper() { }

    static int map(int selection, RegionProfile target) {
        if (target == RegionProfile.FOLLOW) return selection;
        // AM++ currently exposes MAINLAND_CHINA=1 and JAPAN=6 in its settings UI.
        if (selection == 1 || selection == 6) return target.selection();
        return selection;
    }
}
