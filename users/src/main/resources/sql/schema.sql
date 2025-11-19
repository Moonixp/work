-- drop schema public cascade;

-- create schema public;

create table if not exists users (
    id UUID primary key default gen_random_uuid(),
    username TEXT unique not null,
    image TEXT not null,
    created_at TIMESTAMPTZ not null default NOW(),
    updated_at TIMESTAMPTZ not null default NOW(),
    deleted_at TIMESTAMPTZ
);
