FROM eclipse-temurin:21-jre-alpine

RUN apk --no-cache upgrade \
    && addgroup -g 2000 appuser && adduser -u 2000 -D -G appuser appuser

WORKDIR /deployments
RUN mkdir -p /deployments/data && chown -R appuser:appuser /deployments/data
COPY target/kingu-*-SNAPSHOT.jar kingu.jar
USER appuser
CMD  [ "java", "-jar", "kingu.jar"]
