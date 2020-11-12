create table if not exists "views"
(
    user_id int not null references cloud_users (id),
    article_id int not null references articles (id),
    view_date timestamp without time zone NOT NULL
);