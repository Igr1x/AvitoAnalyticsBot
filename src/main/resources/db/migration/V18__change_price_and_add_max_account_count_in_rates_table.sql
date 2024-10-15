ALTER TABLE rates
ADD COLUMN max_account_count INTEGER;

UPDATE rates
SET max_account_count = 0
WHERE id = 4;

UPDATE rates
SET cost = 1290, description = 'Доступно подключение до 3-х Авито аккаунтов', max_account_count = 3
WHERE id = 1;

UPDATE rates
SET cost = 2350, description = 'Доступно подключение до 6-и Авито аккаунтов', max_account_count = 6
WHERE id = 2;

UPDATE rates
SET cost = 4990, description = 'Доступно подключение до 10-и Авито аккаунтов', max_account_count = 10
WHERE id = 3;

ALTER TABLE rates
ADD COLUMN cost_per_day INTEGER;

UPDATE rates
SET cost_per_day = cost / 30;