
create table if not exists "template"(
     id serial primary key,
     template_name varchar not null unique ,
     content varchar not null
);