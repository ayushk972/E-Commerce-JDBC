-- E-Commerce JDBC Database Setup
-- Run this script ONCE in MySQL to create all tables.

CREATE DATABASE IF NOT EXISTS e_com;
USE e_com;

-- --------------------------------------------------------
-- Products table
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    product_id   INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category     VARCHAR(50)  NOT NULL,
    price        DOUBLE       NOT NULL,
    quantity     INT          NOT NULL,
    brand        VARCHAR(50)  NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- --------------------------------------------------------
-- Customers table (includes password & timestamps)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    mobile      CHAR(10)     NOT NULL,
    address     VARCHAR(255) NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- --------------------------------------------------------
-- Orders table
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    order_id     VARCHAR(20) PRIMARY KEY,
    customer_id  INT         NOT NULL,
    total_amount DOUBLE      NOT NULL,
    order_status VARCHAR(20) NOT NULL DEFAULT 'PLACED',
    order_date   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- --------------------------------------------------------
-- Order Items table
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    order_id     VARCHAR(20)  NOT NULL,
    product_id   INT          NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    price        DOUBLE       NOT NULL,
    quantity     INT          NOT NULL,
    total_price  DOUBLE       NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id)   REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- --------------------------------------------------------
-- Cart table (persistent cart per customer)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS cart (
    cart_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id  INT NOT NULL,
    quantity    INT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_cart_item (customer_id, product_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (product_id)  REFERENCES products(product_id)
);

-- --------------------------------------------------------
-- Sample product data
-- --------------------------------------------------------
INSERT IGNORE INTO products (product_id, product_name, category, price, quantity, brand) VALUES
(1,  'iPhone 16',          'Electronics', 999.99,  10, 'Apple'),
(2,  'Samsung Galaxy S24', 'Electronics', 849.99,   8, 'Samsung'),
(3,  'MacBook Air M3',     'Laptops',    1299.99,   5, 'Apple'),
(4,  'Dell XPS 15',        'Laptops',    1199.99,   6, 'Dell'),
(5,  'Nike Air Max',       'Footwear',    129.99,  20, 'Nike'),
(6,  'Adidas Ultraboost',  'Footwear',    159.99,  15, 'Adidas'),
(7,  'Levi''s Jeans',      'Clothing',     69.99,  30, 'Levi''s'),
(8,  'Sony WH-1000XM5',   'Electronics', 349.99,  12, 'Sony');
