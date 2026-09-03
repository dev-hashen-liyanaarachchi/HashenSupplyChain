-- ============================================================================
-- GLOBALTRADE LOGISTICS CORPORATION PLATFORM
-- DATABASE INDEX OPTIMIZATION SCRIPT
-- ============================================================================

USE globaltrade_db;

-- 1. Refresh Token Indexing
CREATE UNIQUE INDEX idx_token_unique ON refresh_token(token);
CREATE INDEX idx_token_username ON refresh_token(username);

-- 2. Product Search Indexing
CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_vendor ON products(vendor_id);
CREATE INDEX idx_product_hs_code ON products(hs_code);

-- 3. Order & Shipment Indexing
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_customer ON orders(customer_id);
CREATE INDEX idx_shipment_tracking ON shipments(tracking_number);
CREATE INDEX idx_shipment_status ON shipments(status);

-- 4. Customs Clearance Indexing
CREATE INDEX idx_customs_shipment ON customs_documents(shipment_id);
CREATE INDEX idx_customs_hs_code ON customs_documents(hs_code);

-- 5. Audit Logging Indexing
CREATE INDEX idx_audit_username ON audit_logs(username);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
