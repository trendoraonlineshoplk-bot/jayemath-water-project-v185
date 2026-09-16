JAYEMATH WATER BILLING v177 - XP-P301A ANDROID BLUETOOTH DIRECT PRINT

This version keeps the existing web app features and adds a native Android Bluetooth Classic bridge for Xprinter XP-P301A.

Printer confirmed from user photo: Xprinter XP-P301A, USB + Bluetooth, ESC/POS receipt printing.
Xprinter specifications list XP-P301A with USB + Bluetooth and ESC/POS receipt mode.

HOW TO USE ON ANDROID
1. Build/install this Android project from Android Studio.
2. Turn on the XP-P301A and Android Bluetooth.
3. Open Android Bluetooth settings and pair the XP-P301A first.
4. Open the Jayemath app -> Printer Setup.
5. Press "Connect XP-P301A".
6. Select the paired XP-P301A from the native Bluetooth list.
7. Press "Test Print".
8. After a successful test, normal Bill Print will send ESC/POS directly to the connected printer.

IMPORTANT
- The native bridge uses Bluetooth Classic SPP UUID 00001101-0000-1000-8000-00805F9B34FB.
- Existing browser/Windows print and the previous BLE option are retained.
- This package is Android Studio source code. An APK is not included because this environment does not have the Android SDK/Gradle build toolchain installed.
- Supabase cloud settings can be placed in app/src/main/assets/config.js if the bundled local app needs them.
- If the user's current deployment is hosted online, the same native bridge JavaScript can also be integrated into a WebView/Capacitor Android wrapper that loads that hosted app.
