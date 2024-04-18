--liquibase formatted sql

--changeset steshabolk:create-bank-accounts-id-seq
CREATE SEQUENCE IF NOT EXISTS bank_accounts_id_seq START WITH 1 INCREMENT BY 1;

--changeset steshabolk:create-bank-accounts-table
CREATE TABLE IF NOT EXISTS bank_accounts
(
    id      BIGINT  DEFAULT nextval('bank_accounts_id_seq') PRIMARY KEY,
    user_id BIGINT REFERENCES users (id) ON DELETE RESTRICT,
    balance  NUMERIC DEFAULT 0.0 NOT NULL
);
