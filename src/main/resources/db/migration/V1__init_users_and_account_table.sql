CREATE TABLE users (
    id serial NOT NULL constraint w_users_pk PRIMARY KEY,
    telegram_id varchar(255)
);

CREATE TABLE account_data (
    id serial NOT NULL constraint w_account_data_pk PRIMARY KEY,
    user_id integer,
    client_id varchar(255),
    client_secret varchar(255),
    shets_ref varchar(255),
    constraint fk_users FOREIGN KEY(user_id) REFERENCES users(id)
);

