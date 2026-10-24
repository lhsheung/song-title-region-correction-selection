package dev.amenhancer.plugin.regiontitle;

import java.util.Locale;

/** Region profiles already understood by AM++'s catalog resolver. */
public enum RegionProfile {
    FOLLOW("follow", "跟随 AM++（不覆盖）", 0, "", ""),
    MAINLAND_CHINA("cn", "中国大陆", 1, "cn", "zh-CN"),
    US_SIMPLIFIED("us_zh", "美国（简体中文）", 2, "us", "zh-Hans"),
    HONG_KONG("hk", "中国香港", 3, "hk", "zh-HK"),
    TAIWAN("tw", "中国台湾", 4, "tw", "zh-TW"),
    KOREA("kr", "韩国", 5, "kr", "ko-KR"),
    JAPAN("jp", "日本", 6, "jp", "ja-JP");

    private final String id;
    private final String displayName;
    private final int selection;
    private final String storefront;
    private final String language;

    RegionProfile(String id, String displayName, int selection, String storefront, String language) {
        this.id = id;
        this.displayName = displayName;
        this.selection = selection;
        this.storefront = storefront;
        this.language = language;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public int selection() { return selection; }
    public String storefront() { return storefront; }
    public String language() { return language; }

    public String technicalSummary() {
        if (this == FOLLOW) return "插件不改写目录地区";
        return "storefront=" + storefront + "，language=" + language;
    }

    public static RegionProfile byId(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        for (RegionProfile profile : values()) {
            if (profile.id.equals(normalized)) return profile;
        }
        return HONG_KONG;
    }
}
