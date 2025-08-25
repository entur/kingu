FROM eclipse-temurin:21-noble

WORKDIR /deployments
COPY target/kingu-*-SNAPSHOT.jar kingu.jar
RUN addgroup --gid 2000 appuser && adduser --uid 2000 --disabled-password --ingroup appuser appuser
RUN mkdir -p /deployments/data && chown -R appuser:appuser /deployments/data
USER appuser
CMD  [ "java", "-jar", "kingu.jar"]
