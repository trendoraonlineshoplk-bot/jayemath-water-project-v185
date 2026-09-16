V183 - ALL OPTIONS + UNIVERSAL LOGIN + SMS BALANCE

Based on V181 Complete App + Balance Correction + Cover.

Login:
- Customer, Staff and Super Admin login are available on BOTH PC and phone/Android.
- No device-based restriction is used.
- Staff keeps Name + Email + Phone + Password validation and Super Admin approval.
- Customer uses Account Number + last 4 digits of registered phone.

Existing features retained:
Billing, payments, reports, SMS, water service automation, meter replacement, staff management,
correction/void workflow, printer setup, online payment queue, settings/tariffs, PWA cover,
Android XP-P301A Bluetooth bridge, backup/restore, offline queue.

New:
- SMS Balance menu for Super Admin and Staff.
- Shared Supabase sms_balance table.
- Super Admin can add/adjust SMS units.
- Successful SMS sends automatically reduce the shared SMS balance using provider sms_count.
- Staff can view balance but cannot change it.
- send-sms Edge Function now logs sent SMS and updates the shared balance.

The SMS balance is an app-side shared credit tracker. Text.lk's current public API docs document SMS
sending and SMS reports but do not expose a documented account-credit balance endpoint. Therefore the
Super Admin updates the app balance after each Text.lk top-up.

Build/sign the Android project with Android Studio/Gradle to produce the APK.
