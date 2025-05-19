#!/bin/bash
echo "Running setup"
cd ..
chmod +x mvnw
echo "Download Maven dependencies for local execution"
./mvnw clean package -Dtestcontainers.enabled=false
echo "Setup finished successfully"
