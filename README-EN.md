# Moon Suggestion

[中文](README.md)| **English** 



This is an open-source application inspired by the **Aicy Suggestion** feature found in the Flyme custom ROM.

## Features



- **Seamless Integration** :
  The app integrates smoothly into the system settings under **Settings → System → Useful Suggestions** 
- **Context-Aware Recommendations Based on User Actions** :
  - When the screen dimming is canceled, it is recommended to keep the screen on until the device is locked.
  - When headphones are connected, a music playback application is suggested.
  - When the flashlight is turned on, the system recommends turning it off after use.
  - When copying links from apps such as Taobao or Baidu Netdisk, the corresponding application will be suggested for opening the link.

## Integration Instructions



To integrate this module into your AOSP-based ROM:

1. Sync this repository into the source tree at:
   `packages/apps/ExthmUseful`
2. Add the following line to your build configuration to include the package:

```
# useful_suggestion
PRODUCT_PACKAGES += \
	ExthmUseful
```

Apply the following commits to ensure full functionality:

- For **screen suggestion support** :
  [Base: Add exthmUseful_screenUseful broadcast support](https://github.com/RUYUE-AOSP-STUDIO/android_frameworks_base/commit/eac7f1c88e2f4571f67071ec3241d2d3b203d62f)
- For **clipboard suggestion support** :
  [Base: Add exthmusable read clipboard support](https://github.com/RUYUE-AOSP-STUDIO/android_frameworks_base/commit/eac7f1c88e2f4571f67071ec3241d2d3b203d62f)