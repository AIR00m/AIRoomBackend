# Debian 계열 (apt 사용 가능)
FROM openjdk:17-slim

# curl 설치
RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends curl tzdata; \
    ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime; \
    echo "Asia/Seoul" > /etc/timezone; \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*


# JAR 복사
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# ✅ HEALTHCHECK 제거!
ENTRYPOINT ["java", "-jar", "/app.jar"]