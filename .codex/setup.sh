#!/bin/bash
chmod +x mvnw
./mvnw clean package -Dtestcontainers.enabled=false
