create table if not exists  "active_sessions" (
    refresh_token varchar(255) primary key,
    user_agent text,
    issue_date timestamp without time zone NOT NULL,
    expire_date timestamp without time zone NOT NULL,
    user_id int not null references cloud_users(id)
);