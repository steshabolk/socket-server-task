--liquibase formatted sql

--changeset steshabolk:create-users-id-seq
CREATE SEQUENCE IF NOT EXISTS users_id_seq START WITH 1 INCREMENT BY 1;

--changeset steshabolk:create-users-table
CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT DEFAULT nextval('users_id_seq') PRIMARY KEY,
    login    VARCHAR(128) UNIQUE NOT NULL,
    password VARCHAR             NOT NULL
);
