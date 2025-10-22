-- =========================================================
-- 🚀 RESET DATABASE DỮ LIỆU DEMO
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM product_image;
DELETE FROM post_product;
DELETE FROM wallet;
DELETE FROM seller;
DELETE FROM buyer;
DELETE FROM admin;
DELETE FROM subscription_packages;
DELETE FROM package_price;
DELETE FROM category;
DELETE FROM system_policy;
DELETE FROM subscription;
DELETE FROM shipping_partner;
DELETE FROM dispute_category;
DELETE FROM orders;
DELETE FROM payment;

ALTER TABLE product_image AUTO_INCREMENT = 1;
ALTER TABLE post_product AUTO_INCREMENT = 1;
ALTER TABLE wallet AUTO_INCREMENT = 1;
ALTER TABLE seller AUTO_INCREMENT = 1;
ALTER TABLE buyer AUTO_INCREMENT = 1;
ALTER TABLE admin AUTO_INCREMENT = 1;
ALTER TABLE subscription_packages AUTO_INCREMENT = 1;
ALTER TABLE package_price AUTO_INCREMENT = 1;
ALTER TABLE category AUTO_INCREMENT = 1;
ALTER TABLE orders AUTO_INCREMENT = 1;
ALTER TABLE shipping_partner AUTO_INCREMENT = 1;
ALTER TABLE dispute_category AUTO_INCREMENT = 1;
ALTER TABLE subscription AUTO_INCREMENT = 1;
ALTER TABLE payment AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 📦 INSERT GÓI ĐĂNG KÝ
-- =========================================================
INSERT INTO subscription_packages (
    subscription_package_id,
    package_name,
    description,
    is_active,
    max_product,
    max_img_per_post,
    created_at,
    updated_at
)
VALUES
(1, 'Basic Plan', 'Gói cơ bản cho người mới bắt đầu', TRUE, 10, 3, NOW(), NOW()),
(2, 'Pro Plan', 'Gói chuyên nghiệp cho doanh nghiệp nhỏ', TRUE, 50, 6, NOW(), NOW()),
(3, 'Premium Plan', 'Gói cao cấp cho doanh nghiệp lớn', TRUE, 200, 10, NOW(), NOW()),
(4, 'Legacy Plan', 'Gói cũ, không còn được hỗ trợ', FALSE, 20, 5, NOW(), NOW())
ON DUPLICATE KEY UPDATE subscription_package_id = subscription_package_id;

-- =========================================================
-- 💰 INSERT GIÁ CÁC GÓI
-- =========================================================
INSERT INTO package_price (
    price,
    is_active,
    duration_by_day,
    currency,
    discount_percent,
    created_at,
    updated_at,
    package_id
)
VALUES
(49000, TRUE, 30, 'VND', 0, NOW(), NOW(), 1),
(129000, TRUE, 90, 'VND', 10, NOW(), NOW(), 1),

(199000, TRUE, 30, 'VND', 0, NOW(), NOW(), 2),
(549000, TRUE, 90, 'VND', 8, NOW(), NOW(), 2),
(999000, TRUE, 180, 'VND', 15, NOW(), NOW(), 2),

(499000, TRUE, 30, 'VND', 0, NOW(), NOW(), 3),
(1299000, TRUE, 90, 'VND', 10, NOW(), NOW(), 3),
(2499000, TRUE, 180, 'VND', 20, NOW(), NOW(), 3),

(99000, FALSE, 30, 'VND', 0, NOW(), NOW(), 4)
ON DUPLICATE KEY UPDATE package_id = package_id;

-- =========================================================
-- 🧑‍💼 ADMIN
-- =========================================================
INSERT INTO admin (
    avatar_url,
    employee_number,
    password,
    full_name,
    phone_number,
    is_super_admin,
    email,
    status,
    gender,
    created_at,
    updated_at
)
VALUES (
    'https://cdn.example.com/avatar/admin1.png',
    '1234567890',
    '{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2', -- password: Vien.123456@
    'Nguyen Van Quan Tri',
    '0901123456',
    TRUE,
    'admin@example.com',
    'ACTIVE',
    'MALE',
    NOW(),
    NOW()
);

