FROM maven:latest as builder
COPY . /app
WORKDIR /app
RUN mvn install

FROM openjdk:11-jre-slim
COPY --from=builder /app/target/bot4future-*-jar-with-dependencies.jar /app/bot.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","/app/bot.jar"]