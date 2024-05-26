CREATE TABLE avito_cost (                                                                                              region VARCHAR(64),
    id SERIAL PRIMARY KEY,
    category VARCHAR(64),
    subcategory VARCHAR(64),
    subcategory1 VARCHAR(64),
    subcategory2 VARCHAR(64),
    subcategory3 VARCHAR(64),
    subcategory4 VARCHAR(64),
    subcategory5 VARCHAR(64),
    cost NUMERIC(4,2)
);