CREATE TABLE account_data (
    id serial NOT NULL constraint w_account_data_pk PRIMARY KEY,
    user_id integer,
    client_id varchar(255) NOT NULL UNIQUE,
    client_secret varchar(255),
    sheets_ref varchar(255) NOT NULL
);

CREATE TABLE rates(
    id serial NOT NULL constraint w_rates_pk PRIMARY KEY,
    title varchar(255),
    cost numeric(18,2),
    description text
);

CREATE TABLE users (
    id serial NOT NULL constraint w_users_pk PRIMARY KEY,
    username varchar(255) NOT NULL,
    telegram_id varchar(255) NOT NULL UNIQUE,
    account_id integer,
    balance numeric(18, 2) default 0.0,
    rate_id integer default 4,
    CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES account_data(id),
    CONSTRAINT fk_rate FOREIGN KEY (rate_id) REFERENCES rates(id)
);