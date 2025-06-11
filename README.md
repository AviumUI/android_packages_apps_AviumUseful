# Moon Suggestion

**中文** | [English](README-EN.md) 



灵感来源于 Flyme 定制 ROM 中的 **Aicy 建议** 功能。

## 功能特性



- **无缝集成**： 应用可无缝集成到系统设置中（路径：**设置 → 系统 → 便利功能**）
- **基于用户操作的上下文感知推荐**：
  - 当取消屏幕变暗时，建议保持屏幕常亮直到设备锁定
  - 当连接耳机时，推荐音乐播放应用
  - 当手电筒开启时，系统建议使用后及时关闭
  - 当从淘宝/百度网盘等应用复制链接时，会推荐使用对应应用打开链接

## 集成说明



要将此模块集成到基于 AOSP 的 ROM 中：

1. 将代码库同步至源码树的以下位置： `packages/apps/ExthmUseful`
2. 在构建配置中添加以下行以包含该软件包：

```
# useful_suggestion
PRODUCT_PACKAGES += \
    ExthmUseful
```

为确保完整功能，请应用以下提交：

- For **screen suggestion support** :
  [Base: Add exthmUseful_screenUseful broadcast support](https://github.com/RUYUE-AOSP-STUDIO/android_frameworks_base/commit/eac7f1c88e2f4571f67071ec3241d2d3b203d62f)
- For **clipboard suggestion support** :
  [Base: Add exthmusable read clipboard support](https://github.com/RUYUE-AOSP-STUDIO/android_frameworks_base/commit/eac7f1c88e2f4571f67071ec3241d2d3b203d62f)
