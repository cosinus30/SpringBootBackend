create table if not exists "likes"
(
    user_id int not null references cloud_users (id),
    article_id int not null references articles (id),
    like_date timestamp without time zone NOT NULL
);