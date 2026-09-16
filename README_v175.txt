JAYEMATH WATER PROJECT - v175 STAFF EMAIL CONFIRMATION

ADDITIVE CHANGE ONLY
- Existing customers, billing, payments, SMS, reports, printers, login, Staff Approve/Reject/Revoke options are unchanged.
- Added/strengthened the Super Admin Staff Email confirmation button.
- Pending Staff: Approve + Confirm Email + Reject remain available.
- Approved Staff: Confirm Email + Revoke Access are available.
- Rejected/Revoked Staff: Approve Again + Confirm Email are available.
- Clicking "Confirm Email" calls the secure Supabase RPC confirm_staff_email.
- Email confirmation is separate from Staff access approval. Super Admin can confirm the email first, then Approve access.

IMPORTANT SUPABASE STEP
If your Supabase project does not already have the confirm_staff_email function, run the included:
SUPABASE_STAFF_EMAIL_CONFIRM.sql

Run it in Supabase Dashboard -> SQL Editor -> New query -> Run.

After running it:
1. Open the app.
2. Login as Super Admin.
3. Open Users & Security.
4. Refresh Staff Requests.
5. For the newly created Staff account, click "Confirm Email".
6. Then click "Approve" if the Staff member should be allowed to log in.

The SQL is additive and uses SECURITY DEFINER with a Super Admin role check. It does not delete or alter the existing Staff access workflow.
