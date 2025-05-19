#!/bin/bash
echo "Running setup"
chmod +x mvnw
echo "Download Maven dependencies for local execution"
./mvnw clean package -Dtestcontainers.enabled=false
echo "Setup finished successfully"
