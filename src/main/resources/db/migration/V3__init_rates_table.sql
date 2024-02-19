CREATE TABLE rates(
    id serial NOT NULL constraint w_rates_pk PRIMARY KEY,
    title varchar(255),
    description text
);