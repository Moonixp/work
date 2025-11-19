-- drop schema public cascade;

-- create schema public;

create table if not exists groups (
    id UUID primary key default gen_random_uuid(),
	name TEXT not null,
    owner_id UUID not null on delete cascade,
	created_at TIMESTAMPTZ not null default NOW(),
	updated_at TIMESTAMPTZ not null default NOW(),
	deleted_at TIMESTAMPTZ
);

create table if not exists group_members (
    group_id UUID references groups(id) on delete cascade,
	user_id UUID not null on delete	cascade,
		created_at TIMESTAMPTZ not null default NOW(),
		deleted_at TIMESTAMPTZ,
		primary key (group_id,
		user_id)
);

create table if not exists chats (
    id uuid primary key default gen_random_uuid(),
	group_id UUID references groups(id) on delete cascade,
	is_direct_chat BOOLEAN not null default false,
	created_at TIMESTAMPTZ not null default NOW(),
	updated_at TIMESTAMPTZ not null default NOW(),
	deleted_at TIMESTAMPTZ,

    constraint chk_chat_type check (
        (group_id is not null
		and is_direct_chat = false)
	or
        (group_id is null
		and is_direct_chat = true)
    )
);

create table if not exists chat_members (
    chat_id UUID references chats(id) on delete cascade,
	user_id UUID not null on delete cascade,
	joined_at TIMESTAMPTZ not null default NOW(),
	left_at TIMESTAMPTZ,
    primary key (chat_id, user_id)
);

-- drop table messages;
create table if not exists messages (
    id UUID primary key default gen_random_uuid() ,
	chat_id UUID not null references chats(id) on delete cascade,
	sender_id UUID  not null on delete	cascade,
		content TEXT not null,
		created_at TIMESTAMPTZ not null default NOW(),
		updated_at TIMESTAMPTZ not null default NOW(),
		deleted_at TIMESTAMPTZ
);

-- drop table group_applications;
create table if not exists group_applications (
    id UUID primary key default gen_random_uuid(),
    group_id UUID not null references groups(id) on delete cascade,
    user_id UUID  not null on delete cascade,
    status TEXT  default 'PENDING' check (status in ('PENDING', 'APPROVED', 'REJECTED')) not null,
    applied_at TIMESTAMPTZ not null default NOW(),
    approved_at TIMESTAMPTZ default null null,
    unique (group_id, user_id)
);