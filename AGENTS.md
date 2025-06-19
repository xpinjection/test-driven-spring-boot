# AI Agent Guide

## Dev Environment Tips
- If project dependencies didn't change run all Maven commands with --offline flag to avoid internet usage.
- Use `mvn compile --offline` to quickly compile the project.

## Testing Instructions
- Use `mvn verify -Dtestcontainers.enabled=false --offline` command to execute all tests.
- The commit should pass all tests before you merge.
- To verify changes in a single class, execute tests for this class with `mvn -Dtest="TestClassName" test -Dtestcontainers.enabled=false --offline` command.
- Fix any test errors until the whole suite is green.
