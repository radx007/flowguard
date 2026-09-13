CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              idempotency_key VARCHAR(255) NOT NULL UNIQUE,
                              amount NUMERIC(19, 2) NOT NULL,
                              currency VARCHAR(3) NOT NULL,
                              merchant_id VARCHAR(255) NOT NULL,
                              status VARCHAR(32) NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL
);