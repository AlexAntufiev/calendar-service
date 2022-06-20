create table users
(
    email text primary key
);

create table meetup
(
    id         bigserial primary key,
    name       text,
    organizer  text        not null,
    guests     text[]      not null,
    start_time timestamptz not null,
    end_time   timestamptz not null,
    repeat     smallint    not null,
    privacy    smallint    not null
);

create table calendar
(
    id        bigserial primary key,
    email     text     not null,
    date      date     not null,
    meetup_id bigint   not null,
    status    smallint not null
);

create index calendar_email_status_idx ON calendar (email, status) where calendar.status <> 2;
