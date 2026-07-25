FROM maven:3.9.11-eclipse-temurin-11 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:11-jre

WORKDIR /app
COPY --from=build /app/target/criczone-spring-1.0.0.jar app.jar

EXPOSE 8080

CMD ["/opt/java/openjdk/bin/java", "-jar", "app.jar"]
