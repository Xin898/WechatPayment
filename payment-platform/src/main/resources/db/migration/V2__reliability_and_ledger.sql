ALTER TABLE payment_intent
    ADD COLUMN refunded_amount BIGINT NOT NULL DEFAULT 0 CHECK (refunded_amount >= 0);

CREATE TABLE refund (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payment_intent(id),
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    amount BIGINT NOT NULL CHECK (amount > 0),
    status VARCHAR(32) NOT NULL,
    failure_code VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_refund_payment_created ON refund(payment_id, created_at);

CREATE TABLE ledger_transaction (
    id UUID PRIMARY KEY,
    reference_type VARCHAR(32) NOT NULL,
    reference_id UUID NOT NULL,
    description VARCHAR(256) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_ledger_transaction_reference ON ledger_transaction(reference_id, created_at);

CREATE TABLE ledger_entry (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES ledger_transaction(id),
    account_code VARCHAR(96) NOT NULL,
    side VARCHAR(8) NOT NULL,
    amount BIGINT NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(32) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(256)
);

CREATE INDEX idx_outbox_dispatch ON outbox_event(status, available_at, created_at);

CREATE TABLE webhook_delivery (
    id UUID PRIMARY KEY,
    outbox_event_id UUID NOT NULL UNIQUE REFERENCES outbox_event(id),
    endpoint VARCHAR(256) NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    failures_before_success INTEGER NOT NULL DEFAULT 2,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_status_code INTEGER,
    last_error VARCHAR(256),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_webhook_retry ON webhook_delivery(status, next_attempt_at);
