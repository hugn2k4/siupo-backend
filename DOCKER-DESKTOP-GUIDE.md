# 🐳 Chạy với Docker Desktop - Hướng dẫn nhanh

## Bước 1: Mở Docker Desktop

1. Khởi động **Docker Desktop** từ Start Menu
2. Đợi Docker khởi động xong (icon Docker màu xanh ở system tray)

## Bước 2: Chạy project

### Cách 1: Dùng Docker Desktop UI (Dễ nhất!)

1. Mở **Docker Desktop**
2. Vào tab **"Images"** → Click nút **"+" (Import/Build)**
3. Chọn thư mục project này
4. Hoặc đơn giản: Click vào **"Compose"** tab → Chọn `docker-compose.yml`

**HOẶC** dùng PowerShell:

### Cách 2: Dùng PowerShell/Terminal

```powershell
# Trong thư mục project
docker compose up --build
```

Xong! App sẽ chạy ở http://localhost:8080

## Bước 3: Xem trong Docker Desktop

- Vào tab **"Containers"** để xem app đang chạy
- Click vào container `siupo-app-dev` để xem logs
- Click vào `siupo-mysql-dev` để xem MySQL logs

## Bước 4: Dừng app

Trong Docker Desktop:

- Click vào container → Click nút **"Stop"**

Hoặc trong terminal:

```powershell
docker compose down
```

## Test API

```powershell
# PowerShell
Invoke-RestMethod -Uri http://localhost:8080

# hoặc mở browser
start http://localhost:8080
```

---

## Troubleshooting

### Lỗi "Port 8080 already in use"

```powershell
# Tìm và kill process
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### MySQL không kết nối được

Đợi 10-15 giây để MySQL khởi động xong, xem logs trong Docker Desktop.

### Build lỗi

```powershell
# Clean build
docker compose down -v
docker compose up --build
```

---

## Xem file khác:

- `DOCKER-GUIDE.md` - Hướng dẫn đầy đủ
- `.env.example` - Config template cho production
