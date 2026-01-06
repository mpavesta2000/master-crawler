# Master Crawler

A web crawling application built with Spring Boot for efficiently scraping and processing web content.

## 🚀 Technologies

- Java 21
- Spring Boot 3.5.4
- Spring Data JPA
- Spring JDBC
- Maven
- Persian Date/Time Support

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6.3 or higher
- PostgreSQL (or your preferred database)

## 🛠️ Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/mpavesta2000/master-crawler.git
   cd master-crawler
   ```

2. **Configure the database**
   - Create a new database in PostgreSQL
   - Update `application.properties` or `application.yml` with your database credentials

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/avesta/mastercrawler/
│   │   ├── MasterCrawlerApplication.java  # Main application class
│   │   ├── config/                       # Configuration classes
│   │   ├── controller/                   # REST controllers
│   │   ├── model/                        # Entity classes
│   │   ├── repository/                   # Data access layer
│   │   ├── service/                      # Business logic
│   │   └── util/                         # Utility classes
│   └── resources/
│       ├── application.properties        # Application configuration
│       └── static/                       # Static resources
```

## 🔧 Configuration

Configure your application in `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server Configuration
server.port=8080
```

## 📝 Features

- Web crawling with configurable rules
- Data persistence with Spring Data JPA
- RESTful API endpoints
- Persian date/time support
- Database integration

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
