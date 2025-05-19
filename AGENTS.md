# Contributor Guide

## Dev Environment Tips
- Use `./mvnw compile` to quickly compile the project.

## Testing Instructions
- Use `./mvnw verify -Dtestcontainers.enabled=false` command to execute all tests.
- The commit should pass all tests before you merge.
- To verify changes in a single class, execute tests for this class with `./mvnw -Dtest="TestClassName" test -Dtestcontainers.enabled=false` command.
- Fix any test errors until the whole suite is green.
