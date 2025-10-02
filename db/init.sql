SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET collation_connection = 'utf8mb4_unicode_ci';

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
    createdOn DATETIME NOT NULL default current_timestamp,
    updatedOn DATETIME NOT NULL default current_timestamp on update current_timestamp,

    CONSTRAINT ck_brand_name CHECK (TRIM(name) <> '')
);

-- Table: sale_item
CREATE TABLE IF NOT EXISTS sale_item (
    id INT PRIMARY KEY,
    brand_id INT NOT NULL,
    model VARCHAR(255) NOT NULL,
    description TEXT CHARACTER SET utf8mb4 NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price INTEGER NOT NULL,
    screenSizeInch DECIMAL(3, 1),
    ramGb INTEGER,
    storageGb INTEGER,
    color VARCHAR(255),
    createdOn DATETIME NOT NULL default current_timestamp,
    updatedOn DATETIME NOT NULL default current_timestamp on update current_timestamp,

    CONSTRAINT fk_sale_item_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
    CONSTRAINT ck_sale_item_model CHECK (TRIM(model) <> ''),
    CONSTRAINT ck_sale_item_description CHECK (TRIM(description) <> ''),
    CONSTRAINT ck_sale_item_quantity CHECK (quantity >= 0),
    CONSTRAINT ck_sale_item_price CHECK (price >= 0),
    CONSTRAINT ck_sale_item_color CHECK (color IS NULL OR TRIM(color) <> '')
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE sale_item
MODIFY COLUMN id int auto_increment;

CREATE TABLE IF NOT EXISTS sale_item_picture (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_item_id INT NOT NULL,
    old_picture_name VARCHAR(255) NOT NULL, -- original uploaded filename
    new_picture_name VARCHAR(255) NOT NULL, -- unique stored filename
    file_size_bytes INT NOT NULL,           -- file size in bytes
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sale_item_picture_item FOREIGN KEY (sale_item_id) REFERENCES sale_item(id)
        ON DELETE CASCADE,
    CONSTRAINT ck_file_size_bytes CHECK (file_size_bytes <= 2 * 1024 * 1024), -- ≤ 2MB
    CONSTRAINT ck_old_picture_name CHECK (TRIM(old_picture_name) <> ''),
    CONSTRAINT ck_new_picture_name CHECK (TRIM(new_picture_name) <> '')
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fullname VARCHAR(100) NOT NULL,
    role ENUM('CUSTOMER','SELLER') NOT NULL,
    createdOn DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE seller (
    account_id INT PRIMARY KEY,
    mobile VARCHAR(20),
    bank_account_no VARCHAR(50),
    bank_name VARCHAR(100),
    national_card_no VARCHAR(50),
    national_card_photo VARCHAR(255), -- store path to uploaded image
    CONSTRAINT fk_seller_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
);
ALTER TABLE sale_item
ADD COLUMN seller_id INT,
ADD CONSTRAINT fk_sale_item_seller FOREIGN KEY (seller_id) REFERENCES seller(account_id);


-- Insert data into the 'brand' table with specific createdOn timestamps
-- This will influence the order when sorted by brand creation time.
INSERT INTO brand (name, websiteUrl, isActive, countryOfOrigin, createdOn) VALUES
('Samsung', 'https://www.samsung.com', 1, 'South Korea', '2023-01-01 10:00:00'), -- Will be id 1
('Apple', 'https://www.apple.com', 1, 'United States', '2023-01-01 10:01:00'), -- Will be id 2
('Xiaomi', 'https://www.mi.com', 1, 'China', '2023-01-01 10:02:00'), -- Will be id 3
('Huawei', 'https://www.huawei.com', 1, 'China', '2023-01-01 10:03:00'), -- Will be id 4
('ASUS', 'https://www.asus.com', 1, 'Taiwan', '2023-01-01 10:04:00'), -- Will be id 5
('OPPO', 'https://www.oppo.com', 1, 'China', '2023-01-01 10:05:00'), -- Will be id 6
('OnePlus', 'https://www.oneplus.com', 1, 'China', '2023-01-01 10:06:00'),
('Sony', 'https://www.sony.com', 1, 'Japan', '2023-01-01 10:07:00'),
('LG', 'https://www.lg.com', 1, 'South Korea', '2023-01-01 10:08:00'),
('Nokia', 'https://www.nokia.com', 0, 'Finland', '2023-01-01 10:09:00'),
('Motorola', 'https://www.motorola.com', 0, 'United States', '2023-01-01 10:10:00'),
('Vivo', 'https://www.vivo.com', 1, 'China', '2023-01-01 10:11:00'),
('Google', 'https://store.google.com', 1, 'United States', '2023-01-01 10:12:00'),
('Realme', 'https://www.realme.com', 1, 'China', '2023-01-01 10:13:00'),
('BlackBerry', 'https://www.blackberry.com', 1, 'Canada', '2023-01-01 10:14:00'),
('HTC', 'https://www.htc.com', 1, 'Taiwan', '2023-01-01 10:15:00'),
('ZTE', 'https://www.zte.com', 1, 'China', '2023-01-01 10:16:00'),
('Lenovo', 'https://www.lenovo.com', 1, 'China', '2023-01-01 10:17:00'),
('Honor', 'https://www.hihonor.com', 1, 'China', '2023-01-01 10:18:00'),
('Nothing', 'https://nothing.tech', 1, 'United Kingdom', '2023-01-01 10:19:00');

-- Demonstrate brand order by creation time (Apple -> Samsung -> Xiaomi -> Huawei -> ASUS -> OPPO)
-- Note: This assumes the brand IDs match the insertion order, which AUTO_INCREMENT typically ensures.
SELECT id, name, createdOn FROM brand ORDER BY createdOn ASC;

-- Demonstrate brand order by name Ascending (Apple -> ASUS -> Huawei -> OPPO -> Samsung -> Xiaomi)
SELECT id, name FROM brand ORDER BY name ASC;
ALTER TABLE sale_item_picture ADD COLUMN display_order INT NOT NULL DEFAULT 1;
ALTER TABLE sale_item_picture
MODIFY createdOn TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- INSERT INTO account (nickname, email, password, fullname, role)
-- VALUES ('default_seller', 'seller@example.com', 'hashedpassword', 'System Seller', 'SELLER');
-- INSERT INTO seller (account_id, mobile, bank_account_no, bank_name, national_card_no, national_card_photo)
-- VALUES (LAST_INSERT_ID(), '0812345678', '123-456-7890', 'Bangkok Bank', '1234567890123', 'default.jpg');

-- INSERT INTO account (nickname, email, password, fullname, role)
-- VALUES ('nig_seller', 'seller1@example.com', 'hashedpassword', 'System Seller', 'SELLER');
-- INSERT INTO seller (account_id, mobile, bank_account_no, bank_name, national_card_no, national_card_photo)
-- VALUES (LAST_INSERT_ID(), '0812345678', '123-456-7890', 'Bangkok Bank', '1234567890123', 'default.jpg');

-- INSERT INTO account (nickname, email, password, fullname, role)
-- VALUES ('help_seller', 'seller2@example.com', 'hashedpassword', 'System Seller', 'SELLER');
-- INSERT INTO seller (account_id, mobile, bank_account_no, bank_name, national_card_no, national_card_photo)
-- VALUES (LAST_INSERT_ID(), '0812345678', '123-456-7890', 'Bangkok Bank', '1234567890123', 'default.jpg');

-- INSERT INTO account (nickname, email, password, fullname, role)
-- VALUES ('test_seller', 'seller3@example.com', 'hashedpassword', 'System Seller', 'SELLER');
-- INSERT INTO seller (account_id, mobile, bank_account_no, bank_name, national_card_no, national_card_photo)
-- VALUES (LAST_INSERT_ID(), '0812345678', '123-456-7890', 'Bangkok Bank', '1234567890123', 'default.jpg');
ALTER TABLE account
ADD COLUMN status ENUM('INACTIVE','ACTIVE') NOT NULL DEFAULT 'INACTIVE';
-- Create EmailVerificationToken table
CREATE TABLE IF NOT EXISTS email_verification_token (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    CONSTRAINT fk_token_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- Add new columns
ALTER TABLE seller ADD COLUMN national_card_photo_front VARCHAR(255);
ALTER TABLE seller ADD COLUMN national_card_photo_back VARCHAR(255);

-- If you want to remove the old column
ALTER TABLE seller DROP COLUMN national_card_photo;
-- Insert data into the 'sale_item' table
-- IDs are now auto-incremented, so we don't provide them.
-- Use brand_id based on the order brands were inserted (e.g., Apple is 1, Samsung is 2).
-- Set specific createdOn dates for items to test item-level creation time sorting.
INSERT INTO sale_item (
   id, brand_id, model, description, price, ramGb, screenSizeInch, storageGb, color, quantity, createdOn
) VALUES

-- Samsung Sale Items (brand_id: 1)
(16, 1, 'Galaxy S23 Ultra', 'Samsung Galaxy S23 Ultra 512GB สีดำปีศาจ สภาพนางฟ้า 99% ไร้รอย แถมเคสแท้ แบตอึดสุดๆ รองรับปากกา S-Pen อุปกรณ์ครบกล่อง ประกันศูนย์เหลือ 6 เดือน ส่งฟรี', 39600, NULL, 6.8, 512, NULL, 6, '2023-03-02 10:00:00'), -- Samsung item 1 (earliest)
(17, 1, 'Galaxy S23+', 'Premium flagship model', 33000, 8, 6.6, 256, 'Cream', 8, '2023-03-02 10:01:00'), -- Samsung item 2
(18, 1, 'Galaxy Z Fold4', 'สมาร์ทโฟนพับได้สุดล้ำ จอใหญ่เท่าแท็บเล็ต ทำงานได้หลากหลาย', 59400, 12, 7.6, 256, 'Phantom Green', 3, '2023-03-02 10:02:00'),
(19, 1, 'Galaxy Z Flip4', 'Compact foldable', 33000, 8, 6.7, 128, 'Bora Purple', 5, '2023-03-02 10:03:00'),
(20, 1, 'Galaxy A53 5G', 'มือถือ 5G สเปคดี กล้องเทพ แบตอึด คุ้มค่าน่าใช้', 14850, 6, 6.5, 128, 'Awesome Blue', 12, '2023-03-02 10:04:00'),
(21, 1, 'Galaxy A33 5G', 'Budget 5G phone', 11550, 6, 6.4, 128, 'Awesome White', 15, '2023-03-02 10:05:00'),
(22, 1, 'Galaxy S22', 'เรือธงตัวท็อปจาก Samsung พร้อม S Pen ในตัว กล้อง 200MP ซูมไกลสุด 100x', 26400, 8, 6.1, 128, 'Pink Gold', 7, '2023-03-02 10:06:00'),
(23, 1, 'Galaxy M53', 'Mid-range performance', 14850, 6, 6.7, 128, 'Green', 9, '2023-03-02 10:07:00'),
(24, 1, 'Galaxy A73 5G', 'Premium mid-range', 16500, 8, 6.7, 256, 'Gray', 6, '2023-03-02 10:08:00'),
(25, 1, 'Galaxy S21 FE', 'Fan Edition model', 19800, 6, 6.4, 128, 'Olive', 8, '2023-03-02 10:09:00'),

-- Apple Sale Items (brand_id: 2)
(1, 2, 'iPhone 14 Pro Max', 'ไอโฟนเรือธงรุ่นล่าสุด มาพร้อม Dynamic Island จอใหญ่สุดในตระกูล กล้องระดับโปร', 42900, 6, 6.7, 512, 'Space Black', 5, '2023-03-01 10:00:00'), -- Apple item 1 (earliest)
(2, 2, 'iPhone 14', 'ไอโฟนรุ่นใหม่ล่าสุด รองรับ 5G เร็วแรง ถ่ายภาพสวยทุกสภาพแสง', 29700, 6, 6.1, 256, 'Midnight', 8, '2023-03-01 10:01:00'), -- Apple item 2
(3, 2, 'iPhone 13 Pro', 'ไอโฟนรุ่นโปร จอ ProMotion 120Hz กล้องระดับมืออาชีพ', 33000, 6, 6.1, 256, 'Sierra Blue', 3, '2023-03-01 10:02:00'),
(4, 2, 'iPhone 13', 'Previous gen base model', 23100, 4, 6.1, 128, 'Pink', 10, '2023-03-01 10:03:00'),
(5, 2, 'iPhone 12 Pro Max', '2020 flagship model', 29700, 6, 6.7, 256, 'Pacific Blue', 4, '2023-03-01 10:04:00'),
(6, 2, 'iPhone 12', '2020 base model', 19800, 4, 6.1, 128, 'Purple', 6, '2023-03-01 10:05:00'),
(7, 2, 'iPhone SE 2022', 'Budget-friendly model', 14190, 4, 4.7, 64, 'Starlight', 15, '2023-03-01 10:06:00'),
(8, 2, 'iPhone 14 Plus', 'iPhone 14 Plus 128GB สี Starlight เครื่องศูนย์ไทย โมเดล TH แบต 100% มีกล่องครบ ประกันศูนย์ถึง พ.ย. 68 ส่งฟรี', 29700, 6, 6.7, 256, 'Blue', 7, '2023-03-01 10:07:00'),
(9, 2, 'iPhone 13 mini', 'Compact previous gen', 19800, 4, 5.4, 128, 'Green', 5, '2023-03-01 10:08:00'),
(10, 2, 'iPhone 12 mini', 'Compact 2020 model', 16500, 4, 5.4, 64, 'Red', 4, '2023-03-01 10:09:00'),

-- Xiaomi Sale Items (brand_id: 3)
(31, 3, '13 Pro', 'เรือธงสเปคแรงจาก Xiaomi กล้องไลก้า ชาร์จไว 120W', 33000, 12, 6.73, 256, 'Black', 8, '2023-03-03 10:00:00'),
(32, 3, '13T Pro', 'Xiaomi 13T Pro 12/512GB สี Meadow Green ชิป Dimensity 9200+ เร็วแรง กล้อง Leica ถ่ายรูปสวยขั้นเทพ มีที่ชาร์จ 120W ครบกล่อง จัดส่งฟรีทั่วประเทศ', 23100, 12, NULL, NULL, 'Alpine Blue', 6, '2023-03-03 10:01:00'),
(33, 3, 'POCO F5', 'มือถือสเปคเทพ เน้นเล่นเกม จอ 120Hz ราคาคุ้มค่า', 13200, 8, 6.67, 256, 'Carbon Black', 10, '2023-03-03 10:02:00'),
(34, 3, 'Redmi Note 12 Pro', 'กล้องคมชัด 108MP แบตอึด ชาร์จเร็ว ราคาโดนใจ', 9900, 8, 6.67, 128, 'Sky Blue', 15, '2023-03-03 10:03:00'),
(35, 3, '12T Pro', 'Previous flagship', 21450, 8, 6.67, 256, 'Cosmic Black', 5, '2023-03-03 10:04:00'),
(36, 3, 'POCO X5 Pro', 'Mid-range performer', 9900, 8, 6.67, 128, 'Yellow', 12, '2023-03-03 10:05:00'),
(37, 3, 'Redmi 12C', 'Budget friendly', 5940, 4, 6.71, 64, 'Ocean Blue', 20, '2023-03-03 10:06:00'),
(38, 3, '12 Lite', 'Slim mid-range', 13200, 8, 6.55, 128, 'Lite Pink', 8, '2023-03-03 10:07:00'),
(39, 3, 'POCO M5', 'Budget gaming', 7590, 6, 6.58, 128, 'Power Black', 14, '2023-03-03 10:08:00'),
(40, 3, 'Redmi Note 11', 'Previous gen mid-range', 8250, 6, 6.43, 128, 'Star Blue', 10, '2023-03-03 10:09:00'),

-- Huawei Sale Items (brand_id: 4)
(46, 4, 'P60 Pro', 'กล้องเรือธงระดับเทพ เซ็นเซอร์ใหญ่พิเศษ ถ่ายภาพกลางคืนสวยเยี่ยม', 36300, 12, 6.67, 256, 'Rococo Pearl', 5, '2023-03-04 10:00:00'),
(47, 4, 'Mate 50 Pro', 'เรือธงตระกูล Mate จอ OLED คมชัด ดีไซน์พรีเมียม', 42900, 8, 6.74, 256, 'Silver Black', 4, '2023-03-04 10:01:00'),
(48, 4, 'nova 11 Pro', 'สมาร์ทโฟนดีไซน์สวย กล้องหน้าคู่ เน้นเซลฟี่ ชาร์จไว', 19800, 8, 6.78, 256, 'Green', 8, '2023-03-04 10:02:00'),
(49, 4, 'P50 Pro', 'Previous flagship', 29700, 8, 6.6, 256, 'Cocoa Gold', 6, '2023-03-04 10:03:00'),
(50, 4, 'nova 10', 'Stylish mid-range', 16500, 8, 6.67, 128, 'Starry Silver', 10, '2023-03-04 10:04:00'),
(51, 4, 'Mate X3', 'Premium foldable', 66000, 12, 7.85, 512, 'Feather Gold', 3, '2023-03-04 10:05:00'),
(52, 4, 'nova 9', 'Previous mid-range', 13200, 8, 6.57, 128, 'Starry Blue', 12, '2023-03-04 10:06:00'),
(53, 4, 'P50 Pocket', 'Foldable fashion', 46200, 8, 6.9, 256, 'Premium Gold', 4, '2023-03-04 10:07:00'),
(54, 4, 'nova Y70', 'Budget friendly', 9900, 4, 6.75, 128, 'Crystal Blue', 15, '2023-03-04 10:08:00'),
(55, 4, 'Mate 40 Pro', 'Classic flagship', 26400, 8, 6.76, 256, 'Mystic Silver', 5, '2023-03-04 10:09:00'),

-- ASUS Sale Items (brand_id: 5)
(61, 5, 'ROG Phone 7', 'สมาร์ทโฟนเกมมิ่งสเปคโหด จอ 165Hz เสียงสเตอริโอคู่ แบตอึด', 33000, 16, 6.78, 512, 'Phantom Black', 4, '2023-03-05 10:00:00'),
(62, 5, 'ROG Phone 6D', 'เกมมิ่งโฟนพลังแรง CPU Dimensity ระบายความร้อนเยี่ยม', 29700, 16, 6.78, 256, 'Space Gray', 5, '2023-03-05 10:01:00'),
(63, 5, 'Zenfone 9', 'มือถือกะทัดรัด สเปคแรง กล้องกันสั่น ใช้ง่ายมือเดียว', 23100, 8, 5.9, 128, 'Midnight Black', 8, '2023-03-05 10:02:00'),
(64, 5, 'ROG Phone 6', 'Previous gaming flagship', 29700, 12, 6.78, 256, 'Storm White', 6, '2023-03-05 10:03:00'),
(65, 5, 'Zenfone 8', 'Previous compact flagship', 19800, 8, 5.9, 128, 'Obsidian Black', 7, '2023-03-05 10:04:00'),
(66, 5, 'ROG Phone 5s', 'Gaming performance', 26400, 12, 6.78, 256, 'Phantom Black', 5, '2023-03-05 10:05:00'),
(67, 5, 'Zenfone 8 Flip', 'Flip camera flagship', 26400, 8, 6.67, 256, 'Galactic Black', 4, '2023-03-05 10:06:00'),
(68, 5, 'ROG Phone 5', 'Classic gaming phone', 23100, 12, 6.78, 256, 'Storm White', 6, '2023-03-05 10:07:00'),
(69, 5, 'Zenfone 7', 'Flip camera classic', 19800, 8, 6.67, 128, 'Aurora Black', 5, '2023-03-05 10:08:00'),
(70, 5, 'ROG Phone 3', 'Legacy gaming phone', 16500, 12, 6.59, 256, 'Black Glare', 3, '2023-03-05 10:09:00'),

-- OPPO Sale Items (brand_id: 6)
(76, 6, 'Find X6 Pro', 'กล้องเทพระดับมืออาชีพ ชิป Snapdragon 8 Gen 2 ชาร์จไว 100W', 33000, 12, 6.82, 256, 'Cosmos Black', 5, '2023-03-06 10:00:00'),
(77, 6, 'Reno9 Pro+', 'OPPO Reno9 Pro+ 5G 256GB สี Glossy Purple สวยสะดุดตา ใช้งานลื่นสุดๆ แบต 4700 mAh รองรับชาร์จไว ครบกล่อง + ใบเสร็จศูนย์ ส่งฟรี Flash Express', 23100, 12, 6.7, 256, 'Eternal Gold', 8, '2023-03-06 10:01:00'),
(78, 6, 'Find N2 Flip', 'สมาร์ทโฟนพับได้สุดหรู จอนอกใหญ่พิเศษ กล้องคู่คมชัด', 33000, 8, 6.8, 256, 'Astral Black', 4, '2023-03-06 10:02:00'),
(79, 6, 'Reno8 Pro', 'ดีไซน์บางเบา กล้องคมชัด ชาร์จเร็วสุด ระบบเสียงดี', 19800, 8, 6.7, 256, 'Glazed Green', 10, '2023-03-06 10:03:00'),
(80, 6, 'Find X5 Pro', 'Previous flagship', 29700, 12, 6.7, 256, 'Ceramic White', 6, '2023-03-06 10:04:00'),
(81, 6, 'A78', 'Mid-range performer', 9900, 8, 6.56, 128, 'Glowing Black', 15, '2023-03-06 10:05:00'),
(82, 6, 'Reno7', 'Style focused mid-range', 13200, 8, 6.43, 128, 'Startrails Blue', 12, '2023-03-06 10:06:00'),
(83, 6, 'Find X5 Lite', 'Previous gen lite', 14850, 8, 6.43, 128, 'Starry Black', 8, '2023-03-06 10:07:00'),
(84, 6, 'A77', 'Budget friendly', 8250, 6, 6.56, 128, 'Ocean Blue', 20, '2023-03-06 10:08:00'),
(85, 6, 'Reno6 Pro', 'Classic premium', 16500, 12, 6.55, 256, 'Arctic Blue', 7, '2023-03-06 10:09:00');

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    status ENUM('PENDING','PAID','SHIPPED','CANCELLED') DEFAULT 'PENDING',
	orderNote VARCHAR(255),
    createdOn TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedOn TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES account(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    sale_item_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price_each INT NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_item FOREIGN KEY (sale_item_id) REFERENCES sale_item(id) ON DELETE CASCADE
);

