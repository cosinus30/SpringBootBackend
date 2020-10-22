
create table if not exists "email_templates"(
     id serial primary key,
     template_name text not null unique ,
     content text not null
);