CREATE TABLE catalog_product (
    sku VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    unit_price BIGINT NOT NULL CHECK (unit_price > 0),
    currency VARCHAR(3) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE inventory_item (
    sku VARCHAR(64) PRIMARY KEY REFERENCES catalog_product(sku),
    on_hand BIGINT NOT NULL CHECK (on_hand >= 0),
    reserved BIGINT NOT NULL DEFAULT 0 CHECK (reserved >= 0),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE shopping_cart (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(128) NOT NULL,
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE cart_item (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES shopping_cart(id),
    sku VARCHAR(64) NOT NULL REFERENCES catalog_product(sku),
    product_name VARCHAR(128) NOT NULL,
    unit_price BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    UNIQUE(cart_id, sku)
);

CREATE TABLE sales_order (
    id UUID PRIMARY KEY,
    order_number VARCHAR(64) NOT NULL UNIQUE,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    customer_id VARCHAR(128) NOT NULL,
    cart_id UUID NOT NULL REFERENCES shopping_cart(id),
    status VARCHAR(32) NOT NULL,
    total_amount BIGINT NOT NULL,
    voucher_amount BIGINT NOT NULL DEFAULT 0,
    payable_amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_id UUID REFERENCES payment_intent(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE order_line (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES sales_order(id),
    sku VARCHAR(64) NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    unit_price BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    line_total BIGINT NOT NULL
);

CREATE TABLE voucher_wallet (
    customer_id VARCHAR(128) PRIMARY KEY,
    balance BIGINT NOT NULL DEFAULT 0 CHECK (balance >= 0),
    currency VARCHAR(3) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE voucher_entry (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(128) NOT NULL REFERENCES voucher_wallet(customer_id),
    entry_type VARCHAR(16) NOT NULL,
    amount BIGINT NOT NULL CHECK (amount > 0),
    entry_reference VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_order_customer_created ON sales_order(customer_id, created_at);
CREATE INDEX idx_voucher_entry_customer_created ON voucher_entry(customer_id, created_at);

INSERT INTO catalog_product(sku, name, unit_price, currency, active) VALUES
    ('SKU-COFFEE', 'Origin Coffee Beans', 1899, 'CNY', TRUE),
    ('SKU-MUG', 'System Design Mug', 2499, 'CNY', TRUE),
    ('SKU-HOODIE', 'Platform Engineering Hoodie', 6999, 'CNY', TRUE);

INSERT INTO inventory_item(sku, on_hand, reserved, version) VALUES
    ('SKU-COFFEE', 100, 0, 0),
    ('SKU-MUG', 60, 0, 0),
    ('SKU-HOODIE', 30, 0, 0);
