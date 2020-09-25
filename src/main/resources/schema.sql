DROP TABLE IF EXISTS users;

CREATE TABLE users(
    username varchar(100) NOT NULL UNIQUE,
    email varchar(100) NOT NULL UNIQUE,
    password varchar(100) NOT NULL,
    id INT GENERATED ALWAYS AS IDENTITY
);