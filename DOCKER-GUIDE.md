# 🐳 Hướng dẫn chạy Docker - Siupo Restaurant

## 📋 Mục lục

- [Yêu cầu](#yêu-cầu)
- [Chạy Local/Dev](#chạy-localdev)
- [Chạy Production](#chạy-production)
- [Các lệnh hữu ích](#các-lệnh-hữu-ích)
- [Troubleshooting](#troubleshooting)

---

## ✅ Yêu cầu

- Docker Desktop (Windows/Mac) hoặc Docker Engine (Linux)
- Docker Compose v2+
- Git

---

## 🔧 Chạy Local/Dev

### Cách 1: Dùng Docker Compose (Khuyến nghị - tự động setup MySQL)

```powershell
# Build và chạy (lần đầu)
docker compose up --build

# Lần sau chỉ cần
docker compose up

# Chạy background
docker compose up -d

# Xem logs
docker compose logs -f app
```

**Truy cập**: http://localhost:8080

### Cách 2: Chạy Docker thủ công (không cần MySQL container)

```powershell
# Build image
docker build -t siupo-restaurant:dev .

# Chạy container (cần MySQL đang chạy ở localhost:3306)
docker run --rm -p 8080:8080 `
  -e "SPRING_PROFILES_ACTIVE=dev" `
  --name siupo-dev `
  siupo-restaurant:dev
```

### Cách 3: Chạy jar trực tiếp (không Docker)

```powershell
# Build jar
.\mvnw.cmd clean package -DskipTests

# Chạy với profile dev
java -Dspring.profiles.active=dev -jar target\siupo-restaurant-0.0.1-SNAPSHOT.jar
```

---

## 🚀 Chạy Production

### Bước 1: Chuẩn bị file .env

```powershell
# Copy template
cp .env.example .env

# Chỉnh sửa các giá trị trong .env
notepad .env
```

**⚠️ QUAN TRỌNG**: Đổi tất cả giá trị mặc định trong `.env`:

- `MYSQL_ROOT_PASSWORD`: mật khẩu MySQL mạnh
- `JWT_SECRET`: chuỗi bí mật dài tối thiểu 256 bit
- `SPRING_MAIL_USERNAME` và `SPRING_MAIL_PASSWORD`: Gmail App Password
- `APP_DEFAULT_ADMIN_PASSWORD`: mật khẩu admin mạnh

### Bước 2: Chạy với Docker Compose Production

```powershell
# Build và chạy production
docker compose -f docker-compose.prod.yml up --build -d

# Kiểm tra trạng thái
docker compose -f docker-compose.prod.yml ps

# Xem logs
docker compose -f docker-compose.prod.yml logs -f app
```

**Truy cập**: http://localhost:8080 (hoặc port bạn đã set trong .env)

### Bước 3: Chạy trên cloud (Render, AWS, DigitalOcean...)

**Trên Render/Railway/Fly.io**:

1. Push code lên GitHub
2. Connect repository với platform
3. Thêm Dockerfile detection (tự động)
4. Set biến môi trường theo `.env.example`
5. Deploy

**Trên VPS (Ubuntu/Debian)**:

```bash
# Clone repo
git clone <your-repo>
cd siupo-restaurant

# Copy và config .env
cp .env.example .env
nano .env

# Chạy production
docker compose -f docker-compose.prod.yml up -d

# Setup Nginx reverse proxy (optional)
# Install certbot cho SSL
```

---

## 🛠️ Các lệnh hữu ích

### Docker Compose

```powershell
# Dừng containers
docker compose down

# Dừng và xóa volumes (⚠️ mất data)
docker compose down -v

# Rebuild image
docker compose build --no-cache

# Xem logs từ 100 dòng cuối
docker compose logs --tail=100 -f app

# Chạy lệnh trong container
docker compose exec app sh
```

### Docker thủ công

```powershell
# List containers
docker ps -a

# Stop container
docker stop siupo-app-dev

# Remove container
docker rm siupo-app-dev

# Xem logs
docker logs -f siupo-app-dev

# Exec vào container
docker exec -it siupo-app-dev sh

# Xóa image
docker rmi siupo-restaurant:dev
```

### Kết nối MySQL trong container

```powershell
# Vào MySQL container
docker compose exec mysql mysql -uroot -p123456 siupo_db

# Hoặc từ host (nếu port 3306 đã expose)
mysql -h127.0.0.1 -uroot -p123456 siupo_db
```

---

## 🐛 Troubleshooting

### 1. Port 8080 đã bị chiếm

```powershell
# Tìm process đang dùng port 8080
netstat -ano | findstr :8080

# Kill process (thay PID)
taskkill /PID <PID> /F

# Hoặc đổi port trong docker-compose.yml
ports:
  - "8081:8080"  # host:container
```

### 2. MySQL connection refused

```powershell
# Kiểm tra MySQL đã chạy chưa
docker compose ps

# Xem logs MySQL
docker compose logs mysql

# Đợi MySQL khởi động xong (healthcheck)
docker compose up -d
docker compose logs -f mysql
# Đợi thấy "ready for connections"
```

### 3. Out of memory (OOM)

Chỉnh `JAVA_OPTS` trong `.env` hoặc docker-compose:

```yaml
environment:
  - JAVA_OPTS=-Xms256m -Xmx512m
```

### 4. Build chậm trên Windows/OneDrive

Docker build trong OneDrive thư mục có thể bị chậm do sync. Giải pháp:

```powershell
# Clone repo ra ngoài OneDrive
cd C:\Projects
git clone <repo>
```

### 5. Xem health check API

```powershell
# Nếu có Spring Actuator
curl http://localhost:8080/actuator/health

# Hoặc PowerShell
Invoke-RestMethod -Uri http://localhost:8080/actuator/health
```

### 6. Lỗi "cannot find siupo-restaurant-0.0.1-SNAPSHOT.jar"

Đảm bảo pom.xml có đúng version và finalName:

```xml
<artifactId>siupo-restaurant</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

Hoặc update Dockerfile COPY path.

---

## 📊 Kiểm tra resource usage

```powershell
# Xem CPU/Memory usage
docker stats

# Chỉ xem app container
docker stats siupo-app-dev
```

---

## 🔒 Security Best Practices

1. **Không commit file `.env`** (đã có trong .gitignore)
2. **Đổi tất cả mật khẩu mặc định** trong production
3. **Dùng secrets management** (Docker Swarm secrets, Kubernetes secrets, AWS Secrets Manager)
4. **Enable SSL/TLS** cho production (dùng Nginx + Let's Encrypt)
5. **Giới hạn resource** cho container:
   ```yaml
   deploy:
     resources:
       limits:
         cpus: "1"
         memory: 1G
   ```

---

## 📧 Hỗ trợ

Nếu gặp vấn đề, kiểm tra:

1. Docker logs: `docker compose logs -f`
2. Application logs trong container
3. MySQL logs: `docker compose logs mysql`
4. Biến môi trường: `docker compose exec app env`

---

**Tác giả**: Siupo Team  
**Cập nhật**: October 2025
