FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && apt-get upgrade -y && apt-get clean && rm -rf /var/lib/apt/lists/* \
    && groupadd -g 2000 appuser && useradd -u 2000 -g appuser -M -s /sbin/nologin appuser

WORKDIR /deployments
RUN mkdir -p /deployments/data && chown -R appuser:appuser /deployments/data
COPY target/kingu-*-SNAPSHOT.jar kingu.jar
USER appuser
CMD  [ "java", "-jar", "kingu.jar"]
