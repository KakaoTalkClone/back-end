#FROM openjdk:17-jdk-slim
FROM eclipse-temurin:17-jdk-jammy

COPY build/libs/piuda-0.0.1-SNAPSHOT.jar app.jar
#ENTRYPOINT ["java", "-jar", "/app.jar"]

# JVM 시간 설정 - 서울 기준 (스케쥴러 작동을 위함)
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Duser.timezone=${TZ}", "-jar", "/app.jar"]
