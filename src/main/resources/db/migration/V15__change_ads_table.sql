ALTER TABLE ads
DROP COLUMN closing_date;

ALTER TABLE ads
DROP COLUMN pub_date;

ALTER TABLE ads
ADD COLUMN city varchar(64);

ALTER TABLE ads
ADD COLUMN pub_date date;

ALTER TABLE ads
ADD COLUMN closing_date date;