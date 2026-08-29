# Build Stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/aiinterviewcoach-0.0.1-SNAPSHOT.jar target/aiinterviewcoach-0.0.1-SNAPSHOT.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "target/aiinterviewcoach-0.0.1-SNAPSHOT.jar"]
