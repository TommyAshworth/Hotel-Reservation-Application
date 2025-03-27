
FROM openjdk:17

# Setting the working directory in the container
WORKDIR /app

# Copying the built JAR file into the container
COPY target/D387_sample_code-0.0.2-SNAPSHOT.jar /app/app.jar

# Exposing the application port
EXPOSE 8080

# Running the application

ENTRYPOINT ["java", "-jar", "app.jar"]