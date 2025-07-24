#!/bin/bash

set -e
set +x

cd "$(dirname "$0")"
mvn package -D skipTests --no-transfer-progress
java -jar target/test-spring-boot-starter*.jar --async
