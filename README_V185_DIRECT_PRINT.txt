JAYEMATH V185.1 — NATIVE ANDROID DIRECT PRINT BUILD

This build combines the V185 web app (including the Customers loading fix and sidebar scroll fix)
with the native Android Bluetooth Classic SPP bridge for Xprinter XP-P301A 80mm ESC/POS printing.

Kept from V185:
- Customers, Meter Readings, Billing, Payments
- Online Payments
- Reports, SMS, SMS Balance
- Printer Setup
- Users/Staff, Settings, Data Management
- App icon/PWA assets
- Customer cloud loading runs in background instead of blocking the Customers page
- Sidebar can scroll on phone/tablet

Direct phone printing:
1. Install the Android APK built from this project.
2. Turn on XP-P301A and Android Bluetooth.
3. Pair XP-P301A in Android Bluetooth settings first.
4. Open the app -> Printer Setup.
5. Tap "Connect XP-P301A" and select the paired printer.
6. Tap "Test Print".
7. Use the bill Print button; the app sends ESC/POS bytes directly to the printer.

The browser/PWA version cannot provide reliable Classic Bluetooth SPP direct printing for this printer;
the Android native bridge is what provides the phone direct-print path.

APK build workflow:
- .github/workflows/build-apk.yml builds a debug APK using GitHub Actions.
- Output: app/build/outputs/apk/debug/*.apk
