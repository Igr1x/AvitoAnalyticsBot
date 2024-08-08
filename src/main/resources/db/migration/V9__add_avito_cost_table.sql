CREATE TABLE avito_cost (                                                                                              region VARCHAR(64),
    id SERIAL PRIMARY KEY,
    category VARCHAR(128),
    subcategory VARCHAR(128),
    subcategory1 VARCHAR(128),
    subcategory2 VARCHAR(128),
    subcategory3 VARCHAR(128),
    subcategory4 VARCHAR(128),
    subcategory5 VARCHAR(128),
    cost NUMERIC(4,2)
);