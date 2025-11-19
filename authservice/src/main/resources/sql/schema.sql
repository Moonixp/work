-- drop schema public cascade;

-- create schema public;

-- create table if not exists users (
--     id UUID primary key default gen_random_uuid(),
--     username TEXT unique not null,
--     email TEXT unique not null,
--     password_hash VARCHAR(255) not null,
--  	role VARCHAR CHECK (role IN ('MANAGER', 'USER')),
--     created_at TIMESTAMPTZ not null default NOW(),
--     updated_at TIMESTAMPTZ not null default NOW(),
--     deleted_at TIMESTAMPTZ
-- );

create table if not exists auth (
    user_id UUID primary key not null, -- references the users_id
    email TEXT unique not null,
    password_hash VARCHAR(255) not null,
 	role VARCHAR CHECK (role IN ('MANAGER', 'USER')),
    created_at TIMESTAMPTZ not null default NOW(),
    updated_at TIMESTAMPTZ not null default NOW(),
    deleted_at TIMESTAMPTZ
);
