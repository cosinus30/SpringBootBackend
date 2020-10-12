
create table if not exists "template"(
     id serial primary key,
     template_name text not null unique ,
     content text not null
);