FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/regenwormen-0.0.1-SNAPSHOT.jar"]
