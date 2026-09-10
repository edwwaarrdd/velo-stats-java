# Development image: the application jar, built here so no JDK is needed on the host.
#
# The database and the ride export are bind-mounted rather than copied, so data survives a rebuild
# and a new export can be dropped in without one. The jar itself lives in the image, so a code change
# needs a rebuild.

FROM maven:3-eclipse-temurin-25 AS build

WORKDIR /build

# Dependencies are resolved from the pom alone, so editing source code does not invalidate this
# layer.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /build/target/velo-stats-*.jar /app/velo-stats.jar

# The database directory may be empty on a fresh checkout, and every mode migrates on start.
RUN mkdir -p /app/database /app/data

ENV HTTP_PORT=8000 \
    DB_DATABASE=/app/database/database.sqlite \
    RIDES_JSON_PATH=/app/data/rides.json

# A tiny wrapper, so a command reads the same whether the container is being started or exec'd into.
RUN printf '#!/bin/sh\nexec java -jar /app/velo-stats.jar "$@"\n' > /usr/local/bin/velo \
    && chmod +x /usr/local/bin/velo

EXPOSE 8000

ENTRYPOINT ["velo"]
CMD ["serve"]
