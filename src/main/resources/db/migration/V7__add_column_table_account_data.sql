ALTER TABLE account_data
ADD account_name varchar(255);

ALTER TABLE account_data
RENAME COLUMN user_id TO user_owner_id;

ALTER TABLE account_data
ADD user_id integer;