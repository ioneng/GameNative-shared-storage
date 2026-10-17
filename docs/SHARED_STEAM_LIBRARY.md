# Shared internal Steam library (experimental)

GameNative and DroidDeck can refer to the **same game files** on Android's primary shared storage. GameNative still manages its own Wine prefix, configuration and save data. The game files live at:

```text
/storage/emulated/0/GameNative/Steam/
└── steamapps/
    ├── appmanifest_<appid>.acf
    └── common/
        └── <game directory>/
```

The path belongs to the current Android user; the `/storage/emulated/0` component may differ on secondary profiles.

## Setup

1. Install a compatible build of this fork. **Back up your existing GameNative app data before replacing any APK.** A differently signed APK with the same package name cannot update an existing installation; uninstalling the original may erase its private games, prefixes and saves.
2. In GameNative's storage settings, grant **Enable shared storage access** if prompted. Android 11+ requires the optional **All files access** permission to write the public `GameNative` directory in the Modern build. This fork requests it, but never grants it automatically.
3. Enable the existing additional/external game storage option, then select **Shared internal storage (GameNative)**. This is an opt-in target; the normal private install location is unchanged by default.
4. Use GameNative's **Container storage manager → Move to external storage** on one small installed Steam game. Its external target now means the public internal folder when that option is selected. The move operation migrates the files rather than keeping two complete copies, but temporarily needs free space for at least the file being copied.
5. Launch the game once in GameNative if it has never generated a Steam manifest. GameNative will publish an existing/generated `.acf` manifest beside the shared `common` directory. If a manifest already exists there (e.g. modified by Steam in DroidDeck), GameNative does not overwrite it.
6. In DroidDeck's **Second library** settings, choose `/storage/emulated/0/GameNative/Steam`. DroidDeck should see the existing game files, subject to Steam ownership and compatibility. It may still verify or download missing/changed files.

No symbolic link is created in Android's public emulated storage. GameNative keeps the Wine-side symbolic link in its private filesystem and repoints it after a move; DroidDeck makes its own library registration.

## Limitations and risks

- All-files access is a powerful Android permission, particularly in the Modern flavor. Do not enable shared storage unless you want this access.
- This is a single shared **game-file library**, not a shared Wine/Proton prefix or a shared save directory. Launchers may have separate configuration and save data.
- Steam updates, game modifications, verification or uninstalling in either application can alter the files the other application uses. Do not run both launchers against the same game simultaneously, and treat either app's uninstall as destructive to the other app's reference.
- GameNative's move utility moves files incrementally. If a move is interrupted, source and destination may each contain part of the game; **do not delete either folder**, and do not retry blindly until recovery is checked.
- Exporting existing Steam manifests depends on GameNative having generated the private manifest. The public `.acf` is not a guaranteed substitute for depot validation; Steam may still verify some files.
- This is not a tested workaround for Android app sandbox permissions or the signatures of installed APKs. Confirm that the shared storage directory is writable before moving any game.

The original DroidDeck application does not need to be patched to choose a second library.
