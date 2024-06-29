ALTER TABLE ads
DROP COLUMN closing_date;

ALTER TABLE ads
ADD COLUMN pub_date date;

ALTER TABLE ads
ADD COLUMN closing_date date;