-- =========================================================
-- 👤 BUYER
-- =========================================================
-- SỬ DỤNG DEFAULT ACCOUNT DÙM CON NHA MẤY MÁ
-- TẠI SEED DATA NÊN MỖI LẦN CHẠY LẠI LÀ CÁC ACCOUNT CŨ KHI SIGN UP ĐỒ NÀY NỌ LÀ NÓ SẼ BỊ MẤT NHA MẤY MẸ
INSERT INTO buyer (
    username,
    password,
    full_name,
    default_shipping_address,
    ward_name,
    district_name,
    province_name,
    is_active,
    phone_number,
    avatar_url,
    avatar_public_id,
    email,
    gender,
    date_of_birth,
    created_at,
    updated_at,
    deleted_at
)
VALUES
    ('buyerdeptrai','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 1','123 Nguyễn Văn Cừ, Phường 1, Quận 5, Hồ Chí Minh','Phường 1','Quận 5','Hồ Chí Minh',TRUE,'0900000001',NULL,'buyer1_public_id','buyer1@example.com','MALE','2000-01-01',NOW(),NULL,NULL),
    ('buyerxautrai','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 2','124 Nguyễn Văn Cừ, Phường 2, Quận 5, Hồ Chí Minh','Phường 2','Quận 5','Hồ Chí Minh',TRUE,'0900000002',NULL,'buyer2_public_id','buyer2@example.com','FEMALE','2000-02-02',NOW(),NULL,NULL),
    ('truongdoanvien','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 3','125 Nguyễn Văn Cừ, Phường 3, Quận 5, Hồ Chí Minh','Phường 3','Quận 5','Hồ Chí Minh',TRUE,'0900000003',NULL,'buyer3_public_id','buyer3@example.com','MALE','2000-03-03',NOW(),NULL,NULL),
    ('tranthihanh','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 4','126 Nguyễn Văn Cừ, Phường 4, Quận 5, Hồ Chí Minh','Phường 4','Quận 5','Hồ Chí Minh',TRUE,'0900000004',NULL,'buyer4_public_id','buyer4@example.com','FEMALE','2000-04-04',NOW(),NULL,NULL),
    ('phanthithaovy','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 5','127 Nguyễn Văn Cừ, Phường 5, Quận 5, Hồ Chí Minh','Phường 5','Quận 5','Hồ Chí Minh',TRUE,'0900000005',NULL,'buyer5_public_id','buyer5@example.com','MALE','2000-05-05',NOW(),NULL,NULL),
    ('doanthikimthuy','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 6','128 Nguyễn Văn Cừ, Phường 6, Quận 5, Hồ Chí Minh','Phường 6','Quận 5','Hồ Chí Minh',TRUE,'0900000006',NULL,'buyer6_public_id','buyer6@example.com','FEMALE','2000-06-06',NOW(),NULL,NULL),
    ('nguyenphuongduy','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 7','129 Nguyễn Văn Cừ, Phường 7, Quận 5, Hồ Chí Minh','Phường 7','Quận 5','Hồ Chí Minh',TRUE,'0900000007',NULL,'buyer7_public_id','buyer7@example.com','MALE','2000-07-07',NOW(),NULL,NULL),
    ('doanvienne','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 8','130 Nguyễn Văn Cừ, Phường 8, Quận 5, Hồ Chí Minh','Phường 8','Quận 5','Hồ Chí Minh',TRUE,'0900000008',NULL,'buyer8_public_id','buyer8@example.com','FEMALE','2000-08-08',NOW(),NULL,NULL),
    ('truongviendoan','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 9','131 Nguyễn Văn Cừ, Phường 9, Quận 5, Hồ Chí Minh','Phường 9','Quận 5','Hồ Chí Minh',TRUE,'0900000009',NULL,'buyer9_public_id','buyer9@example.com','MALE','2000-09-09',NOW(),NULL,NULL),
    ('anhvienne','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 10','132 Nguyễn Văn Cừ, Phường 10, Quận 5, Hồ Chí Minh','Phường 10','Quận 5','Hồ Chí Minh',TRUE,'0900000010',NULL,'buyer10_public_id','buyer10@example.com','FEMALE','2000-10-10',NOW(),NULL,NULL),
    ('viennehaha','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 11','133 Nguyễn Văn Cừ, Phường 11, Quận 5, Hồ Chí Minh','Phường 11','Quận 5','Hồ Chí Minh',TRUE,'0900000011',NULL,'buyer11_public_id','buyer11@example.com','MALE','2000-11-11',NOW(),NULL,NULL),
    ('homnaytroidepqua','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 12','134 Nguyễn Văn Cừ, Phường 12, Quận 5, Hồ Chí Minh','Phường 12','Quận 5','Hồ Chí Minh',TRUE,'0900000012',NULL,'buyer12_public_id','buyer12@example.com','FEMALE','2000-12-12',NOW(),NULL,NULL),
    ('troiquadepluon','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 13','135 Nguyễn Văn Cừ, Phường 13, Quận 5, Hồ Chí Minh','Phường 13','Quận 5','Hồ Chí Minh',TRUE,'0900000013',NULL,'buyer13_public_id','buyer13@example.com','MALE','2001-01-13',NOW(),NULL,NULL),
    ('anhyeunhieuem','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 14','136 Nguyễn Văn Cừ, Phường 14, Quận 5, Hồ Chí Minh','Phường 14','Quận 5','Hồ Chí Minh',TRUE,'0900000014',NULL,'buyer14_public_id','buyer14@example.com','FEMALE','2001-02-14',NOW(),NULL,NULL),
    ('anhyeuem','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 15','137 Nguyễn Văn Cừ, Phường 15, Quận 5, Hồ Chí Minh','Phường 15','Quận 5','Hồ Chí Minh',TRUE,'0900000015',NULL,'buyer15_public_id','buyer15@example.com','MALE','2001-03-15',NOW(),NULL,NULL),
    ('beiucuaanhoi','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 16','138 Nguyễn Văn Cừ, Phường 16, Quận 5, Hồ Chí Minh','Phường 16','Quận 5','Hồ Chí Minh',TRUE,'0900000016',NULL,'buyer16_public_id','buyer16@example.com','FEMALE','2001-04-16',NOW(),NULL,NULL),
    ('homnayemsaoroi','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 17','139 Nguyễn Văn Cừ, Phường 17, Quận 5, Hồ Chí Minh','Phường 17','Quận 5','Hồ Chí Minh',TRUE,'0900000017',NULL,'buyer17_public_id','buyer17@example.com','MALE','2001-05-17',NOW(),NULL,NULL),
    ('anhoiemdoi','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 18','140 Nguyễn Văn Cừ, Phường 18, Quận 5, Hồ Chí Minh','Phường 18','Quận 5','Hồ Chí Minh',TRUE,'0900000018',NULL,'buyer18_public_id','buyer18@example.com','FEMALE','2001-06-18',NOW(),NULL,NULL),
    ('doiquaemoi','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 19','141 Nguyễn Văn Cừ, Phường 19, Quận 5, Hồ Chí Minh','Phường 19','Quận 5','Hồ Chí Minh',TRUE,'0900000019',NULL,'buyer19_public_id','buyer19@example.com','MALE','2001-07-19',NOW(),NULL,NULL),
    ('minhbuocquadoinhau','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 20','142 Nguyễn Văn Cừ, Phường 20, Quận 5, Hồ Chí Minh','Phường 20','Quận 5','Hồ Chí Minh',TRUE,'0900000020',NULL,'buyer20_public_id','buyer20@example.com','FEMALE','2001-08-20',NOW(),NULL,NULL),
    ('delamnhaudau','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 21','143 Nguyễn Văn Cừ, Phường 21, Quận 5, Hồ Chí Minh','Phường 21','Quận 5','Hồ Chí Minh',TRUE,'0900000021',NULL,'buyer21_public_id','buyer21@example.com','MALE','2001-09-21',NOW(),NULL,NULL),
    ('homnayminhdidauvaycobe','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 22','144 Nguyễn Văn Cừ, Phường 22, Quận 5, Hồ Chí Minh','Phường 22','Quận 5','Hồ Chí Minh',TRUE,'0900000022',NULL,'buyer22_public_id','buyer22@example.com','FEMALE','2001-10-22',NOW(),NULL,NULL),
    ('sapinsertxongroi','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 23','145 Nguyễn Văn Cừ, Phường 23, Quận 5, Hồ Chí Minh','Phường 23','Quận 5','Hồ Chí Minh',TRUE,'0900000023',NULL,'buyer23_public_id','buyer23@example.com','MALE','2001-11-23',NOW(),NULL,NULL),
    ('toiuserhaimuoitu','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 24','146 Nguyễn Văn Cừ, Phường 24, Quận 5, Hồ Chí Minh','Phường 24','Quận 5','Hồ Chí Minh',TRUE,'0900000024',NULL,'buyer24_public_id','buyer24@example.com','FEMALE','2001-12-24',NOW(),NULL,NULL),
    ('toiyeufpt','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 25','147 Nguyễn Văn Cừ, Phường 25, Quận 5, Hồ Chí Minh','Phường 25','Quận 5','Hồ Chí Minh',TRUE,'0900000025',NULL,'buyer25_public_id','buyer25@example.com','MALE','2002-01-25',NOW(),NULL,NULL),
    ('fptcoyeutoikhong','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 26','148 Nguyễn Văn Cừ, Phường 26, Quận 5, Hồ Chí Minh','Phường 26','Quận 5','Hồ Chí Minh',TRUE,'0900000026',NULL,'buyer26_public_id','buyer26@example.com','FEMALE','2002-02-26',NOW(),NULL,NULL),
    ('coyeukhongthibao','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 27','149 Nguyễn Văn Cừ, Phường 27, Quận 5, Hồ Chí Minh','Phường 27','Quận 5','Hồ Chí Minh',TRUE,'0900000027',NULL,'buyer27_public_id','buyer27@example.com','MALE','2002-03-27',NOW(),NULL,NULL),
    ('cobecuaanhoi','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 28','150 Nguyễn Văn Cừ, Phường 28, Quận 5, Hồ Chí Minh','Phường 28','Quận 5','Hồ Chí Minh',TRUE,'0900000028',NULL,'buyer28_public_id','buyer28@example.com','FEMALE','2002-04-28',NOW(),NULL,NULL),
    ('homnaydihoctre','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 29','151 Nguyễn Văn Cừ, Phường 29, Quận 5, Hồ Chí Minh','Phường 29','Quận 5','Hồ Chí Minh',TRUE,'0900000029',NULL,'buyer29_public_id','buyer29@example.com','MALE','2002-05-29',NOW(),NULL,NULL),
    ('FEcoganglennha','{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2','Buyer 30','152 Nguyễn Văn Cừ, Phường 30, Quận 5, Hồ Chí Minh','Phường 30','Quận 5','Hồ Chí Minh',TRUE,'0900000030',NULL,'buyer30_public_id','buyer30@example.com','FEMALE','2002-06-30',NOW(),NULL,NULL);


