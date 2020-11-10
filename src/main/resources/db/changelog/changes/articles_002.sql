create table if not exists  "articles" (
    id serial primary key,
    content text NOT NULL,
    published boolean NOT NULL default false,
    release_date timestamp without time zone NOT NULL,
    content_type text NOT NULL,
    read_time int NOT NULL,
    author int not null references cloud_users(id)
);