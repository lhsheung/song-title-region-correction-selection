package dev.amenhancer.plugin.regiontitle;

/** Pure-Java contract checks; no Android runtime required. */
public final class RegionContractTest {
    public static void main(String[] args) {
        check(RegionProfile.byId("hk") == RegionProfile.HONG_KONG, "Hong Kong decode");
        check(RegionProfile.byId(" HK ") == RegionProfile.HONG_KONG, "Normalized decode");
        check(RegionProfile.byId("missing") == RegionProfile.HONG_KONG, "Safe default");
        check(SelectionRemapper.map(1, RegionProfile.HONG_KONG) == 3, "CN base to HK");
        check(SelectionRemapper.map(6, RegionProfile.HONG_KONG) == 3, "JP base to HK");
        check(SelectionRemapper.map(1, RegionProfile.TAIWAN) == 4, "CN base to TW");
        check(SelectionRemapper.map(6, RegionProfile.MAINLAND_CHINA) == 1, "JP base to CN");
        check(SelectionRemapper.map(1, RegionProfile.JAPAN) == 6, "CN base to JP");
        check(SelectionRemapper.map(0, RegionProfile.HONG_KONG) == 0, "Original mode untouched");
        check(SelectionRemapper.map(3, RegionProfile.JAPAN) == 3, "Existing HK untouched");
        for (RegionProfile profile : RegionProfile.values()) {
            check(RegionProfile.byId(profile.id()) == profile, "Round trip " + profile.id());
            check(SelectionRemapper.map(6, RegionProfile.FOLLOW) == 6, "Follow is passthrough");
        }
        check(RegionProfile.HONG_KONG.storefront().equals("hk"), "HK storefront");
        check(RegionProfile.HONG_KONG.language().equals("zh-HK"), "HK language");
        check(RegionProfile.TAIWAN.storefront().equals("tw"), "TW storefront");
        check(RegionProfile.TAIWAN.language().equals("zh-TW"), "TW language");
        System.out.println("RegionContractTest: all checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