-- =========================================================
-- 🏪 WALLET
-- =========================================================

INSERT INTO wallet (balance, concurrency, provider, created_at, buyer_id)
VALUES
(1000000.00, 'VND', 'VNPay', NOW(), 1),
(1100000.00, 'VND', 'VNPay', NOW(), 2),
(1200000.00, 'VND', 'VNPay', NOW(), 3),
(1300000.00, 'VND', 'VNPay', NOW(), 4),
(1400000.00, 'VND', 'VNPay', NOW(), 5),
(1500000.00, 'VND', 'VNPay', NOW(), 6),
(1600000.00, 'VND', 'VNPay', NOW(), 7),
(1700000.00, 'VND', 'VNPay', NOW(), 8),
(1800000.00, 'VND', 'VNPay', NOW(), 9),
(1900000.00, 'VND', 'VNPay', NOW(), 10),
(2000000.00, 'VND', 'VNPay', NOW(), 11),
(2100000.00, 'VND', 'VNPay', NOW(), 12),
(2200000.00, 'VND', 'VNPay', NOW(), 13),
(2300000.00, 'VND', 'VNPay', NOW(), 14),
(2400000.00, 'VND', 'VNPay', NOW(), 15),
(2500000.00, 'VND', 'VNPay', NOW(), 16),
(2600000.00, 'VND', 'VNPay', NOW(), 17),
(2700000.00, 'VND', 'VNPay', NOW(), 18),
(2800000.00, 'VND', 'VNPay', NOW(), 19),
(2900000.00, 'VND', 'VNPay', NOW(), 20),
(3000000.00, 'VND', 'VNPay', NOW(), 21),
(3100000.00, 'VND', 'VNPay', NOW(), 22),
(3200000.00, 'VND', 'VNPay', NOW(), 23),
(3300000.00, 'VND', 'VNPay', NOW(), 24),
(3400000.00, 'VND', 'VNPay', NOW(), 25),
(3500000.00, 'VND', 'VNPay', NOW(), 26),
(3600000.00, 'VND', 'VNPay', NOW(), 27),
(3700000.00, 'VND', 'VNPay', NOW(), 28),
(3800000.00, 'VND', 'VNPay', NOW(), 29),
(3900000.00, 'VND', 'VNPay', NOW(), 30);

