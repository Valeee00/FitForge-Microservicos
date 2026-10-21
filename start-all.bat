@echo off
REM Sobe os dois microsservicos em janelas separadas (8081 e 8082)
call mvn -q -B install -DskipTests
start "treinos-service :8081" java -jar treinos-service\target\app.jar
start "progresso-service :8082" java -jar progresso-service\target\app.jar
