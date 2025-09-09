
# 🍽️ Siupo Restaurant - Backend (Microservice)

> Dự án Spring Boot Microservice REST API cho hệ thống quản lý nhà hàng Siupo.


## 🛠️ Công nghệ sử dụng

- **Spring Boot 3**
- **Java 21**
- **Maven Wrapper**
- **Spring Cloud (Config, Gateway, Discovery)**
- **Spring Data JPA**
- **Spring Security + JWT**
- **Spring Mail**
- **Lombok**
- **MySQL**
- **Hibernate Validator**
- **JJWT**


## 📋 Yêu cầu hệ thống

- **Java** >= 21
- **Maven** >= 3.9
- **MySQL**


```bash
git clone https://github.com/hugn2k4/siupo-backend.git
```


Các service sẽ được thêm dần vào thư mục này, mỗi service là một project Spring Boot riêng biệt.
Hệ thống hỗ trợ cấu hình tập trung qua `config-server`.


## 🔒 Yêu cầu khi làm việc nhóm

**Kiểm tra commit message tự động:**
1. Đảm bảo đã cài Node.js trên máy.
2. Sau khi clone repo, chỉ cần chạy:
   ```bash
   npm install
   ```
   Husky sẽ tự động cài hook kiểm tra commit message.
3. Mỗi lần commit sẽ được kiểm tra tự động, chỉ commit đúng chuẩn mới được chấp nhận.
4. Nếu gặp lỗi, kiểm tra lại các file sau đã có trong repo:
   - `package.json` có script "prepare": "husky install"
   - `commitlint.config.js` cấu hình chuẩn
   - Thư mục `.husky` với file `commit-msg` hook đúng nội dung

**Yêu cầu khác:**
- Tuân thủ quy tắc đặt tên nhánh, commit message, workflow nhóm như hướng dẫn bên dưới.
- Mỗi service nên có README riêng mô tả chức năng, cách chạy, cấu hình.


## 🚀 Build & chạy các service

**Cách 1: Chạy bằng Maven**
Di chuyển vào từng thư mục service và chạy:
```bash
cd <service-folder>
mvn spring-boot:run
```
Ví dụ:
```bash
cd service-gateway
mvn spring-boot:run
```

**Cách 2: Chạy cùng lúc nhiều service bằng IntelliJ**
Trong IntelliJ, chọn từng class khởi động (ví dụ: `ServiceGatewayApplication`, `ServiceRegistryApplication`, ...) và nhấn Run để chạy đồng thời nhiều service.
Bạn cũng có thể tạo nhiều cấu hình Run/Debug để khởi động tất cả service cùng lúc.

Các service sẽ chạy trên các port riêng, ví dụ:
- service-sample: 8888
- service-gateway: 8080
- service-registry: 8761
- Các service khác: 8081, 8082, ...


## 📁 Cấu trúc thư mục dự án

```text
siupo-restaurant/
├── config-server/         # Quản lý cấu hình tập trung
├── service-gateway/       # API Gateway
├── service-registry/      # Service Discovery (Eureka)
├── service-.../           # Các service sẽ thêm sau
├── .env                   # Biến môi trường dùng chung
└── README.md
```


## 🌿 Quy trình làm việc với Git & Workflow nhóm

### Cấu trúc nhánh

```text
main              # Nhánh chính (production)
dev               # Nhánh phát triển
feature/*         # Nhánh tính năng
bugfix/*          # Nhánh sửa bug
hotfix/*          # Nhánh sửa lỗi khẩn cấp
<tên-thành-viên>  # Nhánh cá nhân (ví dụ: hung, minh, kimanh...)
```


#### Ví dụ commit đúng chuẩn:
```text
feat(auth): thêm xác thực bằng Google
fix(order): sửa lỗi load dữ liệu khi reload
docs: cập nhật README
style: format lại code
refactor: tối ưu service Order
test: thêm unit test cho utils
chore: nâng cấp phiên bản maven
```


### Workflow cơ bản
1. Checkout nhánh dev
2. Tạo nhánh mới (feature/..., bugfix/..., hotfix/... hoặc tên cá nhân)
3. Làm việc, commit đúng chuẩn
4. Push lên remote, tạo Pull Request về dev
5. Review code, merge, xóa nhánh nếu muốn
6. Luôn pull dev trước khi tạo nhánh mới hoặc rebase nhánh đang làm


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

Các service sẽ được bổ sung dần, mỗi service là một project Spring Boot riêng biệt.
Mỗi service nên có README riêng mô tả chức năng, cách chạy, cấu hình.


---

**config-server** là một service dùng Spring Cloud Config Server để quản lý cấu hình tập trung cho toàn bộ hệ thống microservice. Thay vì mỗi service tự lưu file cấu hình riêng, tất cả sẽ lấy cấu hình (database, endpoint, biến môi trường, v.v.) từ config-server thông qua HTTP. Điều này giúp dễ dàng thay đổi, cập nhật cấu hình cho nhiều service cùng lúc mà không cần sửa từng service riêng lẻ.

Ví dụ: Khi đổi thông tin kết nối database, chỉ cần cập nhật ở config-server, các service sẽ tự động nhận cấu hình mới.
