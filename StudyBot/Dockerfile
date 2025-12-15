FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/StudyUrfuBot-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
