-- ============================================================================
-- GLOBALTRADE LOGISTICS CORPORATION PLATFORM
-- INITIAL SEED DATA DML SCRIPT
-- ============================================================================

USE
globaltrade_db;

-- Insert Roles
INSERT INTO roles (id, name)
VALUES (1, 'ADMIN'),
       (2, 'CUSTOMER'),
       (3, 'VENDOR'),
       (4, 'WAREHOUSE_MANAGER'),
       (5, 'LOGISTICS_OFFICER'),
       (6, 'CUSTOMS_OFFICER'),
       (7, 'FINANCE_OFFICER');

-- Insert Sample Users (Password: admin123 / vendor123)
INSERT INTO users (id, username, password_hash, email, role_id, active)
VALUES (1, 'admin', 'admin123', 'admin@globaltrade.com', 1, TRUE),
       (2, 'customer_user', 'admin123', 'customer@globaltrade.com', 2, TRUE),
       (3, 'vendor_bosch', 'vendor123', 'sales@bosch.de', 3, TRUE),
       (4, 'warehouse_mgr', 'admin123', 'warehouse@globaltrade.com', 4, TRUE),
       (5, 'logistics_officer', 'admin123', 'logistics@globaltrade.com', 5, TRUE),
       (6, 'customs_officer', 'admin123', 'customs@globaltrade.com', 6, TRUE),
       (7, 'finance_officer', 'admin123', 'finance@globaltrade.com', 7, TRUE);

-- Insert Countries
INSERT INTO countries (id, code, name)
VALUES (1, 'DE', 'Germany'),
       (2, 'LK', 'Sri Lanka'),
       (3, 'US', 'United States'),
       (4, 'SG', 'Singapore');

-- Insert Sample Addresses
INSERT INTO addresses (id, street_line1, city, state, postal_code, country_id)
VALUES (1, 'Bosch-Allee 1', 'Stuttgart', 'Baden-Württemberg', '70839', 1),
       (2, '100 Port Road', 'Colombo', 'Western', '00100', 2);

-- Insert Sample Warehouses
INSERT INTO warehouses (id, name, address_id, max_capacity, current_capacity)
VALUES (1, 'Hamburg Export Hub', 1, 50000, 12000),
       (2, 'Colombo Logistics Depot', 2, 30000, 8000);

-- Insert Vendors
INSERT INTO vendors (id, user_id, company_name, tax_identification_number, status, address_id)
VALUES (1, 3, 'Bosch Logistics GmbH', 'DE-TAX-998877', 'ACTIVE', 1);

-- Insert HS Codes Master Table
INSERT INTO hs_codes (id, code, description_type, duty_rate)
VALUES (1, '9018.90', 'Medical & Surgical Diagnostic Instruments', 5.0),
       (2, '8504.40', 'Electrical Power Transformers & Converters', 7.5),
       (3, '8471.30', 'Portable Laptops & Automatic Data Processors', 0.0),
       (4, '3004.90', 'Pharmaceutical Medicaments & Vaccines', 0.0),
       (5, '8703.23', 'Motor Vehicles & Logistics Fleet Transports', 15.0);

-- Insert Categories & Brands
INSERT INTO categories (id, name)
VALUES (1, 'Medical Equipment');
INSERT INTO brands (id, name)
VALUES (1, 'Bosch Healthcare');

-- Insert Sample Products (Master Products without Vendor Column)
INSERT INTO products (id, sku, name, description, price, category_id, brand_id, status, hs_code)
VALUES (1, 'MED-9018-X', 'Precision Ultrasound Probe', 'High frequency diagnostic transducer unit', 4500.00, 1, 1,
        'ACTIVE', '9018.90'),
       (2, 'ELC-8504-A', 'Industrial High-Capacity Transformer', 'Heavy duty power converter module', 1250.00, 1, 1,
        'ACTIVE', '8504.40');

-- Insert Initial Inventories (Stock Allocation linked to Supplying Vendor & Unit Price)
INSERT INTO inventories (id, warehouse_id, product_id, vendor_id, unit_price, available_qty, reserved_qty,
                         reorder_threshold)
VALUES (1, 1, 1, 1, 4500.00, 1000, 50, 100),
       (2, 2, 2, 1, 1250.00, 500, 20, 50);
