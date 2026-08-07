# syntax=docker/dockerfile:1

FROM eclipse-temurin:24-jdk AS build
WORKDIR /project

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew
RUN ./gradlew --version --no-daemon

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:24-jre
WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin app
COPY --from=build /project/build/libs/*.jar app.jar
USER app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
