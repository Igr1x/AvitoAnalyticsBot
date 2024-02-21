ALTER TABLE account_data
ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);