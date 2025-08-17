-- Creating user and granting access rights
CREATE USER bank_user WITH PASSWORD 'bank_password';
CREATE DATABASE bank OWNER bank_user;

-- Granting rights to the public schema
GRANT ALL PRIVILEGES ON DATABASE bank TO bank_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO bank_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO bank_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO bank_user;

-- Granting rights to future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO bank_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO bank_user;
