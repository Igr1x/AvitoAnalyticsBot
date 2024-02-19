ALTER TABLE users
ADD balance NUMERIC(18, 2);

ALTER TABLE users
ADD rate_id integer;

ALTER TABLE users
ADD constraint fk_rates FOREIGN KEY(rate_id) REFERENCES rates(id);