create table if not exists  "article_comments" (
    comment_id serial primary key,
    content text not null,
    comment_date timestamp without time zone NOT NULL,
    user_id int not null references cloud_users(id),
    article_id int not null references articles(id)
);