-- =========================================================
-- 🏪 SELLER
-- =========================================================
INSERT INTO seller (
    identity_front_image_url,
    identity_back_image_url,
    business_license_url,
    selfie_url,
    status,
    store_name,
    store_policy_url,
    tax_number,
    identity_number,
    created_at,
    updated_at,
    deleted_at,
    buyer_id,
    admin_id,
    ghn_shop_id
)
VALUES
('https://cdn.example.com/images/id_front_1.jpg', 'https://cdn.example.com/images/id_back_1.jpg', 'https://cdn.example.com/images/business_license_1.jpg', 'https://cdn.example.com/images/selfie_1.jpg', 'PENDING', 'Neko Store 1', 'https://cdn.example.com/policies/policy_1.pdf', 'TAX123456781', 'ID987654321', NOW(), NULL, NULL, 1, 1, 197654),
('https://cdn.example.com/images/id_front_2.jpg', 'https://cdn.example.com/images/id_back_2.jpg', 'https://cdn.example.com/images/business_license_2.jpg', 'https://cdn.example.com/images/selfie_2.jpg', 'ACCEPTED', 'Neko Store 2', 'https://cdn.example.com/policies/policy_2.pdf', 'TAX123456782', 'ID987654322', NOW(), NULL, NULL, 2, 1, 197654),
('https://cdn.example.com/images/id_front_3.jpg', 'https://cdn.example.com/images/id_back_3.jpg', 'https://cdn.example.com/images/business_license_3.jpg', 'https://cdn.example.com/images/selfie_3.jpg', 'ACCEPTED', 'Neko Store 3', 'https://cdn.example.com/policies/policy_3.pdf', 'TAX123456783', 'ID987654323', NOW(), NULL, NULL, 3, 1, 197654),
('https://cdn.example.com/images/id_front_4.jpg', 'https://cdn.example.com/images/id_back_4.jpg', 'https://cdn.example.com/images/business_license_4.jpg', 'https://cdn.example.com/images/selfie_4.jpg', 'PENDING', 'Neko Store 4', 'https://cdn.example.com/policies/policy_4.pdf', 'TAX123456784', 'ID987654324', NOW(), NULL, NULL, 4, 1, 197654),
('https://cdn.example.com/images/id_front_5.jpg', 'https://cdn.example.com/images/id_back_5.jpg', 'https://cdn.example.com/images/business_license_5.jpg', 'https://cdn.example.com/images/selfie_5.jpg', 'ACCEPTED', 'Neko Store 5', 'https://cdn.example.com/policies/policy_5.pdf', 'TAX123456785', 'ID987654325', NOW(), NULL, NULL, 5, 1, 197654),
('https://cdn.example.com/images/id_front_6.jpg', 'https://cdn.example.com/images/id_back_6.jpg', 'https://cdn.example.com/images/business_license_6.jpg', 'https://cdn.example.com/images/selfie_6.jpg', 'ACCEPTED', 'Neko Store 6', 'https://cdn.example.com/policies/policy_6.pdf', 'TAX123456786', 'ID987654326', NOW(), NULL, NULL, 6, 1, 197654),
('https://cdn.example.com/images/id_front_7.jpg', 'https://cdn.example.com/images/id_back_7.jpg', 'https://cdn.example.com/images/business_license_7.jpg', 'https://cdn.example.com/images/selfie_7.jpg', 'PENDING', 'Neko Store 7', 'https://cdn.example.com/policies/policy_7.pdf', 'TAX123456787', 'ID987654327', NOW(), NULL, NULL, 7, 1, 197654),
('https://cdn.example.com/images/id_front_8.jpg', 'https://cdn.example.com/images/id_back_8.jpg', 'https://cdn.example.com/images/business_license_8.jpg', 'https://cdn.example.com/images/selfie_8.jpg', 'ACCEPTED', 'Neko Store 8', 'https://cdn.example.com/policies/policy_8.pdf', 'TAX123456788', 'ID987654328', NOW(), NULL, NULL, 8, 1, 197654),
('https://cdn.example.com/images/id_front_9.jpg', 'https://cdn.example.com/images/id_back_9.jpg', 'https://cdn.example.com/images/business_license_9.jpg', 'https://cdn.example.com/images/selfie_9.jpg', 'ACCEPTED', 'Neko Store 9', 'https://cdn.example.com/policies/policy_9.pdf', 'TAX123456789', 'ID987654329', NOW(), NULL, NULL, 9, 1, 197654),
('https://cdn.example.com/images/id_front_10.jpg', 'https://cdn.example.com/images/id_back_10.jpg', 'https://cdn.example.com/images/business_license_10.jpg', 'https://cdn.example.com/images/selfie_10.jpg', 'PENDING', 'Neko Store 10', 'https://cdn.example.com/policies/policy_10.pdf', 'TAX123456790', 'ID987654330', NOW(), NULL, NULL, 10, 1, 197654),
('https://cdn.example.com/images/id_front_11.jpg', 'https://cdn.example.com/images/id_back_11.jpg', 'https://cdn.example.com/images/business_license_11.jpg', 'https://cdn.example.com/images/selfie_11.jpg', 'ACCEPTED', 'Neko Store 11', 'https://cdn.example.com/policies/policy_11.pdf', 'TAX123456791', 'ID987654331', NOW(), NULL, NULL, 11, 1, 197654),
('https://cdn.example.com/images/id_front_12.jpg', 'https://cdn.example.com/images/id_back_12.jpg', 'https://cdn.example.com/images/business_license_12.jpg', 'https://cdn.example.com/images/selfie_12.jpg', 'ACCEPTED', 'Neko Store 12', 'https://cdn.example.com/policies/policy_12.pdf', 'TAX123456792', 'ID987654332', NOW(), NULL, NULL, 12, 1, 197654),
('https://cdn.example.com/images/id_front_13.jpg', 'https://cdn.example.com/images/id_back_13.jpg', 'https://cdn.example.com/images/business_license_13.jpg', 'https://cdn.example.com/images/selfie_13.jpg', 'PENDING', 'Neko Store 13', 'https://cdn.example.com/policies/policy_13.pdf', 'TAX123456793', 'ID987654333', NOW(), NULL, NULL, 13, 1, 197654),
('https://cdn.example.com/images/id_front_14.jpg', 'https://cdn.example.com/images/id_back_14.jpg', 'https://cdn.example.com/images/business_license_14.jpg', 'https://cdn.example.com/images/selfie_14.jpg', 'ACCEPTED', 'Neko Store 14', 'https://cdn.example.com/policies/policy_14.pdf', 'TAX123456794', 'ID987654334', NOW(), NULL, NULL, 14, 1, 197654),
('https://cdn.example.com/images/id_front_15.jpg', 'https://cdn.example.com/images/id_back_15.jpg', 'https://cdn.example.com/images/business_license_15.jpg', 'https://cdn.example.com/images/selfie_15.jpg', 'ACCEPTED', 'Neko Store 15', 'https://cdn.example.com/policies/policy_15.pdf', 'TAX123456795', 'ID987654335', NOW(), NULL, NULL, 15, 1, 197654),
('https://cdn.example.com/images/id_front_16.jpg', 'https://cdn.example.com/images/id_back_16.jpg', 'https://cdn.example.com/images/business_license_16.jpg', 'https://cdn.example.com/images/selfie_16.jpg', 'PENDING', 'Neko Store 16', 'https://cdn.example.com/policies/policy_16.pdf', 'TAX123456796', 'ID987654336', NOW(), NULL, NULL, 16, 1, 197654),
('https://cdn.example.com/images/id_front_17.jpg', 'https://cdn.example.com/images/id_back_17.jpg', 'https://cdn.example.com/images/business_license_17.jpg', 'https://cdn.example.com/images/selfie_17.jpg', 'ACCEPTED', 'Neko Store 17', 'https://cdn.example.com/policies/policy_17.pdf', 'TAX123456797', 'ID987654337', NOW(), NULL, NULL, 17, 1, 197654),
('https://cdn.example.com/images/id_front_18.jpg', 'https://cdn.example.com/images/id_back_18.jpg', 'https://cdn.example.com/images/business_license_18.jpg', 'https://cdn.example.com/images/selfie_18.jpg', 'PENDING', 'Neko Store 18', 'https://cdn.example.com/policies/policy_18.pdf', 'TAX123456798', 'ID987654338', NOW(), NULL, NULL, 18, 1, 197654),
('https://cdn.example.com/images/id_front_19.jpg', 'https://cdn.example.com/images/id_back_19.jpg', 'https://cdn.example.com/images/business_license_19.jpg', 'https://cdn.example.com/images/selfie_19.jpg', 'ACCEPTED', 'Neko Store 19', 'https://cdn.example.com/policies/policy_19.pdf', 'TAX123456799', 'ID987654339', NOW(), NULL, NULL, 19, 1, 197654),
('https://cdn.example.com/images/id_front_20.jpg', 'https://cdn.example.com/images/id_back_20.jpg', 'https://cdn.example.com/images/business_license_20.jpg', 'https://cdn.example.com/images/selfie_20.jpg', 'PENDING', 'Neko Store 20', 'https://cdn.example.com/policies/policy_20.pdf', 'TAX123456800', 'ID987654340', NOW(), NULL, NULL, 20, 1, 197654);

