DROP TABLE IF EXISTS favourite_items;

CREATE TABLE ads
(
    id SERIAL PRIMARY KEY,
    avito_id BIGINT NOT NULL,
    owner_id INTEGER,
    cost NUMERIC (4,2),
    CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES account_data(id)
)
