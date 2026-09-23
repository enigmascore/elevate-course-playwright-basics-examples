# Multi-stage build: the JDK compiles the jar, only a JRE ships. Nobody needs
# Java on their host - `docker compose build backend` is the whole build.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY mvnw pom.xml ./
COPY .mvn .mvn
# warm the dependency cache in its own layer so source edits rebuild fast
RUN ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8086
# the e2e profile ( fresh database on every start ) is the default; make dev
# overrides it with SPRING_PROFILES_ACTIVE=dev
ENV SPRING_PROFILES_ACTIVE=e2e
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
