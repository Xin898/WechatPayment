CREATE TABLE payment_intent (
    id UUID PRIMARY KEY,
    merchant_order_id VARCHAR(128) NOT NULL UNIQUE,
    amount BIGINT NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider_reference VARCHAR(128),
    failure_code VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE idempotency_record (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    request_hash VARCHAR(64) NOT NULL,
    payment_id UUID NOT NULL REFERENCES payment_intent(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE payment_event (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payment_intent(id),
    type VARCHAR(64) NOT NULL,
    detail VARCHAR(256) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_payment_event_payment_created
    ON payment_event(payment_id, created_at);
