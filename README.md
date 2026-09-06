# JavaMail Application

A Spring Boot mail application with a JSP web UI and REST API. The application uses Spring MVC, Spring Data JPA, PostgreSQL, and Spring Security.

## Technology Stack

- Java 25 LTS
- Spring Boot 3.5.16
- Spring MVC for web and REST controllers
- JSP and JSTL for server-rendered views
- Spring Data JPA with Hibernate
- PostgreSQL
- Spring Security with BCrypt password hashing
- Maven

## Project Structure

```text
Java-Mail-App/
├── pom.xml
├── Dockerfile
├── sql/
│   └── schema.sql                         # Optional PostgreSQL reference schema and seed data
└── src/
    ├── main/
    │   ├── java/com/javamail/
    │   │   ├── JavaMailApplication.java   # Spring Boot entry point
    │   │   ├── config/SecurityConfig.java # Spring Security configuration
    │   │   ├── controller/                # MVC and REST controllers
    │   │   ├── model/                     # JPA entities and domain models
    │   │   ├── repository/                # Spring Data JPA repositories
    │   │   ├── service/                   # Application and authentication services
    │   │   └── util/                      # Small application utilities
    │   ├── resources/
    │   │   └── application.properties    # Server, PostgreSQL, JPA, and upload settings
    │   └── webapp/
    │       ├── css/style.css
    │       ├── js/app.js
    │       └── WEB-INF/                   # JSP views
    └── test/
```

## Requirements

- JDK 25 or later
- Maven 3.8 or later
- PostgreSQL 14 or later

Docker is optional. The provided Dockerfile uses Java 25 for both the Maven build and runtime image.

## PostgreSQL Setup

Create a database and user, for example:

```sql
CREATE DATABASE javamail;
CREATE USER javamail_user WITH PASSWORD 'change-this-password';
GRANT ALL PRIVILEGES ON DATABASE javamail TO javamail_user;
```

The optional [`sql/schema.sql`](sql/schema.sql) file contains a PostgreSQL-compatible reference schema and sample data. In the normal Spring Boot flow, Hibernate creates or updates tables according to the JPA entities because `spring.jpa.hibernate.ddl-auto=update` is configured.

For production, use a migration tool such as Flyway or Liquibase instead of relying on `ddl-auto=update`.

## Configuration

The defaults are suitable for a local PostgreSQL instance. Override them with environment variables when needed:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/javamail"
$env:SPRING_DATASOURCE_USERNAME = "javamail_user"
$env:SPRING_DATASOURCE_PASSWORD = "change-this-password"
$env:PORT = "8081"
```

Equivalent configuration keys are defined in `src/main/resources/application.properties`.

## Run Locally

```bash
mvn spring-boot:run
```

The application starts on [http://localhost:8081](http://localhost:8081). The root route redirects to the login page, and authenticated users are redirected to the mailbox.

## Build and Run the WAR

```bash
mvn clean package
java -jar target/JavaMailApp.war
```

This is an executable Spring Boot WAR. The WAR packaging is required because the application uses JSP views.

## Docker

```bash
docker build -t javamail-app .
docker run --rm -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/javamail \
  -e SPRING_DATASOURCE_USERNAME=javamail_user \
  -e SPRING_DATASOURCE_PASSWORD=change-this-password \
  javamail-app
```

Make sure PostgreSQL is reachable from the container before starting the application.

## Application Features

- User registration and login
- BCrypt password hashing through Spring Security
- Session-based authentication and logout
- Inbox, sent, drafts, starred, important, spam, and trash folders
- Search by subject, body, or sender
- Compose, reply, forward, and save draft flows
- Read/unread, star, important, trash, restore, and spam actions
- Profile updates and password changes
- REST endpoints for authentication, users, and mail operations
- JSP views rendered through Spring MVC

## Main Routes

### Web routes

- `/login`
- `/register`
- `/mailbox`
- `/compose`
- `/viewmail`
- `/profile`

### REST routes

- `/api/auth/**`
- `/api/users/**`
- `/api/mails/**`

The exact request methods and payloads are defined in `AuthViewController`, `UserRestController`, and `MailRestController`.

## Security Notes

- Passwords are encoded with BCrypt; the application does not use the old SHA-256/JDBC password utility.
- PostgreSQL credentials should be supplied through environment variables or a secret manager.
- Protected routes require Spring Security authentication.
- JSP views are under `WEB-INF` and are not directly accessible as public files.

## Validation

Run the test suite with:

```bash
mvn clean test
```
