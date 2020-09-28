DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

CREATE TABLE users(
    username varchar(100) NOT NULL UNIQUE,
    email varchar(100) NOT NULL UNIQUE,
    password varchar(100) NOT NULL,
    id INT GENERATED ALWAYS AS IDENTITY
);

CREATE TABLE roles(
    role varchar(100) NOT NULL UNIQUE,
    id INT GENERATED ALWAYS AS IDENTITY
);