-- ============================================================================
-- GLOBALTRADE LOGISTICS CORPORATION PLATFORM
-- DATABASE SCHEMA DDL SCRIPT
-- ============================================================================

CREATE
DATABASE IF NOT EXISTS globaltrade_db;
USE
globaltrade_db;

-- 1. Permissions & Roles
CREATE TABLE IF NOT EXISTS permissions
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    description VARCHAR
(
    255
)
    );

CREATE TABLE IF NOT EXISTS roles
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    50
) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS role_permissions
(
    role_id
    BIGINT
    NOT
    NULL,
    permission_id
    BIGINT
    NOT
    NULL,
    PRIMARY
    KEY
(
    role_id,
    permission_id
),
    FOREIGN KEY
(
    role_id
) REFERENCES roles
(
    id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    permission_id
) REFERENCES permissions
(
    id
)
  ON DELETE CASCADE
    );

-- 2. Users & Refresh Tokens
CREATE TABLE IF NOT EXISTS users
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    username
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    password_hash VARCHAR
(
    255
) NOT NULL,
    email VARCHAR
(
    100
) NOT NULL,
    role_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    role_id
) REFERENCES roles
(
    id
)
    );

CREATE TABLE IF NOT EXISTS refresh_token
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    token
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    username VARCHAR
(
    50
) NOT NULL,
    expiry_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 3. Geography & Facilities
CREATE TABLE IF NOT EXISTS countries
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    code
    VARCHAR
(
    10
) NOT NULL UNIQUE,
    name VARCHAR
(
    100
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS addresses
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    street_line1
    VARCHAR
(
    150
) NOT NULL,
    street_line2 VARCHAR
(
    150
),
    city VARCHAR
(
    100
) NOT NULL,
    state VARCHAR
(
    100
),
    postal_code VARCHAR
(
    20
),
    country_id BIGINT NOT NULL,
    FOREIGN KEY
(
    country_id
) REFERENCES countries
(
    id
)
    );

CREATE TABLE IF NOT EXISTS warehouses
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    100
) NOT NULL,
    address_id BIGINT NOT NULL,
    max_capacity INT NOT NULL,
    current_capacity INT NOT NULL DEFAULT 0,
    FOREIGN KEY
(
    address_id
) REFERENCES addresses
(
    id
)
    );

-- 4. Customers & Vendors
CREATE TABLE IF NOT EXISTS customers
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    user_id
    BIGINT
    NOT
    NULL
    UNIQUE,
    first_name
    VARCHAR
(
    50
) NOT NULL,
    last_name VARCHAR
(
    50
) NOT NULL,
    phone VARCHAR
(
    30
),
    address_id BIGINT,
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
),
    FOREIGN KEY
(
    address_id
) REFERENCES addresses
(
    id
)
    );

CREATE TABLE IF NOT EXISTS vendors
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    user_id
    BIGINT,
    company_name
    VARCHAR
(
    100
) NOT NULL,
    tax_identification_number VARCHAR
(
    50
) NOT NULL UNIQUE,
    status VARCHAR
(
    30
) NOT NULL DEFAULT 'ACTIVE',
    address_id BIGINT,
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
),
    FOREIGN KEY
(
    address_id
) REFERENCES addresses
(
    id
)
    );

-- 5. Product Catalog
CREATE TABLE IF NOT EXISTS categories
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    100
) NOT NULL,
    parent_category_id BIGINT,
    FOREIGN KEY
(
    parent_category_id
) REFERENCES categories
(
    id
)
    );

CREATE TABLE IF NOT EXISTS brands
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    100
) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS products
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    sku
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    name VARCHAR
(
    150
) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL,
    category_id BIGINT,
    brand_id BIGINT,
    vendor_id BIGINT NOT NULL,
    status VARCHAR
(
    30
) NOT NULL DEFAULT 'ACTIVE',
    hs_code VARCHAR
(
    30
) NOT NULL,
    FOREIGN KEY
(
    category_id
) REFERENCES categories
(
    id
),
    FOREIGN KEY
(
    brand_id
) REFERENCES brands
(
    id
),
    FOREIGN KEY
(
    vendor_id
) REFERENCES vendors
(
    id
)
    );

CREATE TABLE IF NOT EXISTS product_images
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    product_id
    BIGINT
    NOT
    NULL,
    image_url
    VARCHAR
(
    255
) NOT NULL,
    FOREIGN KEY
(
    product_id
) REFERENCES products
(
    id
) ON DELETE CASCADE
    );

-- 6. Inventory Control
CREATE TABLE IF NOT EXISTS inventories
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    warehouse_id
    BIGINT
    NOT
    NULL,
    product_id
    BIGINT
    NOT
    NULL,
    available_qty
    INT
    NOT
    NULL,
    reserved_qty
    INT
    NOT
    NULL
    DEFAULT
    0,
    reorder_threshold
    INT
    NOT
    NULL
    DEFAULT
    100,
    FOREIGN
    KEY
(
    warehouse_id
) REFERENCES warehouses
(
    id
),
    FOREIGN KEY
(
    product_id
) REFERENCES products
(
    id
)
    );

