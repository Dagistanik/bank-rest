-- Создание пользователя и предоставление прав доступа
CREATE USER bank_user WITH PASSWORD 'bank_password';
CREATE DATABASE bank OWNER bank_user;

-- Предоставление прав на схему public
GRANT ALL PRIVILEGES ON DATABASE bank TO bank_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO bank_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO bank_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO bank_user;

-- Предоставление прав на будущие объекты
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO bank_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO bank_user;
