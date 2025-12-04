# Stage 1: Build the application
# We use the official Maven image which has 'mvn' installed globally
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy ONLY pom.xml first
COPY pom.xml .

# 2. Download dependencies using global 'mvn' (Not ./mvnw)
# This layer will be cached if pom.xml doesn't change
RUN mvn dependency:go-offline -B

# 3. Copy the full source code
COPY src ./src

# 4. Build the application using global 'mvn'
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM registry.access.redhat.com/ubi9/openjdk-21:1.23

ENV LANGUAGE='en_US:en'

# Copy the built artifacts from the build stage
COPY --from=build --chown=185 /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8085
USER 185

# Configure Java options for Docker environment
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
