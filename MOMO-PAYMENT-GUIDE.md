# 🚀 HƯỚNG DẪN TEST THANH TOÁN MOMO SANDBOX

## 📝 Tổng quan

Chức năng thanh toán MoMo đã được tích hợp vào hệ thống với đầy đủ các tính năng:

- Tạo payment URL từ MoMo API
- Nhận IPN callback khi thanh toán thành công/thất bại
- Cập nhật trạng thái đơn hàng tự động
- Lưu trữ thông tin giao dịch

## 📋 Danh sách file đã tạo/chỉnh sửa

### ✅ Files mới tạo:

1. **Config**

   - `src/main/java/com/siupo/restaurant/config/MomoConfig.java`

2. **DTOs**

   - `src/main/java/com/siupo/restaurant/dto/request/MomoPaymentRequest.java`
   - `src/main/java/com/siupo/restaurant/dto/request/MomoIpnRequest.java`
   - `src/main/java/com/siupo/restaurant/dto/response/MomoPaymentResponse.java`

3. **Service Layer**

   - `src/main/java/com/siupo/restaurant/service/payment/MomoPaymentService.java`
   - `src/main/java/com/siupo/restaurant/service/payment/MomoPaymentServiceImpl.java`

4. **Controller**
   - `src/main/java/com/siupo/restaurant/controller/MomoPaymentController.java`

### 🔧 Files đã chỉnh sửa:

1. `pom.xml` - Thêm dependency `commons-codec` cho HMAC SHA256
2. `src/main/resources/application-dev.properties` - Thêm cấu hình MoMo
3. `src/main/java/com/siupo/restaurant/dto/response/CreateOrderResponse.java` - Thêm field `payUrl`
4. `src/main/java/com/siupo/restaurant/service/order/OrderServiceImpl.java` - Tích hợp MomoPaymentService

---

## 🔑 Cấu hình MoMo Sandbox

Các thông số đã được cấu hình trong `application-dev.properties`:

```properties
momo.partner-code=MOMO
momo.access-key=F8BBA842ECF85
momo.secret-key=K951B6PE1waDMi640xX08PD3vg6EkVlz
momo.endpoint=https://test-payment.momo.vn/v2/gateway/api/create
momo.redirect-url=http://localhost:3000/payment/momo/return
momo.ipn-url=https://YOUR_NGROK_URL/api/payment/momo/ipn
```

⚠️ **LƯU Ý:** Bạn cần thay `YOUR_NGROK_URL` bằng URL ngrok thực tế của bạn để nhận IPN callback.

---

## 🧪 FLOW TEST THANH TOÁN MOMO

### Bước 1: Chuẩn bị môi trường

1. **Cài đặt ngrok** (để expose localhost ra internet nhận IPN):

   ```bash
   # Download tại: https://ngrok.com/download
   # Chạy ngrok:

   ```

2. **Cập nhật IPN URL**:

   - Copy URL từ ngrok (ví dụ: `https://abc123.ngrok-free.app`)
   - Cập nhật trong `application-dev.properties`:
     ```properties
     momo.ipn-url=https://abc123.ngrok-free.app/api/payment/momo/ipn
     ```

3. **Khởi động Spring Boot**:
   ```bash
   mvn spring-boot:run
   ```

### Bước 2: Tạo đơn hàng với MoMo

**Request:**

```http
POST http://localhost:8080/api/orders
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "shippingAddress": {
    "name": "Nguyễn Văn A",
    "phone": "0123456789",
    "street": "123 Đường ABC",
    "ward": "Phường 1",
    "district": "Quận 1",
    "city": "TP.HCM"
  },
  "paymentMethod": "MOMO",
  "items": [
    {
      "product": {
        "id": 1
      },
      "quantity": 2
    }
  ]
}
```

**Response thành công:**

```json
{
  "code": "200",
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "orderId": 123,
    "status": "WAITING_FOR_PAYMENT",
    "paymentMethod": "MOMO",
    "totalPrice": 100.0,
    "vat": 9.09,
    "shippingFee": 0.0,
    "payUrl": "https://test-payment.momo.vn/pay/app/...",
    "items": [...]
  }
}
```

### Bước 3: Thanh toán trên MoMo

1. **Copy `payUrl`** từ response
2. **Mở trong trình duyệt** hoặc ứng dụng MoMo
3. **Đăng nhập MoMo test account**:
   - SĐT: `0963181714`
   - OTP: `123456` (mã test cố định)
4. **Xác nhận thanh toán**

### Bước 4: Kiểm tra IPN callback

Sau khi thanh toán, MoMo sẽ gọi callback đến:

```
POST https://YOUR_NGROK_URL/api/payment/momo/ipn
```

**Log trong console sẽ hiển thị:**

```
INFO  - Received MoMo IPN callback for orderId: ORDER_123
DEBUG - IPN Request: MomoIpnRequest(...)
INFO  - MoMo payment successful for Order #123
```

### Bước 5: Kiểm tra kết quả

