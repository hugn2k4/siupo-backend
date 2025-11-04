# 🚀 HƯỚNG DẪN TEST PRODUCTION & DEPLOY

## 📋 MỤC LỤC

1. [Test Production Local](#test-production-local)
2. [Deploy lên VPS](#deploy-lên-vps)
3. [Deploy lên Render.com](#deploy-lên-rendercom)
4. [Deploy lên Railway](#deploy-lên-railway)
5. [Troubleshooting](#troubleshooting)

---

## 1️⃣ TEST PRODUCTION LOCAL

### Bước 1: Chuẩn bị file .env.prod

File `.env.prod` đã được tạo sẵn. **QUAN TRỌNG**: Đổi các giá trị sau:

```bash
# Mở file để chỉnh sửa
notepad .env.prod
```

**Phải đổi:**

- `JWT_SECRET` → Tạo random: `openssl rand -base64 32`
- `MYSQL_ROOT_PASSWORD` → Mật khẩu mạnh
- `APP_DEFAULT_ADMIN_PASSWORD` → Mật khẩu admin mạnh

### Bước 2: Build và chạy production

```powershell
# Stop containers dev (nếu đang chạy)
docker compose down

# Build và chạy production với .env.prod
docker compose -f docker-compose.prod.yml --env-file .env.prod up --build -d

# Xem logs
docker compose -f docker-compose.prod.yml logs -f app
```

### Bước 3: Test API production

```powershell
# Test endpoint
Invoke-RestMethod -Uri http://localhost:8080

# Hoặc curl
curl http://localhost:8080/api/products

# Test login
$body = @{
    email = "admin@siupo.com"
    password = "Admin@ProdSecure2025"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:8080/api/auth/login `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

### Bước 4: Kiểm tra MySQL production

```powershell
# Kết nối vào MySQL container
docker exec -it siupo-mysql-prod mysql -uroot -p

# Nhập password: Prod@Strong123
# Sau đó:
USE siupo_db;
SHOW TABLES;
SELECT * FROM users;
```

### Bước 5: Dừng production test

```powershell
# Stop
docker compose -f docker-compose.prod.yml down

# Stop và xóa data (⚠️ cẩn thận!)
docker compose -f docker-compose.prod.yml down -v
```

---

## 2️⃣ DEPLOY LÊN VPS (Ubuntu/Debian)

### Chuẩn bị VPS:

1. **Mua VPS** (DigitalOcean, Vultr, AWS EC2, Azure VM...)
2. **Spec khuyến nghị**:
   - RAM: 2GB+ (tối thiểu 1GB)
   - CPU: 1-2 cores
   - Disk: 20GB+

### Bước 1: SSH vào VPS

```bash
ssh root@your-vps-ip
# hoặc
ssh ubuntu@your-vps-ip
```

### Bước 2: Install Docker

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose -y

# Verify
docker --version
docker-compose --version
```

### Bước 3: Clone repository

```bash
# Install git nếu chưa có
sudo apt install git -y

# Clone repo
git clone https://github.com/hugn2k4/siupo-backend.git
cd siupo-backend

# Checkout branch cần deploy
git checkout main
```

### Bước 4: Tạo .env cho production

```bash
# Copy template
cp .env.example .env

# Edit file
nano .env
```

**Nhập các giá trị production:**

- Database password
- JWT secret (tạo bằng: `openssl rand -base64 32`)
- Gmail credentials
- Admin password

**Lưu**: `Ctrl+O`, `Enter`, `Ctrl+X`

### Bước 5: Deploy!

```bash
# Build và chạy
docker compose -f docker-compose.prod.yml up -d --build

# Xem logs
docker compose -f docker-compose.prod.yml logs -f

# Kiểm tra containers
docker ps
```

### Bước 6: Setup Nginx Reverse Proxy (Optional nhưng khuyến nghị)

```bash
# Install Nginx
sudo apt install nginx -y

# Tạo config
sudo nano /etc/nginx/sites-available/siupo
```

**Nội dung file:**

```nginx
server {
    listen 80;
    server_name your-domain.com;  # Thay bằng domain của bạn

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Enable site:**

```bash
sudo ln -s /etc/nginx/sites-available/siupo /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### Bước 7: Setup SSL với Let's Encrypt

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx -y

# Get SSL certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal (certbot tự động setup)
sudo certbot renew --dry-run
```

### Bước 8: Setup Firewall

```bash
# UFW firewall
sudo ufw allow 22/tcp      # SSH
sudo ufw allow 80/tcp      # HTTP
sudo ufw allow 443/tcp     # HTTPS
sudo ufw enable

# Check status
sudo ufw status
```

---

## 3️⃣ DEPLOY LÊN RENDER.COM (Dễ nhất!)

### Bước 1: Push code lên GitHub

```powershell
# Đảm bảo code đã commit
git add .
git commit -m "Ready for production"
git push origin main
```

### Bước 2: Tạo account Render

1. Truy cập: https://render.com
2. Sign up với GitHub account

### Bước 3: Deploy Database (PostgreSQL/MySQL)

1. Click **"New +"** → **"PostgreSQL"** (hoặc MySQL)
2. Nhập thông tin:
   - Name: `siupo-db`
   - Database: `siupo_db`
   - User: `siupo_user`
   - Region: Singapore (gần Việt Nam nhất)
   - Instance Type: Free
3. Click **"Create Database"**
4. **Lưu lại**: Internal Database URL và External Database URL

### Bước 4: Deploy Web Service

1. Click **"New +"** → **"Web Service"**
2. Connect GitHub repository: `siupo-backend`
3. Cấu hình:

   - **Name**: `siupo-restaurant`
   - **Region**: Singapore
   - **Branch**: main
   - **Root Directory**: (để trống)
   - **Runtime**: Docker
   - **Instance Type**: Free (hoặc Starter $7/month)

4. **Environment Variables** - Click "Add Environment Variable":

```
SPRING_PROFILES_ACTIVE=prod
PORT=8080
SPRING_DATASOURCE_URL=<Internal Database URL từ bước 3>
SPRING_DATASOURCE_USERNAME=siupo_user
SPRING_DATASOURCE_PASSWORD=<password từ Render>
JWT_SECRET=<random string 32+ chars>
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=<Gmail App Password>
APP_DEFAULT_ADMIN_EMAIL=admin@siupo.com
APP_DEFAULT_ADMIN_PASSWORD=Admin@Secure123
APP_DEFAULT_ADMIN_FULLNAME=Admin
```

5. Click **"Create Web Service"**

### Bước 5: Đợi deploy xong

- Render sẽ tự động:
  - Pull code từ GitHub
  - Build Docker image
  - Deploy app
- Thời gian: 5-10 phút
- URL: `https://siupo-restaurant.onrender.com`

### Bước 6: Test

```powershell
# Test API
Invoke-RestMethod -Uri https://siupo-restaurant.onrender.com
```

---

## 4️⃣ DEPLOY LÊN RAILWAY (Đơn giản, có free tier)

### Bước 1: Tạo account Railway

1. Truy cập: https://railway.app
2. Sign up với GitHub

### Bước 2: New Project

1. Click **"New Project"**
2. Chọn **"Deploy from GitHub repo"**
3. Select repository: `siupo-backend`

### Bước 3: Add Database

1. Click **"New"** → **"Database"** → **"MySQL"**
2. Railway tự động tạo MySQL instance

### Bước 4: Add Environment Variables

Click vào service → **"Variables"** → Add:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=${{MySQL.DATABASE_URL}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQL_USER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQL_PASSWORD}}
JWT_SECRET=your_jwt_secret_here
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
APP_DEFAULT_ADMIN_EMAIL=admin@siupo.com
APP_DEFAULT_ADMIN_PASSWORD=Admin@123
APP_DEFAULT_ADMIN_FULLNAME=Admin
```

### Bước 5: Deploy

- Railway tự động detect Dockerfile và deploy
- URL: Railway sẽ generate (ví dụ: `siupo-restaurant.up.railway.app`)

---

## 5️⃣ PUSH DOCKER IMAGE LÊN DOCKER HUB (Để deploy ở nhiều nơi)

### Bước 1: Login Docker Hub

```powershell
docker login
# Nhập username và password Docker Hub
```

### Bước 2: Tag image

```powershell
# Build production image
docker build -t siupo-restaurant:prod .

# Tag với Docker Hub username
docker tag siupo-restaurant:prod your-dockerhub-username/siupo-restaurant:latest
docker tag siupo-restaurant:prod your-dockerhub-username/siupo-restaurant:1.0.0
```

### Bước 3: Push lên Docker Hub

```powershell
docker push your-dockerhub-username/siupo-restaurant:latest
docker push your-dockerhub-username/siupo-restaurant:1.0.0
```

### Bước 4: Deploy trên VPS bằng image từ Docker Hub

```bash
# Trên VPS
docker pull your-dockerhub-username/siupo-restaurant:latest

docker run -d -p 8080:8080 \
  --name siupo-app \
  --env-file .env \
  your-dockerhub-username/siupo-restaurant:latest
```

---

## 🔧 TROUBLESHOOTING

### App không start / crash

```bash
# Xem logs chi tiết
docker compose -f docker-compose.prod.yml logs app

# Xem logs từ 100 dòng cuối
docker compose -f docker-compose.prod.yml logs --tail=100 app

# Follow logs real-time
docker compose -f docker-compose.prod.yml logs -f app
```

### MySQL connection refused

```bash
# Kiểm tra MySQL đã chạy chưa
docker compose -f docker-compose.prod.yml ps

# Xem MySQL logs
docker compose -f docker-compose.prod.yml logs mysql

# Đợi MySQL healthcheck pass (10-20s)
```

### Out of Memory (OOM)

Giảm JAVA_OPTS trong `.env`:

```
JAVA_OPTS=-Xms128m -Xmx256m
```

### Port already in use

```bash
# Trên Linux
sudo lsof -i :8080
sudo kill -9 <PID>

# Trên Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Rebuild clean

```bash
docker compose -f docker-compose.prod.yml down -v
docker compose -f docker-compose.prod.yml up --build -d
```

---

## 📊 MONITORING & LOGS

### Xem resource usage

```bash
docker stats siupo-app-prod siupo-mysql-prod
```

### Backup database

```bash
# Export database
docker exec siupo-mysql-prod mysqldump -uroot -p siupo_db > backup.sql

# Import database
docker exec -i siupo-mysql-prod mysql -uroot -p siupo_db < backup.sql
```

### Auto-restart khi server reboot

Docker containers đã có `restart: unless-stopped`, sẽ tự động chạy lại khi server reboot.

---

## 🎯 CHECKLIST TRƯỚC KHI DEPLOY PRODUCTION

- [ ] Đổi tất cả passwords mặc định
- [ ] Generate JWT_SECRET ngẫu nhiên (32+ chars)
- [ ] Setup Gmail App Password (không dùng password thường)
- [ ] Test kỹ tất cả API endpoints
- [ ] Setup backup database tự động
- [ ] Setup SSL certificate (HTTPS)
- [ ] Setup firewall
- [ ] Setup monitoring (logs, alerts)
- [ ] Document API endpoints
- [ ] Setup CI/CD (GitHub Actions) - optional
- [ ] Load testing - optional

---

## 🚀 QUICK START (TL;DR)

### Test Production Local:

```powershell
docker compose -f docker-compose.prod.yml --env-file .env.prod up --build -d
```

### Deploy VPS:

```bash
git clone <repo>
cd siupo-backend
cp .env.example .env
nano .env  # chỉnh config
docker compose -f docker-compose.prod.yml up -d --build
```

### Deploy Render:

1. Push lên GitHub
2. Render.com → New Web Service
3. Connect repo → Docker
4. Add environment variables
5. Deploy!

---

**Good luck! 🎉**
