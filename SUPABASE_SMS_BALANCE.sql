create table if not exists public.sms_balance (
  id integer primary key default 1,
  balance_units numeric not null default 0,
  total_added numeric not null default 0,
  total_used numeric not null default 0,
  updated_at timestamptz not null default now(),
  updated_by uuid references auth.users(id),
  constraint sms_balance_singleton check (id = 1),
  constraint sms_balance_nonnegative check (balance_units >= 0)
);
insert into public.sms_balance(id,balance_units,total_added,total_used)
values (1,0,0,0)
on conflict (id) do nothing;
alter table public.sms_balance enable row level security;
drop policy if exists sms_balance_staff_select on public.sms_balance;
create policy sms_balance_staff_select on public.sms_balance for select to authenticated using (exists (select 1 from public.user_profiles up where up.id=auth.uid() and up.role in ('Super Admin','Staff') and up.access_status='Approved'));
drop policy if exists sms_balance_superadmin_update on public.sms_balance;
create policy sms_balance_superadmin_update on public.sms_balance for update to authenticated using (exists (select 1 from public.user_profiles up where up.id=auth.uid() and up.role='Super Admin' and up.access_status='Approved')) with check (exists (select 1 from public.user_profiles up where up.id=auth.uid() and up.role='Super Admin' and up.access_status='Approved'));
drop policy if exists sms_balance_superadmin_insert on public.sms_balance;
create policy sms_balance_superadmin_insert on public.sms_balance for insert to authenticated with check (exists (select 1 from public.user_profiles up where up.id=auth.uid() and up.role='Super Admin' and up.access_status='Approved'));
