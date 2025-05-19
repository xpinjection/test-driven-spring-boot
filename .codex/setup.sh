#!/bin/bash
cd ..
chmod +x mvnw
./mvnw clean package -Dtestcontainers.enabled=false
