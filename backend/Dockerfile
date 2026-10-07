# ==========================================
# Stage 1: Build
# ==========================================

FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests


# ==========================================
# Stage 2: Runtime
# ==========================================

FROM eclipse-temurin:25-jre

WORKDIR /app
#
#RUN mkdir -p /app/logs

COPY --from=builder /app/target/*.jar app.jar

#RUN useradd --system --no-create-home appuser
#USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]