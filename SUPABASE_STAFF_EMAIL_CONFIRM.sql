-- Jayemath Water Project - Staff Email Confirmation
-- This is an additive change only. It does not remove or change the existing
-- Staff Approve / Reject / Revoke workflow.

create or replace function public.confirm_staff_email(p_user_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public, auth
as $$
declare
  caller_role text;
  target_role text;
  target_email text;
begin
  select role into caller_role
  from public.user_profiles
  where id = auth.uid();

  if coalesce(caller_role, '') <> 'Super Admin' then
    raise exception 'Only Super Admin can confirm Staff email.';
  end if;

  select role into target_role
  from public.user_profiles
  where id = p_user_id;

  if coalesce(target_role, '') <> 'Staff' then
    raise exception 'Selected account is not a Staff account.';
  end if;

  select email into target_email
  from auth.users
  where id = p_user_id;

  if target_email is null then
    raise exception 'Staff Auth user was not found.';
  end if;

  update auth.users
  set email_confirmed_at = coalesce(email_confirmed_at, now()),
      updated_at = now()
  where id = p_user_id;

  return jsonb_build_object(
    'ok', true,
    'user_id', p_user_id,
    'email', target_email,
    'email_confirmed', true
  );
end;
$$;

revoke all on function public.confirm_staff_email(uuid) from public;
grant execute on function public.confirm_staff_email(uuid) to authenticated;
