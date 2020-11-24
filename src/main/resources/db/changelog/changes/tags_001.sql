create table if not exists "tags"
(
    id serial primary key,
    tag_name varchar(50) not null unique,
    tag_detail text default null,
    article_count int not null default 0
);
