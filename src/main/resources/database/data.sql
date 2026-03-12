-- SQL create scripts for Account module

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(100) NOT NULL UNIQUE,
    account_name VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    website VARCHAR(255),
    country VARCHAR(100),
    country_code VARCHAR(10),
    currency VARCHAR(10),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(50),
    zipcode VARCHAR(20),
    status VARCHAR(50),
    previous_status VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_account_id UNIQUE (account_id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_account_id ON accounts(account_id);
CREATE INDEX IF NOT EXISTS idx_status ON accounts(status);
-- Address search indexes for fast lookups
CREATE INDEX IF NOT EXISTS idx_zipcode ON accounts(zipcode);
CREATE INDEX IF NOT EXISTS idx_city_state ON accounts(city, state);
CREATE INDEX IF NOT EXISTS idx_state_zipcode ON accounts(state, zipcode);

-- Create idempotency_keys table for idempotency support
CREATE TABLE IF NOT EXISTS idempotency_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    request_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(500) NOT NULL,
    response_status INT NOT NULL,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_idempotency_key UNIQUE (idempotency_key)
);

-- Create index for idempotency key lookups
CREATE INDEX IF NOT EXISTS idx_idempotency_key ON idempotency_keys(idempotency_key);

-- Insert sample auto shop accounts
INSERT INTO accounts (account_id, account_name, email, website, country, country_code, currency, address_line1, city, state, zipcode, status, created_at) VALUES
('ACC-000001', 'Midas Auto Service', 'contact@midas.com', 'https://www.midas.com', 'United States', 'US', 'USD', '123 Main Street', 'Chicago', 'IL', '60601', 'ACTIVE', CURRENT_TIMESTAMP),
('ACC-000002', 'Jiffy Lube', 'info@jiffylube.com', 'https://www.jiffylube.com', 'United States', 'US', 'USD', '456 Oak Avenue', 'Los Angeles', 'CA', '90001', 'ACTIVE', CURRENT_TIMESTAMP),
('ACC-000003', 'Firestone Complete Auto Care', 'support@firestone.com', 'https://www.firestonecompleteautocare.com', 'United States', 'US', 'USD', '789 Pine Road', 'Houston', 'TX', '77001', 'ACTIVE', CURRENT_TIMESTAMP),
('ACC-000004', 'Meineke Car Care Center', 'contact@meineke.com', 'https://www.meineke.com', 'United States', 'US', 'USD', '321 Elm Street', 'Phoenix', 'AZ', '85001', 'ACTIVE', CURRENT_TIMESTAMP),
('ACC-000005', 'Pep Boys', 'info@pepboys.com', 'https://www.pepboys.com', 'United States', 'US', 'USD', '654 Maple Drive', 'Philadelphia', 'PA', '19101', 'ACTIVE', CURRENT_TIMESTAMP),
('ACC-000006', 'Goodyear Auto Service', 'support@goodyear.com', 'https://www.goodyearautoservice.com', 'United States', 'US', 'USD', '987 Cedar Lane', 'San Antonio', 'TX', '78201', 'PENDING', CURRENT_TIMESTAMP),
('ACC-000007', 'NTB Tire & Service', 'contact@ntb.com', 'https://www.ntb.com', 'United States', 'US', 'USD', '147 Birch Boulevard', 'San Diego', 'CA', '92101', 'ACTIVE', CURRENT_TIMESTAMP),
('ACC-000008', 'Big O Tires', 'info@bigotires.com', 'https://www.bigotires.com', 'United States', 'US', 'USD', '258 Spruce Court', 'Dallas', 'TX', '75201', 'ACTIVE', CURRENT_TIMESTAMP);
