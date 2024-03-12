# AnimeCon Volunteer Manager Display App
This repository contains the Android app that powers the AnimeCon Volunteering Displays, which are
purpose built, 10-inch rooted Android tablets displaying a Volunteer Manager.

The app uses a WebView with a collection of injected, proprietary APIs that enable control over the
device's brightness and light strip (over a serial port), as well as toggling a Kiosk mode built on
top of the DPM's locked task mechanism. Many thanks to
[Suresh](https://sureshjoshi.com/mobile/android-kiosk-mode-without-root) for the ideas.

**This project is by no means intended to be useful for any other purpose than our own.** However,
all our other code is open source, so this may as well be part of that.


### Setting up the tablets
We require a rooted Android 8.0 (SDK level 26) or later device. A 1280x800 pixel screen is expected,
however the user interface most likely scales well enough for other resolutions.

#### 1. Build the Android app
This repository contains the entire Android Studio project. A small amount of native code is
included for communicating with the serial port for the light strip. Build the APK and deploy it to
the device.

#### 2. Grant the "Device admin apps" special app permission

#### 3. Grant the "Modify system settings" special app permission

#### 4. Grant device owner status to the app
```
adb shell dpm set-device-owner team.animecon.display/.AdminReceiver
```

