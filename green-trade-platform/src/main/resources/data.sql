-- =========================================================
-- 🚀 RESET DATABASE DỮ LIỆU DEMO
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM product_image;
DELETE FROM post_product;
DELETE FROM category;
DELETE FROM seller;
DELETE FROM buyer;
DELETE FROM admin;
DELETE FROM subscription_packages;
DELETE FROM package_price;

ALTER TABLE product_image AUTO_INCREMENT = 1;
ALTER TABLE post_product AUTO_INCREMENT = 1;
ALTER TABLE category AUTO_INCREMENT = 1;
ALTER TABLE seller AUTO_INCREMENT = 1;
ALTER TABLE buyer AUTO_INCREMENT = 1;
ALTER TABLE admin AUTO_INCREMENT = 1;
ALTER TABLE subscription_packages AUTO_INCREMENT = 1;
ALTER TABLE package_price AUTO_INCREMENT = 1;

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
    '$2a$10$6x/hnD9yYSkFVhHCP2SxEOMuFocUpJ1gPMzYZ5RXkmUCPHD1dYJei', -- password: 123456
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
VALUES (
    'truongdoanvien',
    '{bcrypt}$2a$10$0lvhh4z1X9DR5/6bJUacEux35ayoj1xsVeGIE3IED.e6Gs0.VPSi2', -- password: Vien.123456@
    'Truong Doan Vien',
    '123 Nguyen Van Cu, TP HCM',
    TRUE,
    '0909876543',
    'https://cdn.example.com/avatars/buyer1.jpg',
    'buyer1_public_id',
    'truongvien@example.com',
    'MALE',
    '2000-05-15',
    NOW(),
    NULL,
    NULL
);

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
    admin_id
)
VALUES (
    'https://cdn.example.com/images/id_front_1.jpg',
    'https://cdn.example.com/images/id_back_1.jpg',
    'https://cdn.example.com/images/business_license_1.jpg',
    'https://cdn.example.com/images/selfie_1.jpg',
    'ACCEPTED',
    'Neko Store',
    'https://cdn.example.com/policies/policy_1.pdf',
    'TAX123456789',
    'ID987654321',
    NOW(),
    NULL,
    NULL,
    1,
    1
);

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
    seller_id
)
VALUES
-- ================= Xe điện (Category 1)
('Xe máy điện VinFast Klara S', 'VinFast', 'Klara S 2023', 2023, '6 tháng', NULL, 'Rất tốt', 1650.00, 'Xe máy điện VinFast Klara S mới 98%.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện Pega eSH', 'Pega', 'eSH 2022', 2022, '1 năm', NULL, 'Tốt', 1200.00, 'Xe điện Pega eSH ổn định, pin tốt.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện Dibao Gogo SS', 'Dibao', 'Gogo SS 2023', 2023, '3 tháng', NULL, 'Rất tốt', 990.00, 'Xe Dibao Gogo kiểu dáng hiện đại.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện Yadea BuyE 2021', 'Yadea', 'BuyE 2021', 2021, '2 năm', NULL, 'Khá tốt', 850.00, 'Xe Yadea tiết kiệm năng lượng.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện VinFast Feliz S', 'VinFast', 'Feliz S 2023', 2023, '5 tháng', NULL, 'Rất tốt', 1700.00, 'Feliz S pin khỏe, chạy 90km/lần sạc.', 'Bình Dương', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện Detech Espero', 'Detech', 'Espero 2022', 2022, '1 năm', NULL, 'Tốt', 1050.00, 'Xe Detech Espero bền bỉ.', 'Hải Phòng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện JVC Xmen', 'JVC', 'Xmen 2023', 2023, '4 tháng', NULL, 'Rất tốt', 980.00, 'Xe Xmen mạnh mẽ, kiểu dáng thể thao.', 'Đà Lạt', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện YADEA Xmen Neo', 'Yadea', 'Xmen Neo 2022', 2022, '1 năm', NULL, 'Tốt', 1100.00, 'Xmen Neo bản mới, pin khỏe.', 'Cần Thơ', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện Detech Pansy', 'Detech', 'Pansy 2021', 2021, '2 năm', NULL, 'Khá tốt', 780.00, 'Xe điện Pansy nhỏ gọn, phù hợp học sinh.', 'Nha Trang', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện Anbico AP1508', 'Anbico', 'AP1508', 2023, '3 tháng', NULL, 'Xuất sắc', 1050.00, 'Anbico AP1508 pin khỏe, tốc độ ổn.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện EVGO C100', 'EVGO', 'C100', 2023, '6 tháng', NULL, 'Rất tốt', 1120.00, 'EVGO C100 hiện đại, tiết kiệm điện.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe đạp điện HKBike Zinger', 'HKBike', 'Zinger 2021', 2021, '2 năm', NULL, 'Khá tốt', 600.00, 'Xe đạp điện bền, pin 48V.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện VinFast Evo200', 'VinFast', 'Evo200', 2023, '2 tháng', NULL, 'Xuất sắc', 1800.00, 'Evo200 đi 200km mỗi lần sạc.', 'Bình Dương', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),
('Xe điện MBI V', 'MBI', 'V 2023', 2023, '3 tháng', NULL, 'Rất tốt', 1500.00, 'Xe MBI V thiết kế thể thao.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 1, 1, 1),

-- ================= Pin điện (Category 2)
('Pin Lithium 60V-30Ah', 'LG Chem', 'LG60V30A', 2022, '1 năm', NULL, 'Tốt', 350.00, 'Pin lithium cao cấp.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin LFP 72V-25Ah', 'CATL', 'CATL72V25A', 2023, '6 tháng', NULL, 'Rất tốt', 410.00, 'Pin LFP mới, hiệu năng cao.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin 48V-20Ah', 'Samsung SDI', 'SM48V20A', 2022, '1 năm', NULL, 'Tốt', 240.00, 'Pin Samsung chất lượng cao.', 'Hải Phòng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin 60V-35Ah', 'BYD', 'BYD60V35A', 2023, '5 tháng', NULL, 'Rất tốt', 380.00, 'Pin BYD bền, tuổi thọ cao.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin Panasonic 72V-40Ah', 'Panasonic', 'PN72V40A', 2023, '4 tháng', NULL, 'Rất tốt', 450.00, 'Pin Panasonic cao cấp.', 'Cần Thơ', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin 48V-15Ah', 'LG Chem', 'LG48V15A', 2022, '10 tháng', NULL, 'Tốt', 220.00, 'Pin LG nhỏ gọn, hiệu suất cao.', 'Bình Dương', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin CATL 60V-25Ah', 'CATL', 'CATL60V25A', 2022, '1 năm', NULL, 'Tốt', 300.00, 'Pin CATL an toàn, bền.', 'Đà Lạt', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin 72V-30Ah', 'Samsung SDI', 'SM72V30A', 2023, '5 tháng', NULL, 'Rất tốt', 430.00, 'Pin SDI dung lượng lớn.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin 48V-12Ah', 'Toshiba', 'TB48V12A', 2021, '2 năm', NULL, 'Khá tốt', 180.00, 'Pin Toshiba bền.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin 60V-20Ah', 'VinFast', 'VF60V20A', 2023, '3 tháng', NULL, 'Xuất sắc', 330.00, 'Pin VinFast chính hãng.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin LFP 48V-30Ah', 'CATL', 'CATL48V30A', 2023, '2 tháng', NULL, 'Rất tốt', 310.00, 'Pin LFP an toàn.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin Li-ion 36V-10Ah', 'Samsung', 'SM36V10A', 2022, '1 năm', NULL, 'Tốt', 190.00, 'Pin Li-ion cho xe đạp điện.', 'Cần Thơ', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),
('Pin Lithium Titanate 60V-40Ah', 'LG Chem', 'LTO60V40A', 2023, '6 tháng', NULL, 'Rất tốt', 480.00, 'Pin Titanate sạc nhanh.', 'Bình Dương', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 2, 1, 1),

-- ================= Bộ sạc & phụ kiện (Category 3)
('Bộ sạc nhanh 60V-5A', 'VinFast', 'VF60V5A', 2023, '3 tháng', NULL, 'Rất tốt', 120.00, 'Sạc nhanh chính hãng 60V-5A.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),
('Cáp sạc 48V tiêu chuẩn', 'Panasonic', 'PN48Cable', 2022, '1 năm', NULL, 'Tốt', 40.00, 'Cáp sạc 48V tiêu chuẩn.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),
('Bộ sạc 72V-10A', 'Samsung', 'SM72V10A', 2023, '5 tháng', NULL, 'Rất tốt', 160.00, 'Sạc 72V công suất lớn.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),
('Adapter chuyển đổi 60V-48V', 'LG', 'LGAdapter', 2021, '2 năm', NULL, 'Khá tốt', 30.00, 'Adapter giảm điện áp an toàn.', 'Đà Lạt', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),
('Dock sạc đôi 60V', 'BYD', 'BYD60Dock', 2023, '4 tháng', NULL, 'Rất tốt', 180.00, 'Dock sạc đôi hiệu suất cao.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),
('Sạc đa năng 48-72V', 'CATL', 'CATLMulti', 2023, '5 tháng', NULL, 'Rất tốt', 150.00, 'Sạc đa năng cho nhiều dòng.', 'Cần Thơ', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),
('Cáp chống cháy 60V', 'VinFast', 'VFCableSafe', 2023, '4 tháng', NULL, 'Rất tốt', 90.00, 'Cáp chống cháy.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),
('Adapter đổi phích 220V', 'Samsung', 'SM220Plug', 2022, '1 năm', NULL, 'Tốt', 25.00, 'Adapter đổi phích 220V.', 'Bình Dương', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),
('Củ sạc nhanh 48V-5A', 'Pega', 'PG48V5A', 2023, '6 tháng', NULL, 'Rất tốt', 110.00, 'Sạc nhanh 48V-5A.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),
('Sạc không dây mini', 'Universal', 'UNIWireless', 2024, '1 tháng', NULL, 'Xuất sắc', 200.00, 'Sạc không dây cho xe điện.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 3, 1, 1),

-- ================= Phụ tùng xe điện (Category 4)
('Bộ phanh đĩa xe điện', 'VinFast', 'VFBrake2023', 2023, '5 tháng', NULL, 'Rất tốt', 75.00, 'Phanh đĩa chính hãng.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),
('Bánh xe điện 14 inch', 'Yadea', 'YD14Wheel', 2023, '6 tháng', NULL, 'Tốt', 60.00, 'Bánh xe điện 14 inch.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),
('Tay ga điện tử', 'LG', 'LGThrottle', 2023, '3 tháng', NULL, 'Rất tốt', 35.00, 'Tay ga điện tử mượt.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),
('Đèn pha LED xe điện', 'Pega', 'PGLEDLight', 2022, '9 tháng', NULL, 'Tốt', 45.00, 'Đèn LED tiết kiệm điện.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),
('Mâm xe hợp kim 16 inch', 'VinFast', 'VFWheel16', 2023, '6 tháng', NULL, 'Rất tốt', 70.00, 'Mâm xe hợp kim.', 'Cần Thơ', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),
('Yên xe thể thao', 'Yadea', 'YDSaddle', 2023, '7 tháng', NULL, 'Rất tốt', 55.00, 'Yên xe thể thao êm.', 'Hải Phòng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),
('Tay cầm lái', 'Pega', 'PGHandle', 2022, '1 năm', NULL, 'Tốt', 25.00, 'Tay cầm lái bền.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),
('Gác chân xe điện', 'VinFast', 'VFFootRest', 2023, '4 tháng', NULL, 'Rất tốt', 20.00, 'Gác chân xe điện chính hãng.', 'Đà Nẵng', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),
('Chắn bùn xe điện', 'Yadea', 'YDMudGuard', 2023, '5 tháng', NULL, 'Rất tốt', 30.00, 'Bộ chắn bùn chống nước.', 'TP.HCM', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),
('Tem trang trí xe điện', 'Universal', 'UNISticker', 2024, '1 tháng', NULL, 'Xuất sắc', 10.00, 'Tem dán phong cách.', 'Hà Nội', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 4, 1, 1),

-- ================= Khác (Category 5)
('Kính chiếu hậu xe điện', 'Pega', 'PGMirror', 2023, '8 tháng', NULL, 'Tốt', 15.00, 'Kính chiếu hậu chống mờ.', 'Huế', 'APPROVED', TRUE, TRUE, NOW(), NOW(), NULL, 5, 1, 1);
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
-- ✅ KẾT THÚC FILE DATA.SQL
-- =========================================================
