create table if not exists  "attempt" (
    ip varchar(255) primary key,
    attempt_counter int not null
);