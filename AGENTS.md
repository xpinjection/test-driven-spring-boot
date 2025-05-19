# Contributor Guide

## Dev Environment Tips
- Use ./mvnw compile to quickly compile project.

## Testing Instructions
- Use ./mvnw verify to execute all tests.
- The commit should pass all tests before you merge.
- To focus on a single class execute tests for this class: ./mvnw -Dtest="TestClassName" test.
- Fix any test errors until the whole suite is green.
