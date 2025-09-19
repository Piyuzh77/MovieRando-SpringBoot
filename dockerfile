FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
RUN java -Djarmode=layertools -jar /app/target/MovieRando-0.0.1-SNAPSHOT.jar extract --destination /app/extracted


FROM openjdk:17-jdk-slim
WORKDIR /app
EXPOSE 8080
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

COPY --from=build --chown=spring:spring /app/extracted/dependencies ./
COPY --from=build --chown=spring:spring /app/extracted/spring-boot-loader ./
COPY --from=build --chown=spring:spring /app/extracted/snapshot-dependencies ./
COPY --from=build --chown=spring:spring /app/extracted/application ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
