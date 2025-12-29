-- Create the authdb database
CREATE DATABASE authdb;
CREATE USER auth_user WITH PASSWORD 'auth_pass';
GRANT ALL PRIVILEGES ON DATABASE authdb TO auth_user;

-- Connect to authdb and grant schema privileges
\c authdb;
GRANT ALL ON SCHEMA public TO auth_user;

-- Create the resourcedb database
CREATE DATABASE resourcedb;
CREATE USER resource_user WITH PASSWORD 'resource_pass';
GRANT ALL PRIVILEGES ON DATABASE resourcedb TO resource_user;

-- Connect to resourcedb and grant schema privileges
\c resourcedb;
GRANT ALL ON SCHEMA public TO resource_user;

-- Create the bookingdb database
CREATE DATABASE bookingdb;
CREATE USER booking_user WITH PASSWORD 'booking_pass';
GRANT ALL PRIVILEGES ON DATABASE bookingdb TO booking_user;

\c bookingdb;
GRANT ALL ON SCHEMA public TO booking_user;

-- Create the marketplacedb database
CREATE DATABASE marketplacedb;
CREATE USER marketplace_user WITH PASSWORD 'marketplace_pass';
GRANT ALL PRIVILEGES ON DATABASE marketplacedb TO marketplace_user;

\c marketplacedb;
GRANT ALL ON SCHEMA public TO marketplace_user;

-- Create the paymentdb database
CREATE DATABASE paymentdb;
CREATE USER payment_user WITH PASSWORD 'payment_pass';
GRANT ALL PRIVILEGES ON DATABASE paymentdb TO payment_user;

\c paymentdb;
GRANT ALL ON SCHEMA public TO payment_user;
