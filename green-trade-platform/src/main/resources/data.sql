DELETE FROM subscription_packages

-- Gói cơ bản
INSERT INTO subscription_packages;
(package_name, description, is_active, max_product, max_storage_per_img, max_img_per_cost, created_at, updated_at)
VALUES
('Basic Plan', 'Gói cơ bản cho người mới bắt đầu', TRUE, 10, 2048, 3, NOW(), NOW());

-- Gói nâng cao
INSERT INTO subscription_packages
(package_name, description, is_active, max_product, max_storage_per_img, max_img_per_cost, created_at, updated_at)
VALUES
('Pro Plan', 'Gói chuyên nghiệp cho doanh nghiệp nhỏ', TRUE, 50, 4096, 6, NOW(), NOW());

-- Gói cao cấp
INSERT INTO subscription_packages
(package_name, description, is_active, max_product, max_storage_per_img, max_img_per_cost, created_at, updated_at)
VALUES
('Premium Plan', 'Gói cao cấp cho doanh nghiệp lớn', TRUE, 200, 8192, 10, NOW(), NOW());

-- Gói ngừng kích hoạt (ví dụ cho is_active = FALSE)
INSERT INTO subscription_packages
(package_name, description, is_active, max_product, max_storage_per_img, max_img_per_cost, created_at, updated_at)
VALUES
('Legacy Plan', 'Gói cũ, không còn được hỗ trợ', FALSE, 20, 2048, 5, NOW(), NOW());


DELETE FROM package_price;

-- Basic Plan: có 2 gói thời hạn
INSERT INTO package_price
(price, is_active, duration_by_day, currency, discount_percent, created_at, updated_at, package_id)
VALUES
(49000, TRUE, 30, 'VND', 0, NOW(), NOW(), 1),
(129000, TRUE, 90, 'VND', 10, NOW(), NOW(), 1);

-- Pro Plan
INSERT INTO package_price
(price, is_active, duration_by_day, currency, discount_percent, created_at, updated_at, package_id)
VALUES
(199000, TRUE, 30, 'VND', 0, NOW(), NOW(), 2),
(549000, TRUE, 90, 'VND', 8, NOW(), NOW(), 2),
(999000, TRUE, 180, 'VND', 15, NOW(), NOW(), 2);

-- Premium Plan
INSERT INTO package_price
(price, is_active, duration_by_day, currency, discount_percent, created_at, updated_at, package_id)
VALUES
(499000, TRUE, 30, 'VND', 0, NOW(), NOW(), 3),
(1299000, TRUE, 90, 'VND', 10, NOW(), NOW(), 3),
(2499000, TRUE, 180, 'VND', 20, NOW(), NOW(), 3);

-- Legacy Plan (ngừng kích hoạt)
INSERT INTO package_price
(price, is_active, duration_by_day, currency, discount_percent, created_at, updated_at, package_id)
VALUES
(99000, FALSE, 30, 'VND', 0, NOW(), NOW(), 4);