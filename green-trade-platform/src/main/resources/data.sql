-- Ngăn lỗi khóa ngoại trong lúc reset dữ liệu
SET FOREIGN_KEY_CHECKS = 0;

-- Xoá dữ liệu cũ nếu có
DELETE FROM package_price;
DELETE FROM subscription_packages;
DELETE FROM post_product;
DELETE FROM category;
DELETE FROM product_image;
--DELETE FROM admin;
--DELETE FROM seller;
--DELETE FROM buyer;

-- Reset auto increment để tránh lệch ID
ALTER TABLE buyer AUTO_INCREMENT = 1;
ALTER TABLE seller AUTO_INCREMENT = 1;
ALTER TABLE subscription_packages AUTO_INCREMENT = 1;
ALTER TABLE package_price AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 📦 INSERT CÁC GÓI ĐĂNG KÝ
-- ==========================================
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

-- ==========================================
-- 💰 INSERT GIÁ CÁC GÓI
-- ==========================================
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
-- Basic Plan
(49000, TRUE, 30, 'VND', 0, NOW(), NOW(), 1),
(129000, TRUE, 90, 'VND', 10, NOW(), NOW(), 1),

-- Pro Plan
(199000, TRUE, 30, 'VND', 0, NOW(), NOW(), 2),
(549000, TRUE, 90, 'VND', 8, NOW(), NOW(), 2),
(999000, TRUE, 180, 'VND', 15, NOW(), NOW(), 2),

-- Premium Plan
(499000, TRUE, 30, 'VND', 0, NOW(), NOW(), 3),
(1299000, TRUE, 90, 'VND', 10, NOW(), NOW(), 3),
(2499000, TRUE, 180, 'VND', 20, NOW(), NOW(), 3),

-- Legacy Plan
(99000, FALSE, 30, 'VND', 0, NOW(), NOW(), 4)
ON DUPLICATE KEY UPDATE package_id = package_id;

-- =============================================
-- TRIGGER ĐỂ SET ACTIVE CHO GÓI CỦA SELLER
-- =============================================
-- Bật event scheduler (chạy 1 lần)
SET GLOBAL event_scheduler = ON;

-- Tạo event tự động vô hiệu hóa gói đã hết hạn
CREATE EVENT IF NOT EXISTS deactivate_expired_subscriptions
ON SCHEDULE EVERY 1 MINUTE
DO
  UPDATE subscription
  SET is_active = FALSE
  WHERE end_day <= NOW() AND is_active = TRUE;

