Jayemath Water Project App v176

Added without removing existing features:
- Mobile Bluetooth BLE direct ESC/POS printing for 80mm thermal printers.
- Connect Mobile Bluetooth button in Printer Setup.
- Test Print and Disconnect buttons.
- Bill printing automatically uses BLE direct connection first, then existing Windows COM direct print, then existing browser print.

Important:
- Mobile browser direct printing requires Android Chrome/Edge with Web Bluetooth and HTTPS.
- The thermal printer must expose BLE/GATT writable ESC/POS characteristics.
- Bluetooth Classic/SPP-only printers cannot be directly controlled by mobile browser; they need a native Android Bluetooth bridge/app.
- Existing printer, billing, SMS, staff, Supabase and browser-print options are retained.
