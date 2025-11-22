# Green Trade Platform - Backend API

> **Nền tảng thương mại điện tử chuyên về xe điện và pin điện đã qua sử dụng**

Green Trade Platform là một hệ thống backend API được xây dựng bằng Spring Boot, cung cấp nền tảng giao dịch an toàn và minh bạch cho người mua và người bán các sản phẩm xe điện và pin điện đã qua sử dụng tại Việt Nam.

## 📋 Mục lục

- [Tính năng chính](#-tính-năng-chính)
- [Công nghệ sử dụng](#-công-nghệ-sử dụng)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Cài đặt và chạy dự án](#-cài-đặt-và-chạy-dự-án)
- [Cấu hình](#-cấu-hình)
- [API Documentation](#-api-documentation)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Chức năng chi tiết](#-chức-năng-chi-tiết)

## ✨ Tính năng chính

### 👤 Quản lý người dùng
- **Đa vai trò**: Buyer (Người mua), Seller (Người bán), Admin (Quản trị viên)
- **Xác thực JWT**: Bảo mật với JWT token
- **Quản lý profile**: Cập nhật thông tin cá nhân, địa chỉ
- **KYC**: Xác thực danh tính cho seller

### 🛍️ Quản lý sản phẩm
- **Đăng bán sản phẩm**: Upload sản phẩm với nhiều hình ảnh
- **Danh mục**: Phân loại sản phẩm (Xe điện, Pin điện)
- **Tìm kiếm & Lọc**: 
  - Tìm kiếm theo title, brand, model, condition, location
  - Lọc theo category name
  - Pagination hỗ trợ
- **Xác thực sản phẩm**: Admin review và verify sản phẩm
- **AI hỗ trợ**: Tự động tạo mô tả sản phẩm bằng Gemini AI
- **Bad word filter**: Lọc từ ngữ không phù hợp

### 🛒 Quản lý đơn hàng
- **Tạo đơn hàng**: Đặt hàng với nhiều phương thức thanh toán
- **Escrow Service**: Hệ thống ký gửi tiền an toàn (System Wallet)
- **Trạng thái đơn hàng**: Theo dõi trạng thái đơn hàng chi tiết
- **Hủy đơn hàng**: Hủy với lý do cụ thể
- **Invoice**: Tự động tạo hóa đơn cho đơn hàng

### 💰 Thanh toán & Ví
- **Đa phương thức**: Hỗ trợ MoMo, VnPay, COD
- **System Wallet**: Quản lý tiền ký gửi (Escrow)
- **Transaction tracking**: Theo dõi mọi giao dịch

### 🚚 Vận chuyển
- **GHN Integration**: Tích hợp Giao hàng nhanh (GHN)
- **Tính phí vận chuyển**: Tự động tính phí ship
- **Tracking**: Theo dõi đơn hàng

### ⭐ Đánh giá & Tranh chấp
- **Review**: Người mua đánh giá sản phẩm và người bán
- **Rating**: Hệ thống đánh giá sao
- **Dispute Resolution**: Xử lý tranh chấp với nhiều loại dispute
- **Admin mediation**: Admin can thiệp và giải quyết

### 📦 Gói đăng ký (Subscription)
- **Subscription Packages**: Nhiều gói đăng ký cho seller
- **Post limits**: Giới hạn số lượng đăng bài theo gói
- **Premium features**: Tính năng cao cấp cho gói trả phí

### 👥 Social Features
- **Follow Seller**: Người mua có thể follow seller yêu thích
- **Wishlist**: Lưu sản phẩm yêu thích
- **Seller followers**: Seller xem danh sách người follow

## 🛠 Công nghệ sử dụng

### Backend Framework
- **Spring Boot 3.3.4** - Framework chính
- **Java 21** - Ngôn ngữ lập trình
- **Spring Security** - Bảo mật và xác thực
- **Spring Data JPA** - ORM và truy vấn database
- **Spring WebSocket** - Real-time communication

### Database & Cache
- **MySQL 8.0** - Database chính
- **Redis 7** - Cache và session management

### Third-party Services
- **Cloudinary** - Lưu trữ và quản lý hình ảnh
- **Google Gemini AI** - AI hỗ trợ viết mô tả sản phẩm
- **MoMo Payment Gateway** - Cổng thanh toán MoMo
- **VnPay Payment Gateway** - Cổng thanh toán VnPay
- **GHN API** - Tích hợp giao hàng nhanh
- **Gmail SMTP** - Gửi email thông báo

### Tools & Libraries
- **Lombok** - Giảm boilerplate code
- **Swagger/OpenAPI 3** - API documentation
- **JWT** - JSON Web Token authentication
- **Docker & Docker Compose** - Containerization

## 🏗 Kiến trúc hệ thống

```
┌─────────────────┐
│   Client Apps   │
│  (Web/Mobile)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Spring Boot    │
│   REST API      │
│                 │
│  - Controllers  │
│  - Services     │
│  - Repositories │
│  - Security     │
└────────┬────────┘
         │
    ┌────┴────┬──────────────┬──────────────┐
    │         │              │              │
    ▼         ▼              ▼              ▼
┌────────┐ ┌──────┐  ┌──────────┐  ┌──────────┐
│ MySQL  │ │Redis │  │Cloudinary│  │   AI     │
│        │ │      │  │          │  │  Gemini  │
└────────┘ └──────┘  └──────────┘  └──────────┘
         │
    ┌────┴────┬──────────────┬──────────────┐
    │         │              │              │
    ▼         ▼              ▼              ▼
┌────────┐ ┌──────┐  ┌──────────┐  ┌──────────┐
│  MoMo  │ │VnPay │  │   GHN    │  │  Email   │
│        │ │      │  │          │  │  SMTP    │
└────────┘ └──────┘  └──────────┘  └──────────┘
```

## 🚀 Cài đặt và chạy dự án

### Yêu cầu hệ thống
- **Java 21** hoặc cao hơn
- **Maven 3.6+**
- **Docker & Docker Compose** (khuyến nghị)
- **MySQL 8.0** (nếu không dùng Docker)

### Cách 1: Sử dụng Docker Compose (Khuyến nghị)

1. **Clone repository**
```bash
git clone <repository-url>
cd green-trade-platform
```

2. **Chạy với Docker Compose**
```bash
docker-compose up -d
```

Docker Compose sẽ tự động:
- Tạo và chạy MySQL container (port 3307)
- Tạo và chạy Redis container (port 6379)
- Tạo và chạy application container (port 8080)
- Tạo và chạy phpMyAdmin (port 8081)

3. **Kiểm tra ứng dụng**
- API Server: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- phpMyAdmin: http://localhost:8081

## ⚙️ Cấu hình

### Application Properties

Các cấu hình quan trọng trong `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/green_trade
spring.datasource.username=root
spring.datasource.password=your_password

# JWT
spring.application.secret=your-secret-key
spring.application.expireAt=1000000

# Cloudinary (Image Storage)
cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret

# Email
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Payment Gateways
# MoMo và VnPay config...

# GHN Shipping
ghn.api-url=https://dev-online-gateway.ghn.vn
ghn.token=your-ghn-token
```

### Environment Variables

Bạn có thể override các cấu hình bằng environment variables khi chạy Docker:

```bash
docker-compose up -e SPRING_DATASOURCE_PASSWORD=your_password
```

## 📚 API Documentation

API documentation được tạo tự động bằng Swagger/OpenAPI 3.

### Truy cập Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

### Các nhóm API chính:

#### 🔐 Authentication (`/api/v1/auth`)
- Đăng ký (Buyer/Seller)
- Đăng nhập
- Refresh token
- Đổi mật khẩu

#### 👤 Buyer APIs (`/api/v1/buyer`)
- Quản lý profile
- Đặt hàng
- Thanh toán (MoMo, VnPay)
- Follow seller
- Wishlist
- Review sản phẩm
- Chat với seller

#### 🏪 Seller APIs (`/api/v1/seller`)
- Quản lý profile
- Đăng sản phẩm
- Quản lý đơn hàng
- Xem followers
- Quản lý subscription
- Thống kê doanh thu

#### 📦 Product APIs (`/api/v1/post-product`)
- Danh sách sản phẩm (pagination)
- Chi tiết sản phẩm
- Tìm kiếm sản phẩm
- Lọc theo category
- Sản phẩm theo seller

#### 🛒 Order APIs (`/api/v1/order`)
- Tạo đơn hàng
- Chi tiết đơn hàng
- Cập nhật trạng thái
- Hủy đơn hàng
- Invoice

#### 💬 Chat APIs (`/api/v1/chatting`)
- Danh sách conversation
- Gửi tin nhắn (text/image)
- Lịch sử chat
- WebSocket endpoint

#### 👨‍💼 Admin APIs (`/api/v1/admin`)
- Quản lý users
- Verify sản phẩm
- Xử lý dispute
- Quản lý subscription packages
- Thống kê hệ thống

#### ⚙️ System Config (`/api/v1/system-config`)
- Quản lý bad words/whitelist
- Cấu hình hệ thống

## 📁 Cấu trúc dự án

```
green-trade-platform/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── Green_trade/green_trade_platform/
│   │   │       ├── advisor/          # Global exception handlers
│   │   │       ├── config/           # Configuration classes
│   │   │       ├── controller/       # REST controllers
│   │   │       ├── enumerate/        # Enums
│   │   │       ├── exception/        # Custom exceptions
│   │   │       ├── filter/           # Request filters
│   │   │       ├── mapper/           # DTO mappers
│   │   │       ├── model/            # JPA entities
│   │   │       ├── repository/       # JPA repositories
│   │   │       ├── request/          # Request DTOs
│   │   │       ├── response/         # Response DTOs
│   │   │       ├── service/          # Business logic
│   │   │       ├── task/             # Scheduled tasks
│   │   │       └── util/             # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql             # Initial data
│   └── test/
├── database/
│   └── init.sql                     # Database initialization
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🎯 Chức năng chi tiết

### 1. Quản lý Sản phẩm

#### Đăng sản phẩm
- Upload nhiều hình ảnh (tối đa theo subscription)
- AI hỗ trợ viết mô tả tự động
- Chọn category (Xe điện, Pin điện)
- Định giá và điều kiện sản phẩm

#### Tìm kiếm & Lọc
```http
GET /api/v1/post-product/search?type=brand&value=Yamaha&page=0&size=10
GET /api/v1/post-product/category?name=Xe điện&page=0&size=30
```

#### Xác thực sản phẩm
- Seller gửi yêu cầu verify
- Admin review và approve/reject
- Sản phẩm đã verify được ưu tiên hiển thị

### 2. Hệ thống Đặt hàng & Thanh toán

#### Flow đặt hàng
1. Buyer chọn sản phẩm → Tạo đơn hàng
2. Chọn phương thức thanh toán 
3. Thanh toán 
4. Seller xác nhận đơn hàng (COD)
5. Giao hàng qua GHN
6. Buyer xác nhận nhận hàng
7. Tiền được chuyển từ Escrow → Seller

#### System Wallet (Escrow)
- **ESCROW_HOLD**: Tiền đang giữ
- **IS_SOLVED**: Đơn hàng hoàn thành, tiền đã chuyển
- Bảo vệ cả buyer và seller

### 3. Hệ thống Follow

#### Buyer có thể:
- Follow seller yêu thích
- Xem danh sách seller đang follow
- Unfollow

#### Seller có thể:
- Xem danh sách người đang follow mình
- Thống kê followers

```http
GET /api/v1/seller/followers?page=0&size=10
```

### 4. Dispute Resolution

#### Các loại dispute:
- Sản phẩm không đúng mô tả
- Sản phẩm bị hỏng
- Giao hàng sai
- Và nhiều loại khác...

#### Quy trình:
1. Buyer/Seller tạo dispute
2. Admin review
3. Admin đưa ra quyết định
4. Hoàn tiền hoặc giải quyết

**Lưu ý**: Không thể tạo dispute nếu escrow service đã bị hold.

### 55. Bad Word Filter

- Tự động lọc từ ngữ không phù hợp
- Hỗ trợ whitelist
- Áp dụng cho: title, description, chat messages

## 🔒 Bảo mật

- **JWT Authentication**: Token-based authentication
- **Role-based Access Control**: Phân quyền theo vai trò
- **Password Encryption**: BCrypt password hashing
- **Input Validation**: Validate tất cả input từ client
- **SQL Injection Protection**: Sử dụng JPA/Hibernate
- **CORS Configuration**: Cấu hình CORS phù hợp

## 📊 Database Schema

### Các bảng chính:
- `buyer` - Người mua
- `seller` - Người bán
- `admin` - Quản trị viên
- `post_product` - Sản phẩm
- `category` - Danh mục
- `order` - Đơn hàng
- `payment` - Thanh toán
- `system_wallet` - Ví ký gửi
- `message` - Tin nhắn
- `conversation` - Cuộc hội thoại
- `following` - Follow seller
- `wish_listing` - Wishlist
- `review` - Đánh giá
- `dispute` - Tranh chấp
- `subscription` - Gói đăng ký

## 📝 Notes

### Timezone
Hệ thống sử dụng timezone: `Asia/Ho_Chi_Minh` (UTC+7)

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request


## 👨‍💻 Authors

- **Green Trade Team**

## 🙏 Acknowledgments

- Spring Boot community
- All third-party service providers (Cloudinary, GHN, MoMo, VnPay, Gemini AI)

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: 2024

