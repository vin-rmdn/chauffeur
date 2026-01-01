FROM gradle:8-jdk AS build
WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY app/build.gradle.kts settings.gradle.kts ./

RUN ./gradlew dependencies --no-daemon

COPY app/src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
