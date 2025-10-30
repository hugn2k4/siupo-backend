# Stage 1: build
# Sử dụng Maven image với JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Tạo user không phải root để chạy ứng dụng
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR đã build từ stage 1
COPY --from=build /app/target/restaurant-0.0.1-SNAPSHOT.jar app.jar

# Biến môi trường mặc định
ENV PORT=8080
ENV JAVA_OPTS=""

EXPOSE 8080

# Khởi chạy Spring Boot với JAVA_OPTS và PORT
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
