# 🌟 Lumina – Ứng dụng học từ vựng tiếng Anh thông minh

Lumina giúp người dùng tự tạo và quản lý nội dung học theo cấu trúc **Course → Unit → Lesson → Từ vựng**, tự động sinh quiz ôn tập và ứng dụng AI để phân nhóm từ vựng theo chủ đề.

---

## 📱 Tính năng chính (MVP v1.0)

- **Đăng nhập / Đăng ký** – Email & Google OAuth 2.0
- **Quản lý nội dung** – Tạo Course, Unit, Lesson và từ vựng
- **Flashcard + SRS** – Ôn từ với thuật toán Spaced Repetition (SM-2)
- **Quiz tự động** – 2 dạng: trắc nghiệm và điền từ vào câu
- **AI phân nhóm chủ đề** – Tự động gom từ theo ngữ nghĩa bằng Claude API
- **Gamification** – Streak học hằng ngày + điểm XP

---

## 🏗️ Cấu trúc dự án

```
Lumina/
├── android/        # Android app (Kotlin, API 26+)
├── backend/        # Spring Boot REST API
└── docs/           # Tài liệu, SRS, thiết kế
```

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Android | Kotlin, Android Studio, Gradle KTS |
| Backend | Java, Spring Boot |
| Database | PostgreSQL |
| Auth | Google OAuth 2.0, JWT |
| AI | Claude API |
| TTS | Google Text-to-Speech |
| Push Notification | Firebase Cloud Messaging |

---

## 🚀 Hướng dẫn chạy dự án

### Android

1. Mở thư mục `android/` bằng Android Studio
2. Sync Gradle
3. Tạo file `android/local.properties` và thêm đường dẫn SDK:
   ```
   sdk.dir=C:\\Users\\<your_name>\\AppData\\Local\\Android\\Sdk
   ```
4. Chạy trên emulator hoặc thiết bị thật

### Backend

1. Mở thư mục `backend/` bằng IntelliJ IDEA
2. Tạo file cấu hình môi trường (xem `.env.example`)
3. Chạy ứng dụng Spring Boot:
   ```bash
   ./mvnw spring-boot:run
   ```

---

## ⚙️ Biến môi trường

Tạo file `.env` từ file mẫu:

```bash
cp .env.example .env
```

Các biến cần khai báo:

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/lumina
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=your_jwt_secret

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Claude API
CLAUDE_API_KEY=your_claude_api_key
```

> ⚠️ **Không bao giờ commit file `.env` hay `google-services.json` lên repository.**

---

## 📅 Timeline

| Tuần | Mục tiêu |
|---|---|
| 1 | Setup & Foundation |
| 2 | CRUD nội dung |
| 3 | Flashcard & SRS |
| 4 | Quiz & AI |
| 5 | Integration |
| 6 | Gamification |
| 7 | Testing & Bug fix |
| 8 | Buffer & Demo |

---

## 📄 Giấy phép

Dự án nội bộ – chưa phát hành công khai.