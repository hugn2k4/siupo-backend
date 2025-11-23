# Google OAuth2 Login Implementation Guide

## 🎯 Tổng quan

Hệ thống đã được tích hợp đăng nhập bằng Google OAuth2. Người dùng có thể đăng nhập bằng tài khoản Google và hệ thống sẽ tự động tạo tài khoản Customer nếu chưa tồn tại.

## 📋 Các file đã tạo/cập nhật

### 1. OAuth2 Components

- `CustomOAuth2UserService.java` - Xử lý thông tin user từ Google
- `OAuth2AuthenticationSuccessHandler.java` - Xử lý khi đăng nhập thành công
- `OAuth2AuthenticationFailureHandler.java` - Xử lý khi đăng nhập thất bại

### 2. Configuration Files

- `SecurityConfig.java` - Cập nhật để hỗ trợ OAuth2 login
- `application-dev.properties` - Thêm cấu hình Google OAuth2

## 🔧 Hướng dẫn setup

### Bước 1: Tạo Google OAuth2 Credentials

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Vào **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **OAuth 2.0 Client ID**
5. Chọn **Application type**: Web application
6. Thêm **Authorized redirect URIs**:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
7. Copy **Client ID** và **Client Secret**

### Bước 2: Cập nhật application-dev.properties

Mở file `src/main/resources/application-dev.properties` và thay thế:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
```

Bằng Client ID và Client Secret vừa tạo từ Google Cloud Console.

### Bước 3: Cấu hình Frontend Redirect URL

Trong `application-dev.properties`, cập nhật URL frontend của bạn:

```properties
oauth2.frontend.redirect-url=http://localhost:5173/auth/oauth2/callback
```

## 🚀 Cách sử dụng

### Backend Flow

1. **Bắt đầu OAuth2 flow**:

   ```
   GET http://localhost:8080/oauth2/authorization/google
   ```

2. **Sau khi đăng nhập thành công**, user sẽ được redirect về:

   ```
   http://localhost:5173/auth/oauth2/callback?accessToken=xxx&refreshToken=yyy&email=user@gmail.com
   ```

3. **Trong trường hợp lỗi**:
   ```
   http://localhost:5173/auth/oauth2/callback?error=authentication_failed&message=xxx
   ```

### Frontend Implementation

#### 1. Tạo button đăng nhập Google

```jsx
// React/Next.js example
const handleGoogleLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/google";
};

<button onClick={handleGoogleLogin}>
  <img src="/google-icon.svg" alt="Google" />
  Đăng nhập bằng Google
</button>;
```

#### 2. Tạo callback page để nhận token

```jsx
// pages/auth/oauth2/callback.jsx
import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";

export default function OAuth2Callback() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");
    const email = searchParams.get("email");
    const error = searchParams.get("error");

    if (error) {
      // Xử lý lỗi
      console.error("OAuth2 error:", error);
      router.push("/login?error=" + error);
      return;
    }

    if (accessToken && refreshToken) {
      // Lưu tokens vào localStorage hoặc cookie
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);
      localStorage.setItem("userEmail", email);

      // Redirect đến trang chủ
      router.push("/");
    }
  }, [searchParams, router]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto"></div>
        <p className="mt-4">Đang xử lý đăng nhập...</p>
      </div>
    </div>
  );
}
```

#### 3. Sử dụng token để call API

```javascript
// api/axiosConfig.js
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
```

## 🔒 Security Features

- ✅ JWT tokens được tự động tạo sau khi đăng nhập Google thành công
- ✅ User mới sẽ được tạo với role CUSTOMER
- ✅ Password được generate ngẫu nhiên và hash (user không cần biết)
- ✅ Email được verify tự động (vì đã xác thực qua Google)
- ✅ Stateless authentication với JWT

## 📝 Database Changes

Khi user đăng nhập bằng Google lần đầu, hệ thống sẽ tự động tạo:

```java
Customer {
  email: "user@gmail.com",
  fullName: "Google User Name",
  password: "random-hashed-password",
  status: ACTIVE,
  totalSpent: 0.0
}
```

## 🧪 Testing

### 1. Test OAuth2 flow

```bash
# Mở trình duyệt và truy cập:
http://localhost:8080/oauth2/authorization/google
```

### 2. Kiểm tra database

Sau khi đăng nhập thành công, check table `users`:

```sql
SELECT * FROM users WHERE email = 'your-google-email@gmail.com';
```

### 3. Test JWT token

Sử dụng token nhận được để call API:

```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  http://localhost:8080/api/users/me
```

## ⚠️ Lưu ý

1. **Production**: Nhớ thêm production URL vào Authorized redirect URIs trong Google Console
2. **HTTPS**: Trong production, bắt buộc sử dụng HTTPS
3. **CORS**: Đảm bảo frontend URL đã được thêm vào CORS configuration
4. **Refresh Token**: Hiện tại refresh token được truyền qua URL param (không an toàn cho production, nên dùng httpOnly cookie)

## 🐛 Troubleshooting

### Lỗi: "redirect_uri_mismatch"

- Kiểm tra lại redirect URI trong Google Console phải match với backend URL
- Format: `http://localhost:8080/login/oauth2/code/google`

### Lỗi: "Invalid client credentials"

- Kiểm tra lại Client ID và Client Secret trong `application-dev.properties`

### User không được tạo trong database

- Check logs trong console
- Verify MySQL connection
- Kiểm tra `CustomOAuth2UserService` có throw exception không

## 📚 API Endpoints

### OAuth2 Login

```
GET /oauth2/authorization/google
→ Redirect đến Google login page
→ Success: Redirect về frontend với tokens
→ Failure: Redirect về frontend với error
```

### Standard JWT APIs (vẫn hoạt động bình thường)

```
POST /api/auth/login          - Login bằng email/password
POST /api/auth/register       - Đăng ký tài khoản mới
POST /api/auth/refresh-token  - Refresh access token
```

## 🎨 UI/UX Suggestions

1. Thêm divider giữa login form và Google button:

   ```
   ─────── hoặc ───────
   ```

2. Hiển thị loading state khi redirect về từ Google

3. Show error message user-friendly nếu OAuth2 failed

4. Lưu redirect path trước khi đăng nhập để redirect về sau khi thành công
