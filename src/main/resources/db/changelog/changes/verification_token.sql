create table if not exists "verification_token" (
    id serial primary key,
    created_date date not null,
    expiry_date date not null,
    token varchar(255) not null,
    user_id int not null references cloud_users(id)
);