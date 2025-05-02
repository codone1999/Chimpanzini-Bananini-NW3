-- Database: itbms_db

-- Create Database Statement (only if the database doesn't exist)
CREATE DATABASE IF NOT EXISTS itbms_db;

-- Use the Database
USE itbms_db;

-- DROP TABLE IF EXISTS sale_item;
-- DROP TABLE IF EXISTS brand;

-- Table: brand
CREATE TABLE IF NOT EXISTS brand (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    websiteUrl VARCHAR(40),
    isActive BOOLEAN,
    countryOfOrigin VARCHAR(80),
    createdOn DATETIME NOT NULL,
    updatedOn DATETIME NOT NULL,
    CONSTRAINT ck_brand_name CHECK (TRIM(name) <> '')
);

-- Table: sale_item
CREATE TABLE IF NOT EXISTS sale_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    brand_id INT NOT NULL,
    model VARCHAR(60) NOT NULL,
    description TEXT CHARACTER SET utf8mb4 NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price INTEGER NOT NULL,
    screenSizeInch DECIMAL(3, 1),
    ramGb INTEGER,
    storageGb INTEGER,
    color VARCHAR(255),
    createdOn DATETIME NOT NULL,
    updatedOn DATETIME NOT NULL,
    CONSTRAINT fk_sale_item_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
    CONSTRAINT ck_sale_item_model CHECK (TRIM(model) <> ''),
    CONSTRAINT ck_sale_item_description CHECK (TRIM(description) <> ''),
    CONSTRAINT ck_sale_item_quantity CHECK (quantity >= 0),
    CONSTRAINT ck_sale_item_price CHECK (price >= 0),
    CONSTRAINT ck_sale_item_color CHECK (color IS NULL OR TRIM(color) <> '')
);

-- Inserting data into the brand table
INSERT INTO brand (id, name, countryOfOrigin, webSiteUrl, isActive, createdOn, updatedOn) VALUES
(1, 'Samsung', 'South Korea', 'https://www.samsung.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(2, 'Apple', 'United States', 'https://www.apple.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(3, 'Xiaomi', 'China', 'https://www.mi.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(4, 'Huawei', 'China', 'https://www.huawei.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(5, 'OnePlus', 'China', 'https://www.oneplus.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(6, 'Sony', 'Japan', 'https://www.sony.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(7, 'LG', 'South Korea', 'https://www.lg.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(8, 'Nokia', 'Finland', 'https://www.nokia.com', 0, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(9, 'Motorola', 'United States', 'https://www.motorola.com', 0, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(10, 'OPPO', 'China', 'https://www.oppo.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(11, 'Vivo', 'China', 'https://www.vivo.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(12, 'ASUS', 'Taiwan', 'https://www.asus.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(13, 'Google', 'United States', 'https://store.google.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(14, 'Realme', 'China', 'https://www.realme.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(15, 'BlackBerry', 'Canada', 'https://www.blackberry.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(16, 'HTC', 'Taiwan', 'https://www.htc.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(17, 'ZTE', 'China', 'https://www.zte.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(18, 'Lenovo', 'China', 'https://www.lenovo.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(19, 'Honor', 'China', 'https://www.hihonor.com', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(20, 'Nothing', 'United Kingdom', 'https://nothing.tech', 1, '2024-04-19 01:00:00', '2024-04-19 01:00:00');

-- Inserting data into the sale_item table
INSERT INTO sale_item (id, brand_id, model, description, quantity, price, screenSizeInch, ramGb, storageGb, color, createdOn, updatedOn) VALUES
(1, 2, 'iPhone 14 Pro Max', 'ไอโฟนเรือธงรุ่นล่าสุด มาพร้อม Dynamic Island จอใหญ่สุดในตระกูล กล้องระดับโปร', 5, 42900, 6.7, 6, 512, 'Space Black', '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(2, 2, 'iPhone 14', 'ไอโฟนรุ่นใหม่ล่าสุด รองรับ 5G เร็วแรง ถ่ายภาพสวยทุกสภาพแสง', 8, 29700, 6.1, 6, 256, 'Midnight', '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(3, 2, 'iPhone 13 Pro', 'ไอโฟนรุ่นโปร จอ ProMotion 120Hz กล้องระดับมืออาชีพ', 3, 33000, 6.1, 6, 256, 'Sierra Blue', '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(7, 2, 'iPhone SE 2022', 'Budget-friendly model', 15, 14190, 4.7, 4, 64, 'Starlight', '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(8, 2, 'iPhone 14 Plus', 'iPhone 14 Plus 128GB สี Starlight เครื่องศูนย์ไทย โมเดล TH แบต 100% มีกล่องครบ ประกันศูนย์ถึง พ.ย. 68 ส่งฟรี', 7, 29700, 6.7, 6, 256, 'Blue', '2024-04-19 01:00:00', '2024-04-19 01:00:00'),
(16, 1, 'Galaxy S23 Ultra', 'Samsung Galaxy S23 Ultra 512GB สีดาปีศาจ สภาพนางฟ้า 99% ไร้รอย แถมเคสแท้ แบตอึดสุดๆ รองรับปากกา S-Pen', 1, 32900, 7.6, NULL, 512, NULL, '2024-04-19 01:00:00', '2024-04-19 01:00:00');