create table if not exists "bookmarks"
(
    user_id int not null references cloud_users (id),
    article_id int not null references articles (id),
    bookmark_date timestamp without time zone NOT NULL
);