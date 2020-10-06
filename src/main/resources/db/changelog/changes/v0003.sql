create table if not exists  "cloud_users" (
    id serial primary key ,
    username varchar(50) not null unique,
    email varchar(100) not null unique ,
    password varchar(255) not null,
    enabled boolean not null
);

create table if not exists "cloud_roles" (
    id serial primary key ,
    role varchar(50) not null unique,
    is_account_admin boolean not null default false
);

create table if not exists "user_roles" (
    user_id int not null references cloud_users(id),
    role_id int not null references cloud_roles(id)
);
