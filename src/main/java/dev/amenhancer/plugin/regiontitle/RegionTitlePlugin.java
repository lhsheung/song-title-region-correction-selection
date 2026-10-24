package dev.amenhancer.plugin.regiontitle;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import dev.amenhancer.plugin.api.AmppPlugin;
import dev.amenhancer.plugin.api.PluginCall;
import dev.amenhancer.plugin.api.PluginContext;
import dev.amenhancer.plugin.api.PluginHook;
import dev.amenhancer.plugin.api.PluginSettingsSession;
import dev.amenhancer.plugin.api.PluginUnsupportedException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Opens AM++'s existing, but not UI-exposed, catalog region profiles.
 *
 * This plugin intentionally delegates catalog identity matching, ISRC fallback,
 * request rewriting and surface updates to AM++. It only remaps the two built-in
 * fixed selections (1 and 6) at the resolver's central locale/cache-key seams.
 */
public final class RegionTitlePlugin extends AmppPlugin {
    private static final String RESOLVER =
        "io.github.proify.lyricon.amprovider.xposed.AppleInternalCatalogResolver$Companion";
    private static final String PERSISTENT_ACCESS =
        "io.github.proify.lyricon.amprovider.xposed.CatalogPersistentAccessKt";
    private static final String METADATA_STORE =
        "io.github.proify.lyricon.amprovider.xposed.AppleMetadataOverrideStore";
    private static final String TESTED_AMPP_COMMIT =
        "544bae60357bddf9a412871b900dda5938aa1ac6";

    private PluginContext runtime;
    private RegionSettings settings;

    @Override
    public void onLoad(PluginContext context) throws Exception {
        runtime = context;
        settings = new RegionSettings(context.getDataDirectory());
        runtime.claimResource("dev.amenhancer.title-correction.fixed-region", true);
        installRequiredResolverHooks();
        installOptionalCacheHooks();
        runtime.log("已准备地区覆盖：" + settings.active().displayName()
            + "；适配参考 AM++ " + TESTED_AMPP_COMMIT, null);
    }

    @Override
    public void onStart() {
        runtime.log("自选地区歌曲名已启动：" + settings.active().displayName()
            + "（" + settings.active().technicalSummary() + "）", null);
    }

    private void installRequiredResolverHooks() throws Exception {
        ClassLoader moduleLoader = AmppPlugin.class.getClassLoader();
        final Class<?> companion;
        try {
            companion = Class.forName(RESOLVER, false, moduleLoader);
        } catch (ClassNotFoundException error) {
            throw unsupported();
        }
        String[] methods = {
            "storefrontForContentUiLanguage",
            "languageTagsForContentUiLanguage",
            "languageTagForContentUiLanguage",
            "localizedMetadataCacheKey",
            "artistCacheKey"
        };
        for (String name : methods) {
            Method target = findIntMethod(companion, name, 0);
            if (target == null) throw unsupported();
            hookSelection(target, 0);
        }
    }

    private void installOptionalCacheHooks() {
        // These hooks keep cache warm-up and shared artist aliases in the same
        // namespace. They are optional because direct cache-key hooks above are
        // sufficient for correctness, and older builds may not have these seams.
        try {
            Class<?> access = Class.forName(
                PERSISTENT_ACCESS, false, AmppPlugin.class.getClassLoader());
            hookOptionalIntMethod(access, "warmPersistentLocalizedCache", 1);
            hookOptionalIntMethod(access, "finishPersistentCacheWarm", 1);
        } catch (ReflectiveOperationException error) {
            runtime.log("可选持久缓存适配未安装", error);
        }
        try {
            Class<?> store = Class.forName(
                METADATA_STORE, false, AmppPlugin.class.getClassLoader());
            hookOptionalIntMethod(store, "sharedConfiguredArtist", 0);
            hookOptionalIntMethod(store, "rememberSharedConfiguredArtist", 0);
        } catch (ReflectiveOperationException error) {
            runtime.log("可选共享艺人缓存适配未安装", error);
        }
    }

