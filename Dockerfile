# syntax=docker/dockerfile:1

# ---- Stage 1: build with the full JDK ---------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only the build definition first. Docker caches this layer, so
# dependencies are re-downloaded only when pom.xml changes, not on every code edit.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q -DskipTests package

# ---- Stage 2: run with only the JRE ------------------------------------------
# The final image has no compiler, no Maven, no source code: smaller and a smaller attack surface.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Never run as root inside the container.
RUN useradd --system --uid 10001 app
COPY --from=build /app/target/watchlist-api-*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
