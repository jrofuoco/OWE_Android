-- Announcements & Dynamic App Config tables for DebtCollect
-- Run in Supabase SQL Editor (or via `supabase db push`)

create table if not exists public.announcements (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    message text not null,
    type text not null default 'info' check (type in ('info', 'promo', 'warning', 'maintenance')),
    action_label text,
    action_route text,
    is_active boolean not null default true,
    starts_at timestamptz,
    ends_at timestamptz,
    created_at timestamptz not null default now()
);

create index if not exists idx_announcements_active_created_at
    on public.announcements (created_at desc)
    where is_active = true;

alter table public.announcements enable row level security;

drop policy if exists "announcements_select_active" on public.announcements;
create policy "announcements_select_active"
    on public.announcements
    for select
    to anon, authenticated
    using (
        is_active = true
        and (starts_at is null or starts_at <= now())
        and (ends_at is null or ends_at >= now())
    );

create table if not exists public.app_config (
    key text primary key,
    value jsonb not null,
    description text,
    updated_at timestamptz not null default now()
);

alter table public.app_config enable row level security;

drop policy if exists "app_config_select_all" on public.app_config;
create policy "app_config_select_all"
    on public.app_config
    for select
    to anon, authenticated
    using (true);

grant select on public.announcements, public.app_config to anon, authenticated;

insert into public.app_config (key, value, description)
values
    ('pro_monthly_price', '49'::jsonb, 'Monthly OWE Pro price in PHP'),
    ('pro_lifetime_price', '199'::jsonb, 'One-time OWE Pro price in PHP'),
    ('free_active_debt_limit', '5'::jsonb, 'Max active debts on Free tier'),
    ('interstitial_cooldown_minutes', '5'::jsonb, 'Minimum minutes between interstitial ads'),
    ('ads_enabled', 'true'::jsonb, 'Global toggle for Free-tier ads'),
    ('maintenance_mode', 'false'::jsonb, 'Global maintenance mode flag')
on conflict (key) do nothing;

-- Enable Supabase Realtime on announcements and app_config
do $$
begin
    alter publication supabase_realtime add table public.announcements;
exception when duplicate_object then null;
end $$;

do $$
begin
    alter publication supabase_realtime add table public.app_config;
exception when duplicate_object then null;
end $$;
