# ⚙️ Finance Tracker - Backend API (Spring Boot)

The core engine of the Moneymanager application, featuring **AI-driven data extraction**, secure RESTful APIs, and robust financial management logic.

## 🚀 Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3+
- **Security:** Spring Security (JWT & OAuth2)
- **Database:** MySQL / PostgreSQL
- **Build Tool:** Maven
- **AI Processing:** Integration with OCR services (Ollama/Llava) for data parsing.
- **API Doc:** Swagger/OpenAPI

## ✨ Key Features
- **🧠 AI Image Processing:** API endpoints to receive invoice images and return structured JSON data.
- **🔐 Robust Security:** Stateless JWT authentication and secure OAuth2 provider integration.
- **📁 Excel Reporting:** Export personal financial history to professional Excel reports.
- **📧 Notification System:** Automated email service for monthly budget summaries.
- **🔗 RESTful Services:** Scalable architecture for Transactions, Categories, and User Wallets.

## 🛠️ Setup & Installation
1. **Prerequisites:** JDK 21 and Maven installed.
2. **Database:** Ensure your DB (MySQL/PostgreSQL) is running.
3. **Run Application:**
   ```bash
   mvn clean spring-boot:run

## 🐳 Docker Deployment
```bash
docker build -t finance-tracker-be .
docker run -p 8080:8080 finance-tracker-be