# 1단계: Base image
FROM openjdk:17

# 2단계: curl 설치 (Ubuntu/Debian 계열)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 3단계: JAR 파일 복사
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# 4단계: 컨테이너 Health Check 설정
# - 5초마다 체크
# - 시작 대기 시간 30초
# - 3초 안에 응답 없으면 실패
# - 30번까지 재시도
HEALTHCHECK --interval=5s --timeout=3s --start-period=30s --retries=30 \
  CMD curl -fsS "http://127.0.0.1:${SERVER_PORT:-8080}${SERVER_SERVLET_CONTEXT_PATH:-}/actuator/health" \
  | grep -q '"status":"UP"' || exit 1

# 5단계: 앱 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
