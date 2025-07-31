# 1단계: Base image
FROM openjdk:17

# 2단계: JAR 파일 복사
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar


# 3단계: 앱 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
