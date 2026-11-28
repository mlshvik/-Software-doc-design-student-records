# ---------- Етап 1: збірка JAR ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Залежності завантажуються окремим шаром, щоб кешуватися між збірками (з повторами на випадок
# тимчасових обмежень Maven Central).
RUN for attempt in 1 2 3 4; do mvn -B -q dependency:go-offline && break; \
      echo "Спроба $attempt не вдалася, повтор..."; sleep $((attempt * 15)); done
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- Етап 2: мінімальний образ для запуску ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/student-records-*.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
