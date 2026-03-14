FROM maven:3.10.1-jdk-21

WORKDIR /app

COPY . /app

# Build the application (skip tests for faster image builds)
RUN mvn -B -DskipTests package

EXPOSE 8080

# Run the packaged jar
CMD ["java", "-jar", "target/coles-0.0.1-SNAPSHOT.jar"]
