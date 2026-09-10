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

# curl is what the container health check calls. The base image ships no HTTP client at all.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /build/target/velo-stats-*.jar /app/velo-stats.jar

# The database directory may be empty on a fresh checkout, and every mode migrates on start.
RUN mkdir -p /app/database /app/data

ENV HTTP_PORT=8000 \
    DB_DATABASE=/app/database/database.sqlite \
    RIDES_JSON_PATH=/app/data/rides.json

# A tiny wrapper, so a command reads the same whether the container is being started or exec'd into.
# --enable-native-access is for the SQLite driver, which loads its own native library. Without it the
# JVM warns on every start, and a future release will refuse the call outright.
RUN printf '#!/bin/sh\nexec java --enable-native-access=ALL-UNNAMED -jar /app/velo-stats.jar "$@"\n' > /usr/local/bin/velo \
    && chmod +x /usr/local/bin/velo

EXPOSE 8000

ENTRYPOINT ["velo"]
CMD ["serve"]
