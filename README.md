# Song Title Region Correction Selection

一个用于 **AM++（AM-plus-plus）** 的插件，用来自选“歌曲名显示修正”所使用的 Apple Music 目录地区。

AM++ 内置功能只提供“中国大陆”和“日本”两个固定地区。本插件复用 AM++ 已有的目录元数据链路，在不修改 Apple Music 账号地区的前提下，增加中国香港、中国台湾、韩国、美国等地区选项。

作者：**Lau Hing Sheung**

## 基本功能

- 自选歌曲名、艺人名和专辑名所使用的目录地区
- 复用 AM++ 的歌曲匹配、ISRC 回退、元数据缓存与界面刷新逻辑
- 地区缓存相互隔离，切换地区后不会直接读取旧地区缓存
- 不更改 Apple Music 账号地区
- 不翻译歌词，也不修改歌词内容

可选地区：

| 显示名称 | storefront | language |
| --- | --- | --- |
| 跟随 AM++（不覆盖） | 使用 AM++ 当前配置 | 使用 AM++ 当前配置 |
| 中国大陆 | `cn` | `zh-CN` |
| 中国香港 | `hk` | `zh-HK` |
| 中国台湾 | `tw` | `zh-TW` |
| 日本 | `jp` | `ja-JP` |
| 韩国 | `kr` | `ko-KR` |
| 美国（简体中文） | `us` | `zh-Hans` |

## 使用要求

- Android 8.0 及以上
- 支持插件系统的 AM++，插件 SDK v1
- 先在 AM++ 中开启“歌曲名显示修正”
- 内置地区选择必须设为“固定中国大陆”或“固定日本”
- 不要在内置设置中选择“按歌曲原地区修正”，否则插件没有可重映射的固定地区

## 安装

1. 从 [Releases](https://github.com/lhsheung/song-title-region-correction-selection/releases) 下载 `song-title-region-correction-selection.zip`。
2. 打开 Apple Music → AM++ 设置 → 插件 → 导入 ZIP。
3. 启用插件，完全停止并重新打开 Apple Music。
4. 在插件设置中选择地区。
5. 再次完全停止并重新打开 Apple Music，使新地区和缓存命名空间生效。

新导入的插件默认关闭。修改地区后仅返回主页不算重启。

## 实现说明

插件通过 AM++ 插件 SDK 注册方法 Hook，把 AM++ 内置的固定地区编号重映射到所选地区，主要覆盖：

- 目录 storefront
- 元数据请求语言
- 艺术家与歌曲元数据缓存键
- 持久缓存预热与共享艺人缓存

Apple Music 的账号地区、订阅可用性和播放权限不会被修改；插件只影响元数据查询与显示。

## 兼容性

当前版本参考 AM++ 源码提交：

```text
544bae60357bddf9a412871b900dda5938aa1ac6
```

插件依赖 AM++ 内部目录解析器的既有地区映射。AM++ 后续版本如果调整这些内部方法，插件可能需要同步适配。缺少目标方法时，插件会报告“不支持”，而不是绕过宿主版本检查。

已完成 Java 编译、D8 打包、纯 Java 契约测试和 AM++ 包结构/DEX 校验；尚未在真机 LSPosed 或 npatch 环境完成全功能验收。

## 从源码构建

需要 JDK 17、Android SDK Platform 37 和 Build Tools 37.0.0。仓库已包含编译用 `ampp-plugin-api-v1.jar`。

Windows：

```powershell
.\gradlew.bat clean pluginZip -PandroidSdk=C:/Android/Sdk
```

输出：

```text
build/dist/song-title-region-correction-selection.zip
```

ZIP 根目录应直接包含：

```text
plugin.json
code.jar
assets/
```

## 目录

```text
src/main/java/dev/amenhancer/plugin/regiontitle/
  RegionTitlePlugin.java   插件入口、Hook、设置页
  RegionProfile.java       地区配置
  RegionSettings.java      插件配置持久化
  SelectionRemapper.java   地区编号重映射
verification/
  RegionContractTest.java  纯 Java 契约测试
dist/                      可直接导入的发布 ZIP
```
