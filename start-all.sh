#!/usr/bin/env bash
# Sobe os dois microsserviços (8081 e 8082). Ctrl+C encerra os dois.
cd "$(dirname "$0")"
mvn -q -B install -DskipTests
trap 'kill 0' EXIT
java -jar treinos-service/target/app.jar &
java -jar progresso-service/target/app.jar &
wait
