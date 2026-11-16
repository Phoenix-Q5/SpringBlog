FROM eclipse-temurin:8-jdk AS build
WORKDIR /app
COPY . .
COPY docker/application.properties src/main/resources/application.properties
RUN sh ./mvnw -B -DskipTests package

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/MySpringBlog-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8088
USER 10001:10001
ENTRYPOINT ["java", "-jar", "app.jar"]