    private void hookOptionalIntMethod(Class<?> owner, String name, int argumentIndex) {
        Method method = findIntMethod(owner, name, argumentIndex);
        if (method != null) hookSelection(method, argumentIndex);
    }

    private void hookSelection(Method target, int argumentIndex) {
        runtime.getHooks().hook(target, false, new PluginHook() {
            @Override
            public void before(PluginCall call) {
                Object[] arguments = call.getArguments();
                int original = (Integer) arguments[argumentIndex];
                arguments[argumentIndex] = SelectionRemapper.map(original, settings.active());
            }
        });
    }

    /** Matches Kotlin internal methods both before and after module-name mangling. */
    private static Method findIntMethod(Class<?> owner, String sourceName, int argumentIndex) {
        List<Method> matches = new ArrayList<>();
        for (Method method : owner.getDeclaredMethods()) {
            String actualName = method.getName();
            boolean nameMatches = (actualName.equals(sourceName)
                || actualName.startsWith(sourceName + "$"))
                && !actualName.endsWith("$default");
            Class<?>[] parameters = method.getParameterTypes();
            if (nameMatches && parameters.length > argumentIndex
                && parameters[argumentIndex] == Integer.TYPE) {
                matches.add(method);
            }
        }
        return matches.size() == 1 ? matches.get(0) : null;
    }

    private static PluginUnsupportedException unsupported() {
        return new PluginUnsupportedException(
            "当前 AM++ 缺少所需目录地区接口。请使用与插件适配源码相近的 AM++ 版本；参考 commit："
                + TESTED_AMPP_COMMIT);
    }

    @Override
    public PluginSettingsSession createSettings(Context context) {
        int padding = dp(context, 20);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(context);
        title.setText("Song Title Region Correction Selection");
        title.setTextSize(22);
        content.addView(title);

        TextView help = new TextView(context);
        help.setText("请先在 AM++ 中开启“歌曲名显示修正”，并选择“固定中国大陆”或“固定日本”（不要选“按歌曲原地区修正”）。\n\n"
            + "这里只改变 AM++ 查询歌曲名、艺人和专辑信息时使用的目录地区，不更改 Apple Music 账号地区。\n\n"
            + "修改选择后，请完全停止并重开 Apple Music。");
        help.setTextSize(14);
        help.setPadding(0, dp(context, 12), 0, dp(context, 12));
        content.addView(help);

        RadioGroup group = new RadioGroup(context);
        group.setOrientation(RadioGroup.VERTICAL);
        for (RegionProfile profile : RegionProfile.values()) {
            RadioButton button = new RadioButton(context);
            button.setId(profile.ordinal());
            button.setText(profile.displayName());
            button.setTextSize(16);
            button.setPadding(0, dp(context, 8), 0, dp(context, 8));
            group.addView(button);
        }
        group.check(settings.selected().ordinal());
        content.addView(group);

        TextView status = new TextView(context);
        status.setTextSize(13);
        status.setPadding(0, dp(context, 16), 0, 0);
        updateStatus(status);
        content.addView(status);

        group.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            RegionProfile profile = RegionProfile.values()[checkedId];
            try {
                settings.select(profile);
                updateStatus(status);
            } catch (IOException error) {
                runtime.log("地区设置保存失败", error);
                status.setText("保存失败：" + error.getMessage());
            }
        });

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.addView(content, new ScrollView.LayoutParams(
            ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        return new PluginSettingsSession() {
            @Override public View getView() { return scroll; }
            @Override public void close() { group.setOnCheckedChangeListener(null); }
        };
    }

    private void updateStatus(TextView status) {
        status.setText("本次运行：" + settings.active().displayName()
            + "\n已保存：" + settings.selected().displayName()
            + "\n" + settings.selected().technicalSummary()
            + (settings.active() == settings.selected() ? "" : "\n重启后应用已保存的选择。"));
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
