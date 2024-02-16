CREATE TABLE users (
    id serial NOT NULL constraint w_users_pk PRIMARY KEY,
    telegram_id varchar(255),
    client_id varchar(255),
    client_secret varchar(255),
    google_table_url varchar(255)
);