-- =============================
-- 1️⃣ ADMIN
-- =============================
--INSERT INTO admin (
--    avatar_url,
--    employee_number,
--    password,
--    full_name,
--    phone_number,
--    is_super_admin,
--    email,
--    status,
--    gender,
--    created_at,
--    updated_at
--)
--VALUES (
--    'https://cdn.example.com/avatar/admin1.png',
--    '1234567890',
--    '$2a$10$6x/hnD9yYSkFVhHCP2SxEOMuFocUpJ1gPMzYZ5RXkmUCPHD1dYJei', -- BCrypt hash cho '123456'
--    'Nguyen Van Quan Tri',
--    '0901123456',
--    TRUE,
--    'admin@example.com',
--    'ACTIVE',
--    'MALE',
--    NOW(),
--    NOW()
--);
--
---- =============================
---- 2️⃣ BUYER
---- =============================
--INSERT INTO buyer (
--    username,
--    password,
--    full_name,
--    default_shipping_address,
--    is_active,
--    phone_number,
--    avatar_url,
--    avatar_public_id,
--    email,
--    gender,
--    date_of_birth,
--    created_at,
--    updated_at,
--    deleted_at
--)
--VALUES (
--    'truongdoanvien',
--    '$2a$10$WZRTczN8rJXwb8.6g/Al/OeAqC6STHqWZMuSn9sB1MF4f04R74vQ.', -- hash của '123456'
--    'Truong Doan Vien',
--    '123 Nguyen Van Cu, TP HCM',
--    TRUE,
--    '0909876543',
--    'https://cdn.example.com/avatars/buyer1.jpg',
--    'buyer1_public_id',
--    'truongvien@example.com',
--    'MALE',
--    '2000-05-15',
--    NOW(),
--    NULL,
--    NULL
--);
--
---- =============================
---- 3️⃣ SELLER
---- =============================
--INSERT INTO seller (
--    identity_front_image_url,
--    identity_back_image_url,
--    business_license_url,
--    selfie_url,
--    status,
--    store_name,
--    store_policy_url,
--    tax_number,
--    identity_number,
--    created_at,
--    updated_at,
--    deleted_at,
--    buyer_id,
--    admin_id
--)
--VALUES (
--    'https://cdn.example.com/images/id_front_1.jpg',
--    'https://cdn.example.com/images/id_back_1.jpg',
--    'https://cdn.example.com/images/business_license_1.jpg',
--    'https://cdn.example.com/images/selfie_1.jpg',
--    'ACCEPTED',
--    'Neko Store',
--    'https://cdn.example.com/policies/policy_1.pdf',
--    'TAX123456789',
--    'ID987654321',
--    NOW(),
--    NULL,
--    NULL,
--    1,  -- buyer_id
--    NULL  -- admin_id
--);
--
---- =============================
---- 4️⃣ CATEGORY
---- =============================
--INSERT INTO category (name, description)
--VALUES
--('Xe điện', 'Các loại xe điện như xe máy điện, xe đạp điện, ô tô điện.'),
--('Pin điện', 'Các loại pin, ắc quy, pin sạc và linh kiện điện liên quan.');
--
---- =============================
---- 5️⃣ POST_PRODUCT
---- =============================
--INSERT INTO post_product (
--    title,
--    brand,
--    model,
--    manufacture_year,
--    used_duration,
--    rejected_reason,
--    condition_level,
--    price,
--    description,
--    location_trading,
--    status,
--    created_at,
--    updated_at,
--    deleted_at,
--    category_id,
--    admin_id,
--    seller_id
--)
--VALUES
---- Sản phẩm 1: Xe điện
--('Xe máy điện VinFast Klara S',
-- 'VinFast',
-- 'Klara S 2023',
-- 2023,
-- '6 tháng',
-- NULL,
-- 'Rất tốt',
-- 1650.00,
-- 'Xe máy điện VinFast Klara S đời mới, đi được 80km/lần sạc, xe chính hãng, mới 98%.',
-- 'Hà Nội, Việt Nam',
-- TRUE,
-- NOW(),
-- NOW(),
-- NULL,
-- 1,  -- category_id: Xe điện
-- 1,  -- admin_id
-- 1   -- seller_id
--),
--
---- Sản phẩm 2: Pin điện
--('Pin Lithium 60V-30Ah cho xe điện',
-- 'LG Chem',
-- 'LG60V30A',
-- 2022,
-- '1 năm',
-- NULL,
-- 'Tốt',
-- 350.00,
-- 'Pin lithium cao cấp 60V dung lượng 30Ah, tương thích với nhiều dòng xe điện, hiệu suất cao.',
-- 'TP. Hồ Chí Minh, Việt Nam',
-- TRUE,
-- NOW(),
-- NOW(),
-- NULL,
-- 2,  -- category_id: Pin điện
-- 1,  -- admin_id
-- 1   -- seller_id
--);
--
---- =============================
---- 6️⃣ PRODUCT_IMAGE
---- =============================
--
---- Giả sử 2 sản phẩm ở trên có post_id = 1, 2
--INSERT INTO product_image (order_image, image_url, post_id)
--VALUES
--(1, 'https://cdn.example.com/images/vinfast_klara_front.jpg', 1),
--(2, 'https://cdn.example.com/images/vinfast_klara_side.jpg', 1),
--(3, 'https://cdn.example.com/images/vinfast_klara_back.jpg', 1),
--
--(1, 'https://cdn.example.com/images/lithium_battery_60v_front.jpg', 2),
--(2, 'https://cdn.example.com/images/lithium_battery_60v_top.jpg', 2);

-- ===================================================================
-- 🏪 INSERT SELLER GẮN VỚI BUYER --> GẮN CODE NÀY TRONG PHPADMIN
-- NẾU BỎ COMMENT THÌ RUN SẼ BỊ LỖI => ĐOÀN VIÊN KHÔNG CHỊU TRÁCH NHIỆM
-- ====================================================================
--INSERT INTO seller (
--    identity_front_image_url,
--    identity_back_image_url,
--    business_license_url,
--    selfie_url,
--    status,
--    store_name,
--    store_policy_url,
--    tax_number,
--    identity_number,
--    created_at,
--    updated_at,
--    deleted_at,
--    buyer_id
--)
--VALUES (
--    'https://cdn.example.com/images/id_front_1.jpg',
--    'https://cdn.example.com/images/id_back_1.jpg',
--    'https://cdn.example.com/images/business_license_1.jpg',
--    'https://cdn.example.com/images/selfie_1.jpg',
--    'ACCEPTED',
--    'Neko Store',
--    'https://cdn.example.com/policies/policy_1.pdf',
--    'TAX123456789',
--    'ID987654321',
--    NOW(),
--    NULL,
--    NULL,
--    1
--)
--ON DUPLICATE KEY UPDATE seller_id = seller_id;
