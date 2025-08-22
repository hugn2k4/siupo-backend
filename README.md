# 🍽️ Supbo Restaurant - Backend

> Dự án Spring Boot REST API cho hệ thống quản lý nhà hàng Supbo.

---

## 🛠️ Công nghệ sử dụng

- **Spring Boot 3**
- **Java 21**
- **Maven Wrapper**
- **Spring Data JPA**
- **Spring Security + JWT**
- **Spring Mail**
- **Lombok**
- **MySQL**
- **Hibernate Validator**
- **JJWT**

## 📋 Yêu cầu hệ thống

- **Java** >= 21 (21)
- **Maven** >= 3.9 (4.0.0)
- **MySQL**

## 🚀 Cài đặt và chạy dự án

### 1. Clone repository

```bash
# Clone về máy
git clone https://github.com/hugn2k4/siupo-backend.git
cd supbo-restaurant/back-end/supbo-restaurant
```

### 2. Cấu hình database & mail

- Copy file `src/main/resources/application-example.properties` thành `application.properties` và sửa thông tin kết nối DB, email.

### 3. Cài đặt dependencies & chạy server

```bash
# Chạy bằng Maven Wrapper
./mvnw spring-boot:run
```

Server mặc định chạy tại: `http://localhost:8080`

## 📁 Cấu trúc thư mục

```
src/
├── main/
│   ├── java/com/supbo/restaurant/
│   │   ├── controller/      # REST API controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # JPA repositories
│   │   ├── model/           # Entity/model
│   │   ├── dto/             # DTO request/response
│   │   ├── security/        # JWT, Security config
│   │   ├── exception/       # Xử lý exception
│   │   └── ...
│   └── resources/
│       ├── application.properties
│       └── ...
└── test/
    └── ...
```

## 🔑 Các thư viện chính

| Thư viện                       | Chức năng             |
| ------------------------------ | --------------------- |
| spring-boot-starter-web        | REST API              |
| spring-boot-starter-data-jpa   | ORM, truy vấn DB      |
| spring-boot-starter-security   | Bảo mật, JWT          |
| spring-boot-starter-mail       | Gửi email xác thực    |
| spring-boot-starter-validation | Validate dữ liệu      |
| jjwt                           | Xử lý JWT             |
| lombok                         | Giảm code boilerplate |
| mysql-connector-j              | Kết nối MySQL         |

## 📝 Các lệnh Maven cơ bản

| Lệnh                     | Mô tả          |
| ------------------------ | -------------- |
| `./mvnw spring-boot:run` | Chạy server    |
| `./mvnw clean package`   | Build project  |
| `./mvnw test`            | Chạy unit test |

## 🌿 Quy trình làm việc với Git & Workflow nhóm

### Cấu trúc nhánh

```
main              # Nhánh chính (production)
├── dev           # Nhánh phát triển
├── feature/*     # Nhánh tính năng
├── bugfix/*      # Nhánh sửa bug
├── hotfix/*      # Nhánh sửa lỗi khẩn cấp
├── <tên-thành-viên>  # Nhánh cá nhân (nếu muốn tách biệt)
```

### Quy tắc đặt tên nhánh

- **Feature**: `feature/ten-tinh-nang` (ví dụ: `feature/user-authentication`)
- **Bugfix**: `bugfix/ten-loi`
- **Hotfix**: `hotfix/ten-loi-khan-cap`
- **Cá nhân**: `yourname` (ví dụ: `hung`, `minh`, `kimanh`...)

### Cách làm việc

Mỗi thành viên có thể code ở:

- Nhánh cá nhân (tên mình): phù hợp khi làm nhiều task nhỏ, thử nghiệm, hoặc muốn tách biệt hoàn toàn với các thành viên khác.
- Nhánh chức năng (feature/ten-chuc-nang): phù hợp khi làm task lớn, làm việc nhóm nhỏ hoặc khi leader giao task cụ thể.

> Tùy vào quy mô và tính chất công việc, leader sẽ phân công rõ nên code ở nhánh cá nhân hay nhánh chức năng. Khi hoàn thành, luôn tạo Pull Request về nhánh `dev` để review và hợp nhất code.

### Ví dụ workflow

#### 1. Bắt đầu task mới (theo nhánh cá nhân hoặc nhánh chức năng)

```bash
# Checkout nhánh dev
git checkout dev
git pull origin dev

# Tạo nhánh mới (cá nhân hoặc chức năng)
git checkout -b feature/ten-tinh-nang
# hoặc
git checkout -b <ten-ban>

# Làm việc và commit
git add .
git commit -m "feat: thêm tính năng mới"
```

#### 2. Commit message convention

```bash
# Format: <type>: <description>
feat: thêm tính năng đăng nhập
fix: sửa lỗi responsive navbar
docs: cập nhật README
style: format code với prettier
refactor: tối ưu component Header
test: thêm unit test cho utils
```

#### 3. Push và tạo Pull Request

```bash
# Push nhánh lên remote
git push origin feature/ten-tinh-nang
# hoặc
git push origin <ten-ban>

# Tạo Pull Request từ nhánh đang làm -> dev
# Review code → Merge → Xóa nhánh nếu muốn
```

#### 4. Sync với nhánh chính

```bash
# Cập nhật dev thường xuyên
git checkout dev
git pull origin dev

# Rebase nhánh đang làm (nếu cần)
git checkout feature/ten-tinh-nang
git rebase dev
# hoặc
git checkout <ten-ban>
git rebase dev
```

### Các lệnh Git hữu ích

| Lệnh                                | Mô tả                       |
| ----------------------------------- | --------------------------- |
| `git status`                        | Kiểm tra trạng thái file    |
| `git log --oneline`                 | Xem lịch sử commit ngắn gọn |
| `git branch -a`                     | Xem tất cả nhánh            |
| `git checkout -b <branch>`          | Tạo và chuyển nhánh mới     |
| `git branch -d <branch>`            | Xóa nhánh local             |
| `git push origin --delete <branch>` | Xóa nhánh remote            |

### Quy tắc làm việc nhóm

1. **Không push trực tiếp lên main/dev**
2. **Luôn tạo Pull Request để review code**
3. **Commit thường xuyên với message rõ ràng**
4. **Pull dev trước khi tạo branch mới**
5. **Kiểm tra conflict trước khi merge**
