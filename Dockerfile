FROM eclipse-temurin:21-jre-noble

RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/* \
    && addgroup --gid 2000 appuser && adduser --uid 2000 --disabled-password --ingroup appuser appuser

WORKDIR /deployments
RUN mkdir -p /deployments/data && chown -R appuser:appuser /deployments/data
COPY target/kingu-*-SNAPSHOT.jar kingu.jar
USER appuser
CMD  [ "java", "-jar", "kingu.jar"]
