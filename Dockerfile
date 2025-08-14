# ---- Runtime image (작고 빠름)
FROM eclipse-temurin:17-jre

WORKDIR /app

# 로컬에서 빌드된 JAR을 이미지에 복사 (아래 JAR 파일명은 실제 파일명으로 교체)
COPY build/libs/*.jar app.jar

# 운영 포트
ENV SERVER_PORT=8080
EXPOSE 8080

# (선택) JVM 옵션: 컨테이너에 맞는 메모리 및 GC
ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75"

# Spring 프로파일: 기본 prod
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --server.port=${SERVER_PORT}"]