CREATE TABLE IF NOT EXISTS inventory_transactions
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    inventory_id
    BIGINT
    NOT
    NULL,
    transaction_type
    VARCHAR
(
    30
) NOT NULL,
    quantity_changed INT NOT NULL,
    performed_by VARCHAR
(
    50
) NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    inventory_id
) REFERENCES inventories
(
    id
)
    );

CREATE TABLE IF NOT EXISTS replenishment_requests
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    inventory_id
    BIGINT
    NOT
    NULL,
    requested_qty
    INT
    NOT
    NULL,
    status
    VARCHAR
(
    30
) NOT NULL DEFAULT 'REQUESTED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    inventory_id
) REFERENCES inventories
(
    id
)
    );

-- 7. Orders & Payments
CREATE TABLE IF NOT EXISTS orders
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    order_number
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    status VARCHAR
(
    30
) NOT NULL DEFAULT 'PENDING',
    total_amount DOUBLE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    customer_id
) REFERENCES customers
(
    id
)
    );

CREATE TABLE IF NOT EXISTS order_items
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    order_id
    BIGINT
    NOT
    NULL,
    product_id
    BIGINT
    NOT
    NULL,
    quantity
    INT
    NOT
    NULL,
    unit_price
    DOUBLE
    NOT
    NULL,
    FOREIGN
    KEY
(
    order_id
) REFERENCES orders
(
    id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    product_id
) REFERENCES products
(
    id
)
    );

CREATE TABLE IF NOT EXISTS payments
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    order_id
    BIGINT
    NOT
    NULL
    UNIQUE,
    transaction_reference
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    payment_method VARCHAR
(
    30
) NOT NULL,
    payment_status VARCHAR
(
    30
) NOT NULL DEFAULT 'PENDING',
    amount DOUBLE NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    order_id
) REFERENCES orders
(
    id
)
    );

-- 8. Shipping, Carriers & Customs
CREATE TABLE IF NOT EXISTS carriers
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    100
) NOT NULL,
    carrier_type VARCHAR
(
    30
) NOT NULL,
    api_endpoint VARCHAR
(
    255
)
    );

CREATE TABLE IF NOT EXISTS shipments
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    tracking_number
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    carrier_id BIGINT,
    shipment_type VARCHAR
(
    30
) NOT NULL,
    status VARCHAR
(
    30
) NOT NULL DEFAULT 'PREPARING',
    origin_warehouse_id BIGINT NOT NULL,
    destination_address_id BIGINT NOT NULL,
    estimated_delivery DATETIME,
    actual_delivery DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    order_id
) REFERENCES orders
(
    id
),
    FOREIGN KEY
(
    carrier_id
) REFERENCES carriers
(
    id
),
    FOREIGN KEY
(
    origin_warehouse_id
) REFERENCES warehouses
(
    id
),
    FOREIGN KEY
(
    destination_address_id
) REFERENCES addresses
(
    id
)
    );

CREATE TABLE IF NOT EXISTS shipment_items
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    shipment_id
    BIGINT
    NOT
    NULL,
    order_item_id
    BIGINT
    NOT
    NULL,
    quantity
    INT
    NOT
    NULL,
    FOREIGN
    KEY
(
    shipment_id
) REFERENCES shipments
(
    id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    order_item_id
) REFERENCES order_items
(
    id
)
    );

CREATE TABLE IF NOT EXISTS tracking_events
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    shipment_id
    BIGINT
    NOT
    NULL,
    location
    VARCHAR
(
    100
) NOT NULL,
    description VARCHAR
(
    255
) NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    shipment_id
) REFERENCES shipments
(
    id
) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS customs_documents
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    shipment_id
    BIGINT
    NOT
    NULL,
    document_type
    VARCHAR
(
    100
) NOT NULL,
    hs_code VARCHAR
(
    30
) NOT NULL,
    status VARCHAR
(
    30
) NOT NULL DEFAULT 'SUBMITTED',
    inspected_by VARCHAR
(
    50
),
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    shipment_id
) REFERENCES shipments
(
    id
)
    );

-- 9. Ratings, Notifications & Audits
CREATE TABLE IF NOT EXISTS vendor_performances
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    vendor_id
    BIGINT
    NOT
    NULL,
    fulfillment_score
    DOUBLE
    NOT
    NULL,
    on_time_delivery_rate
    DOUBLE
    NOT
    NULL,
    quality_rating
    DOUBLE
    NOT
    NULL,
    evaluated_at
    DATETIME
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP,
    FOREIGN
    KEY
(
    vendor_id
) REFERENCES vendors
(
    id
)
    );

CREATE TABLE IF NOT EXISTS notifications
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    user_id
    BIGINT
    NOT
    NULL,
    title
    VARCHAR
(
    150
) NOT NULL,
    message TEXT NOT NULL,
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
)
    );

CREATE TABLE IF NOT EXISTS audit_logs
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    username
    VARCHAR
(
    50
) NOT NULL,
    user_role VARCHAR
(
    30
),
    action VARCHAR
(
    100
) NOT NULL,
    target_method VARCHAR
(
    150
) NOT NULL,
    ip_address VARCHAR
(
    45
),
    duration_ms BIGINT,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
