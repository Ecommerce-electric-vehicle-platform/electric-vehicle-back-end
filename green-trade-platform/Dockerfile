# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean install -DskipTests -Dproject.build.sourceEncoding=UTF-8

# Stage 2: Run
FROM eclipse-temurin:21-jre

# Set timezone to Vietnam
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Set JVM timezone and run application
ENTRYPOINT ["java", "-Duser.timezone=Asia/Ho_Chi_Minh", "-jar", "app.jar"]
