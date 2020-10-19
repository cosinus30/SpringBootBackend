create table if not exists  "token_blacklist" (
      token text primary key not null unique,
      type varchar(50) not null
);