1. **Kiểm tra trạng thái đơn hàng:**

   ```http
   GET http://localhost:8080/api/orders/123
   Authorization: Bearer YOUR_JWT_TOKEN
   ```

2. **Kết quả mong đợi:**
   - `order.status` = `CONFIRMED` (nếu thanh toán thành công)
   - `payment.status` = `PAID`
   - `payment.transactionId` = ID giao dịch MoMo
   - `payment.resultCode` = 0

---

## 🧪 Test với Postman

### Test IPN endpoint (không cần ngrok):

```http
POST http://localhost:8080/api/payment/momo/ipn
Content-Type: application/json

{
  "partnerCode": "MOMO",
  "orderId": "ORDER_1",
  "requestId": "1234567890",
  "amount": 100000,
  "orderInfo": "Test payment",
  "orderType": "momo_wallet",
  "transId": 9876543210,
  "resultCode": 0,
  "message": "Successful.",
  "payType": "qr",
  "responseTime": 1699999999999,
  "extraData": "",
  "signature": "YOUR_CALCULATED_SIGNATURE"
}
```

### Test endpoint health check:

```http
GET http://localhost:8080/api/payment/momo/ipn/test
```

**Response:**

```json
{
  "code": "200",
  "success": true,
  "message": "MoMo IPN endpoint is working",
  "data": "OK"
}
```

---

## 📊 Luồng xử lý chi tiết

```
1. User tạo đơn hàng với paymentMethod = MOMO
   ↓
2. Backend tạo Order với status = WAITING_FOR_PAYMENT
   ↓
3. Backend tạo MomoPayment với status = PROCESSING
   ↓
4. Backend gọi MoMo API tạo payment
   ↓
5. MoMo trả về payUrl
   ↓
6. Frontend redirect user đến payUrl
   ↓
7. User thanh toán trên MoMo
   ↓
8. MoMo gọi IPN callback đến backend
   ↓
9. Backend verify signature
   ↓
10. Backend cập nhật:
    - Order.status = CONFIRMED
    - Payment.status = PAID
    - Payment.transactionId = transId từ MoMo
```

---

## 🔍 Debug & Logging

Để xem chi tiết log, thêm vào `application-dev.properties`:

```properties
logging.level.com.siupo.restaurant.service.payment=DEBUG
logging.level.com.siupo.restaurant.controller.MomoPaymentController=DEBUG
```

**Log debug sẽ hiển thị:**

- Raw signature string
- Generated signature
- Request/Response body từ MoMo
- IPN callback data

---

## ⚠️ Xử lý lỗi thường gặp

### 1. Lỗi signature không hợp lệ

```
ERROR - Invalid signature from MoMo IPN
```

**Giải pháp:** Kiểm tra `secretKey` trong config có đúng không.

### 2. Lỗi không nhận được IPN

```
# Không có log IPN trong console
```

**Giải pháp:**

- Kiểm tra ngrok đang chạy
- Kiểm tra `ipn-url` trong config
- Kiểm tra firewall

### 3. Lỗi không tìm thấy đơn hàng

```
BadRequestException: Không tìm thấy đơn hàng
```

**Giải pháp:** Kiểm tra `orderId` trong IPN có đúng format `ORDER_{id}`.

---

## 🎯 Kiểm tra chức năng COD vẫn hoạt động

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "shippingAddress": {...},
  "paymentMethod": "COD",
  "items": [...]
}
```

**Kết quả:**

- `payUrl` = `null`
- `order.status` = `CONFIRMED` (ngay lập tức)
- `payment.status` = `PAID`
- `payment.paymentMethod` = `COD`

---

## ✅ Checklist hoàn thành

- [x] Dependency `commons-codec` đã thêm vào pom.xml
- [x] Cấu hình MoMo trong application-dev.properties
- [x] MomoConfig class đọc config thành công
- [x] DTOs cho request/response MoMo
- [x] MomoPaymentService tạo payment URL
- [x] MomoPaymentController nhận IPN callback
- [x] OrderService tích hợp MomoPaymentService
- [x] CreateOrderResponse có field payUrl
- [x] Flow COD vẫn hoạt động bình thường
- [x] Logging đầy đủ cho debug

---

## 📚 Tài liệu tham khảo

- MoMo API Documentation: https://developers.momo.vn/
- MoMo Sandbox Environment: https://test-payment.momo.vn/
- Test credentials: Xem trong file docs của MoMo

---

## 🚀 Production Deployment

Khi deploy production, cần:

1. Thay đổi endpoint sang production:

   ```properties
   momo.endpoint=https://payment.momo.vn/v2/gateway/api/create
   ```

2. Sử dụng production credentials (không phải sandbox)

3. Cập nhật `redirect-url` và `ipn-url` thành domain thực

4. Tắt debug logging:
   ```properties
   logging.level.com.siupo.restaurant.service.payment=INFO
   ```

---

**🎉 HOÀN THÀNH! Code đã sẵn sàng để test MoMo sandbox.**
