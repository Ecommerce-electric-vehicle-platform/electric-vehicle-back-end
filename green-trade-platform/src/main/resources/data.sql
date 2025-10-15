-- Ngăn lỗi khóa ngoại trong lúc reset dữ liệu
SET FOREIGN_KEY_CHECKS = 0;

-- Xoá dữ liệu cũ nếu có
DELETE FROM package_price;
DELETE FROM subscription_packages;
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