-- =========================================================
-- 🧾 SUBSCRIPTION - GÁN GÓI CHO SELLER
-- =========================================================
-- Seller 1–5: Gói Basic (30 ngày)
-- Seller 6–10: Gói Pro (90 ngày)
-- Seller 11–15: Gói Premium (180 ngày)
-- Seller 16–20: Gói Legacy (30 ngày, không còn hoạt động)

INSERT INTO subscription (seller_id, subscription_package_id, is_active, start_day, end_day)
VALUES
(1, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(2, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(3, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(4, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(5, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),

(6, 2, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
(7, 2, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
(8, 2, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
(9, 2, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
(10, 2, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),

(11, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 180 DAY)),
(12, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 180 DAY)),
(13, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 180 DAY)),
(14, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 180 DAY)),
(15, 3, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 180 DAY)),

(16, 4, FALSE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(17, 4, FALSE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(18, 4, FALSE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(19, 4, FALSE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(20, 4, FALSE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY))
ON DUPLICATE KEY UPDATE seller_id = seller_id;

-- =========================================================
-- 🗂 CATEGORY
-- =========================================================
INSERT INTO category (name, description)
VALUES
('Xe điện', 'Các loại xe điện như xe máy điện, xe đạp điện, ô tô điện.'),
('Pin điện', 'Các loại pin, ắc quy, pin sạc và linh kiện điện liên quan.'),
('Phụ kiện & Bộ sạc', 'Các loại sạc, cáp, adapter và phụ kiện cho xe điện.'),
('Phụ tùng xe điện', 'Các linh kiện thay thế và bảo dưỡng xe điện.'),
('Khác', 'Các sản phẩm điện khác.');

-- =========================================================
-- 🚗 POST_PRODUCT (chỉ 5 mẫu ban đầu)
-- =========================================================
INSERT INTO post_product (
    title,
    brand,
    model,
    manufacture_year,
    used_duration,
    rejected_reason,
    condition_level,
    price,
    description,
    location_trading,
    verified_decision_status,
    active,
    verified,
    created_at,
    updated_at,
    deleted_at,
    category_id,
    admin_id,
    seller_id,
    is_sold,
    length,
    width,
    height,
    weight
)
VALUES
-- ================= Xe điện (Category 1)
('Xe máy điện VinFast Klara S', 'VinFast', 'Klara S 2023', 2023, '6 tháng', NULL, 'Rất tốt', 1650.00, 'Xe máy điện VinFast Klara S mới 98%.', 'Hà Nội', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '185', '68', '112', '95000'),
('Xe điện Pega eSH', 'Pega', 'eSH 2022', 2022, '1 năm', NULL, 'Tốt', 1200.00, 'Xe điện Pega eSH ổn định, pin tốt.', 'TP.HCM', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '190', '70', '115', '92000'),
('Xe điện Dibao Gogo SS', 'Dibao', 'Gogo SS 2023', 2023, '3 tháng', NULL, 'Rất tốt', 990.00, 'Xe Dibao Gogo kiểu dáng hiện đại.', 'Đà Nẵng', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '175', '66', '110', '88000'),
('Xe điện Yadea BuyE 2021', 'Yadea', 'BuyE 2021', 2021, '2 năm', NULL, 'Khá tốt', 850.00, 'Xe Yadea tiết kiệm năng lượng.', 'Huế', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '180', '67', '110', '90000'),
('Xe điện VinFast Feliz S', 'VinFast', 'Feliz S 2023', 2023, '5 tháng', NULL, 'Rất tốt', 1700.00, 'Feliz S pin khỏe, chạy 90km/lần sạc.', 'Bình Dương', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '190', '70', '118', '100000'),
('Xe điện Detech Espero', 'Detech', 'Espero 2022', 2022, '1 năm', NULL, 'Tốt', 1050.00, 'Xe Detech Espero bền bỉ.', 'Hải Phòng', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '183', '68', '112', '94000'),
('Xe điện JVC Xmen', 'JVC', 'Xmen 2023', 2023, '4 tháng', NULL, 'Rất tốt', 980.00, 'Xe Xmen mạnh mẽ, kiểu dáng thể thao.', 'Đà Lạt', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '180', '68', '110', '96000'),
('Xe điện YADEA Xmen Neo', 'Yadea', 'Xmen Neo 2022', 2022, '1 năm', NULL, 'Tốt', 1100.00, 'Xmen Neo bản mới, pin khỏe.', 'Cần Thơ', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '182', '68', '110', '95000'),
('Xe điện Detech Pansy', 'Detech', 'Pansy 2021', 2021, '2 năm', NULL, 'Khá tốt', 780.00, 'Xe điện Pansy nhỏ gọn, phù hợp học sinh.', 'Nha Trang', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '165', '60', '105', '80000'),
('Xe điện Anbico AP1508', 'Anbico', 'AP1508', 2023, '3 tháng', NULL, 'Xuất sắc', 1050.00, 'Anbico AP1508 pin khỏe, tốc độ ổn.', 'Hà Nội', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '185', '68', '112', '90000'),
('Xe điện EVGO C100', 'EVGO', 'C100', 2023, '6 tháng', NULL, 'Rất tốt', 1120.00, 'EVGO C100 hiện đại, tiết kiệm điện.', 'TP.HCM', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '185', '70', '115', '93000'),
('Xe đạp điện HKBike Zinger', 'HKBike', 'Zinger 2021', 2021, '2 năm', NULL, 'Khá tốt', 600.00, 'Xe đạp điện bền, pin 48V.', 'Huế', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '160', '55', '100', '70000'),
('Xe điện VinFast Evo200', 'VinFast', 'Evo200', 2023, '2 tháng', NULL, 'Xuất sắc', 1800.00, 'Evo200 đi 200km mỗi lần sạc.', 'Bình Dương', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '190', '70', '120', '98000'),
('Xe điện MBI V', 'MBI', 'V 2023', 2023, '3 tháng', NULL, 'Rất tốt', 1500.00, 'Xe MBI V thiết kế thể thao.', 'Hà Nội', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1, FALSE, '185', '68', '115', '94000'),
-- ================= Pin điện (Category 2)
('Pin Lithium 60V-30Ah', 'LG Chem', 'LG60V30A', 2022, '1 năm', NULL, 'Tốt', 3500000.00, 'Pin lithium cao cấp.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '42', '23', '18', '11500'),
('Pin LFP 72V-25Ah', 'CATL', 'CATL72V25A', 2023, '6 tháng', NULL, 'Rất tốt', 4100000.00, 'Pin LFP mới, hiệu năng cao.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '45', '25', '20', '13000'),
('Pin 48V-20Ah', 'Samsung SDI', 'SM48V20A', 2022, '1 năm', NULL, 'Tốt', 2400000.00, 'Pin Samsung chất lượng cao.', 'Hải Phòng', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '38', '22', '16', '9500'),
('Pin 60V-35Ah', 'BYD', 'BYD60V35A', 2023, '5 tháng', NULL, 'Rất tốt', 380000.00, 'Pin BYD bền, tuổi thọ cao.', 'Huế', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '46', '25', '20', '12800'),
('Pin Panasonic 72V-40Ah', 'Panasonic', 'PN72V40A', 2023, '4 tháng', NULL, 'Rất tốt', 4500000.00, 'Pin Panasonic cao cấp.', 'Cần Thơ', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '50', '28', '22', '14500'),
('Pin 48V-15Ah', 'LG Chem', 'LG48V15A', 2022, '10 tháng', NULL, 'Tốt', 2200000.00, 'Pin LG nhỏ gọn, hiệu suất cao.', 'Bình Dương', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '36', '20', '14', '8200'),
('Pin CATL 60V-25Ah', 'CATL', 'CATL60V25A', 2022, '1 năm', NULL, 'Tốt', 300000.00, 'Pin CATL an toàn, bền.', 'Đà Lạt', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '40', '23', '17', '10800'),
('Pin 72V-30Ah', 'Samsung SDI', 'SM72V30A', 2023, '5 tháng', NULL, 'Rất tốt', 4300000.00, 'Pin SDI dung lượng lớn.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '48', '26', '20', '12500'),
('Pin 48V-12Ah', 'Toshiba', 'TB48V12A', 2021, '2 năm', NULL, 'Khá tốt', 180000.00, 'Pin Toshiba bền.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '32', '18', '12', '6500'),
('Pin 60V-20Ah', 'VinFast', 'VF60V20A', 2023, '3 tháng', NULL, 'Xuất sắc', 330000.00, 'Pin VinFast chính hãng.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '40', '22', '17', '9800'),
('Pin LFP 48V-30Ah', 'CATL', 'CATL48V30A', 2023, '2 tháng', NULL, 'Rất tốt', 31000000.00, 'Pin LFP an toàn.', 'Đà Nẵng', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '42', '23', '18', '10500'),
('Pin Li-ion 36V-10Ah', 'Samsung', 'SM36V10A', 2022, '1 năm', NULL, 'Tốt', 190000000.00, 'Pin Li-ion cho xe đạp điện.', 'Cần Thơ', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '28', '16', '10', '4500'),
('Pin Lithium Titanate 60V-40Ah', 'LG Chem', 'LTO60V40A', 2023, '6 tháng', NULL, 'Rất tốt', 480000.00, 'Pin Titanate sạc nhanh.', 'Bình Dương', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1, FALSE, '45', '25', '20', '13200'),
-- ================= Bộ sạc & phụ kiện (Category 3)
('Bộ sạc nhanh 60V-5A', 'VinFast', 'VF60V5A', 2023, '3 tháng', NULL, 'Rất tốt', 120000.00, 'Sạc nhanh chính hãng 60V-5A.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '25', '20', '10', '3000'),
('Cáp sạc 48V tiêu chuẩn', 'Panasonic', 'PN48Cable', 2022, '1 năm', NULL, 'Tốt', 4000000.00, 'Cáp sạc 48V tiêu chuẩn.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '150', '3', '3', '1200'),
('Bộ sạc 72V-10A', 'Samsung', 'SM72V10A', 2023, '5 tháng', NULL, 'Rất tốt', 160000.00, 'Sạc 72V công suất lớn.', 'TP.HCM', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '30', '25', '12', '4000'),
('Adapter chuyển đổi 60V-48V', 'LG', 'LGAdapter', 2021, '2 năm', NULL, 'Khá tốt', 300000.00, 'Adapter giảm điện áp an toàn.', 'Đà Lạt', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '20', '10', '8', '2000'),
('Dock sạc đôi 60V', 'BYD', 'BYD60Dock', 2023, '4 tháng', NULL, 'Rất tốt', 180000.00, 'Dock sạc đôi hiệu suất cao.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '40', '25', '15', '5000'),
('Sạc đa năng 48-72V', 'CATL', 'CATLMulti', 2023, '5 tháng', NULL, 'Rất tốt', 1005000.00, 'Sạc đa năng cho nhiều dòng.', 'Cần Thơ', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '35', '25', '15', '4000'),
('Cáp chống cháy 60V', 'VinFast', 'VFCableSafe', 2023, '4 tháng', NULL, 'Rất tốt', 900000.00, 'Cáp chống cháy.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '150', '3', '3', '1300'),
('Adapter đổi phích 220V', 'Samsung', 'SM220Plug', 2022, '1 năm', NULL, 'Tốt', 2500000.00, 'Adapter đổi phích 220V.', 'Bình Dương', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '15', '10', '10', '600'),
('Củ sạc nhanh 48V-5A', 'Pega', 'PG48V5A', 2023, '6 tháng', NULL, 'Rất tốt', 100010.00, 'Sạc nhanh 48V-5A.', 'Hà Nội', 'PENDING', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '25', '20', '10', '2800'),
('Sạc không dây mini', 'Universal', 'UNIWireless', 2024, '1 tháng', NULL, 'Xuất sắc', 2000500.00, 'Sạc không dây cho xe điện.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1, FALSE, '15', '15', '5', '900'),
-- ================= Phụ tùng xe điện (Category 4)
('Bộ phanh đĩa xe điện', 'VinFast', 'VFBrake2023', 2023, '5 tháng', NULL, 'Rất tốt', 7500000.00, 'Phanh đĩa chính hãng.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '40', '40', '15', '6000'),
('Bánh xe điện 14 inch', 'Yadea', 'YD14Wheel', 2023, '6 tháng', NULL, 'Tốt', 605012.00, 'Bánh xe điện 14 inch.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '35', '35', '15', '4500'),
('Tay ga điện tử', 'LG', 'LGThrottle', 2023, '3 tháng', NULL, 'Rất tốt', 3500041.00, 'Tay ga điện tử mượt.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '20', '8', '8', '800'),
('Đèn pha LED xe điện', 'Pega', 'PGLEDLight', 2022, '9 tháng', NULL, 'Tốt', 45000025.00, 'Đèn LED tiết kiệm điện.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '18', '18', '12', '1200'),
('Mâm xe hợp kim 16 inch', 'VinFast', 'VFWheel16', 2023, '6 tháng', NULL, 'Rất tốt', 7000074.00, 'Mâm xe hợp kim.', 'Cần Thơ', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '42', '42', '15', '7000'),
('Yên xe thể thao', 'Yadea', 'YDSaddle', 2023, '7 tháng', NULL, 'Rất tốt', 5500025.00, 'Yên xe thể thao êm.', 'Hải Phòng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '45', '30', '15', '2500'),
('Tay cầm lái', 'Pega', 'PGHandle', 2022, '1 năm', NULL, 'Tốt', 257892.00, 'Tay cầm lái bền.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '25', '10', '8', '1000'),
('Gác chân xe điện', 'VinFast', 'VFFootRest', 2023, '4 tháng', NULL, 'Rất tốt', 2000000.00, 'Gác chân xe điện chính hãng.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '30', '15', '8', '1500'),
('Chắn bùn xe điện', 'Yadea', 'YDMudGuard', 2023, '5 tháng', NULL, 'Rất tốt', 3000000.00, 'Bộ chắn bùn chống nước.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '50', '25', '10', '2200'),
('Tem trang trí xe điện', 'Universal', 'UNISticker', 2024, '1 tháng', NULL, 'Xuất sắc', 1000000.00, 'Tem dán phong cách.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1, FALSE, '25', '20', '2', '300'),
-- ================= Khác (Category 5)
('Kính chiếu hậu xe điện', 'Pega', 'PGMirror', 2023, '8 tháng', NULL, 'Tốt', 150000.00, 'Kính chiếu hậu chống mờ.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 5, 1, 1, FALSE, '25', '10', '8', '600');
-- =========================================================
-- 🖼 PRODUCT_IMAGE
-- =========================================================
INSERT INTO product_image (order_image, image_url, post_id)
VALUES
(1, 'https://cdn.example.com/images/vinfast_klara_front.jpg', 1),
(2, 'https://cdn.example.com/images/vinfast_klara_side.jpg', 1),
(1, 'https://cdn.example.com/images/pega_esh_front.jpg', 2),
(1, 'https://cdn.example.com/images/lithium_battery_60v_front.jpg', 3),
(1, 'https://cdn.example.com/images/charger_60v5a.jpg', 4),
(1, 'https://cdn.example.com/images/brake_disc_vf.jpg', 5);

-- =========================================================
-- 🖼 SYSTEM_POLICY
-- =========================================================

INSERT INTO system_policy
(title, content, version, effective_date, expired_date, created_at, updated_at, status, admin_id)
VALUES
(
  'User Registration Policy',
  'When registering for an account on Green Trade Platform, users must provide accurate personal information, agree to the Terms of Service, Privacy Policy, and comply with the community rules. The system reserves the right to suspend or terminate any account that violates these terms.',
  1.0,
  NOW(),
  NULL,
  NOW(),
  NOW(),
  'ACTIVE',
  1
),
(
  'Account Upgrade Policy (Buyer to Seller)',
  'Users who upgrade their accounts from Buyer to Seller must verify identity, provide business-related information, and comply with seller obligations. Any violation may lead to temporary suspension or permanent deactivation of the seller account as determined by the platform administrators.',
  1.0,
  NOW(),
  NULL,
  NOW(),
  NOW(),
  'ACTIVE',
  1
);

-- =========================================================
-- 🚚 SHIPPING_PARTNER (ĐỐI TÁC VẬN CHUYỂN)
-- =========================================================

INSERT INTO shipping_partner (
    email, partner_name, address, website_url, hotline, created_at, updated_at
)
VALUES
    ('support@ghn.vn', 'GHN Express', '20 Đường Tân Sơn, P.15, Q.Tân Bình, TP.HCM', 'https://ghn.vn', '1900636681', NOW(), NOW()),
    ('contact@ghtk.vn', 'Giao Hàng Tiết Kiệm', '435 Hoàng Văn Thụ, P.4, Q.Tân Bình, TP.HCM', 'https://ghtk.vn', '19008092', NOW(), NOW()),
    ('cs@viettelpost.vn', 'Viettel Post', '01 Giang Văn Minh, Q.Ba Đình, Hà Nội', 'https://viettelpost.com.vn', '19008095', NOW(), NOW()),
    ('info@jtexpress.vn', 'J&T Express', '19 Nguyễn Trãi, Q.Thanh Xuân, Hà Nội', 'https://jtexpress.vn', '19001088', NOW(), NOW()),
    ('admin@beelogistics.com.vn', 'Bee Logistics', '12 Trần Hưng Đạo, Q.1, TP.HCM', 'https://beelogistics.com.vn', '02838222266', NOW(), NOW())
    ON DUPLICATE KEY UPDATE partner_name = VALUES(partner_name);


-- =========================================================
-- ORDERS
-- =========================================================

INSERT INTO orders (
    order_code, shipping_address, phone_number, price, shipping_fee,
    status, created_at, updated_at, canceled_at, cancel_reason,
    buyer_id, admin_id, post_id, shipping_partner_id
)
VALUES
    ('ORD-20231001-001','123 Nguyễn Văn Cừ, TP.HCM','0900000001',1650000.00,35000.00,'COMPLETED',NOW(),NOW(),NULL,NULL,1,1,1,1),
    ('ORD-20231001-002','124 Nguyễn Văn Cừ, Hà Nội','0900000002',1200000.00,30000.00,'SHIPPING',NOW(),NOW(),NULL,NULL,2,1,2,1),
    ('ORD-20231001-003','125 Nguyễn Văn Cừ, Đà Nẵng','0900000003',990000.00,25000.00,'PENDING',NOW(),NOW(),NULL,NULL,3,1,3,1),
    ('ORD-20231001-004','126 Nguyễn Văn Cừ, Huế','0900000004',850000.00,25000.00,'CANCELED',NOW(),NOW(),NOW(),'Người mua hủy do thay đổi ý định',4,1,4,1),
    ('ORD-20231001-005','127 Nguyễn Văn Cừ, Bình Dương','0900000005',1700000.00,40000.00,'COMPLETED',NOW(),NOW(),NULL,NULL,5,1,5,1),
    ('ORD-20231001-006','128 Nguyễn Văn Cừ, Cần Thơ','0900000006',1050000.00,30000.00,'PROCESSING',NOW(),NOW(),NULL,NULL,6,1,6,1),
    ('ORD-20231001-007','129 Nguyễn Văn Cừ, Hải Phòng','0900000007',1120000.00,35000.00,'COMPLETED',NOW(),NOW(),NULL,NULL,7,1,7,1),
    ('ORD-20231001-008','130 Nguyễn Văn Cừ, Đà Lạt','0900000008',1800000.00,40000.00,'SHIPPING',NOW(),NOW(),NULL,NULL,8,1,8,1),
    ('ORD-20231001-009','131 Nguyễn Văn Cừ, Nha Trang','0900000009',1500000.00,30000.00,'CANCELED',NOW(),NOW(),NOW(),'Admin phát hiện gian lận',9,1,9,1),
    ('ORD-20231001-010','132 Nguyễn Văn Cừ, Hà Nội','0900000010',1700000.00,35000.00,'AWAITING_PAYMENT',NOW(),NOW(),NULL,NULL,10,1,10,1);

-- =========================================================
-- ⚖️ DISPUTE_CATEGORY (DANH MỤC KHIẾU NẠI / TRANH CHẤP)
-- =========================================================

INSERT INTO dispute_category (title, reason, description)
VALUES
    ('Khiếu nại đơn hàng','Người mua không nhận được hàng','Đơn hàng thất lạc hoặc chưa được giao.'),
    ('Khiếu nại chất lượng sản phẩm','Sản phẩm không đúng mô tả','Sản phẩm không giống mô tả hoặc hư hại.'),
    ('Khiếu nại thanh toán','Thanh toán thất bại nhưng bị trừ tiền','Giao dịch bị lỗi nhưng đã bị trừ tiền.'),
    ('Khiếu nại hoàn tiền','Chậm xử lý hoàn tiền','Yêu cầu hoàn tiền chưa được xử lý.'),
    ('Khiếu nại người bán','Người bán không phản hồi','Người bán không xác nhận hoặc phản hồi.'),
    ('Khiếu nại vận chuyển','Giao hàng chậm hoặc thất lạc','Đối tác giao hàng chậm hoặc thất lạc.'),
    ('Khiếu nại chính sách','Chính sách hoàn tiền / đổi trả không rõ ràng','Người dùng khiếu nại chính sách.'),
    ('Khiếu nại khác','Khác (yêu cầu đặc biệt)','Các loại khiếu nại khác.')
    ON DUPLICATE KEY UPDATE title = VALUES(title);

-- ================= Payment Data =================
INSERT INTO payment (description, gateway_name)
VALUES
    ('Thanh toán khi nhận hàng (COD)', 'COD'),
    ('Thanh toán trực tuyến qua VNPay', 'VNPay');


-- =========================================================
-- 🧾 ORDER DATA (MẪU)
-- =========================================================


-- =========================================================
-- ✅ KẾT THÚC FILE DATA.SQL
-- =========================================================
