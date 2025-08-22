FROM eclipse-temurin:21.0.8_9-jdk-jammy
WORKDIR /deployments
COPY target/kingu-*-SNAPSHOT.jar kingu.jar
RUN addgroup appuser && adduser --disabled-password appuser --ingroup appuser
RUN mkdir -p /deployments/data && chown -R appuser:appuser /deployments/data
USER appuser
CMD  [ "java", "-jar", "kingu.jar"]
