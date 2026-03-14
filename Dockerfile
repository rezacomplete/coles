FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . /app

RUN chmod +x ./mvnw && ./mvnw -B -DskipTests package

EXPOSE 8080

CMD ["java", "-jar", "target/coles-0.0.1-SNAPSHOT.jar"]
