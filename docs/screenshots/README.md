# Screenshot capture guide

These screenshots are real TLS Lab application states used by the root GitHub README.

## Capture environment

- AVD: `Pixel_3a_API_36`
- Resolution: `1080 x 2220`
- Theme: light
- Font/display scale: emulator defaults
- Endpoint: `https://example.com`
- App package: `pat.project.tlsandssl`

Build, start a clean emulator, and install the debug APK:

```bash
./gradlew :app:assembleDebug
$ANDROID_HOME/emulator/emulator -avd Pixel_3a_API_36 -no-snapshot-save
adb wait-for-device
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am force-stop pat.project.tlsandssl
adb shell am start -n pat.project.tlsandssl/.MainActivity
```

Capture a state at native resolution, then create the committed half-size display asset:

```bash
adb exec-out screencap -p > /tmp/tls-lab-state.png
sips --resampleWidth 540 /tmp/tls-lab-state.png --out docs/screenshots/state.png
```

## Required states

| Stable filename | State to capture |
| --- | --- |
| `home.png` | Fresh launch showing the value proposition and first two feature cards. |
| `handshake.png` | Open **TLS 1.3 handshake** on the `ClientHello` step. |
| `https-inspector.png` | Inspect `https://example.com`; show HTTP status, TLS version, cipher suite, and peer-chain count. |
| `wrong-pin.png` | From the successful inspection, run **Wrong pin** and show the expected secure rejection. |
| `implementations.png` | Open **Реализации** and show system trust plus certificate-pinning implementation cards. |
| `enterprise.png` | Open **Enterprise playbook** and show the control selector plus the first architecture scenario. |

## Review checklist

- The screen matches the required state and comes from the current debug build.
- No token, private key, reusable credential, personal identifier, notification, or unrelated app is visible.
- Live network screenshots use only the public non-sensitive `example.com` endpoint.
- Wrong-pin behavior visibly reports the expected rejection; validation is never disabled.
- Image dimensions are consistent and the important text remains legible in GitHub.
- Filenames remain stable so README links do not break.
- The complete gallery remains reasonably small for repository clones.

After updating assets, verify links and sizes:

```bash
find docs/screenshots -name '*.png' -maxdepth 1 -print
du -ch docs/screenshots/*.png
git diff --check
```
