# Code Coverage Setup Guide

## Overview

This project now includes **JaCoCo (Java Code Coverage)** for comprehensive test coverage analysis. JaCoCo is integrated into the Maven build process and automatically generates coverage reports when tests are executed.

## Current Status

### Configuration Added
- **Plugin:** JaCoCo Maven Plugin v0.8.12
- **Location:** `pom.xml` lines 295-335
- **Status:** Configured and ready to use

### What Was Configured

The JaCoCo plugin has been configured with three executions:

1. **prepare-agent** - Instruments code during test execution
2. **report** - Generates coverage reports after tests (HTML, XML, CSV)
3. **jacoco-check** - Validates coverage against minimum thresholds during verify phase

### Current Threshold
- **Minimum Line Coverage:** 0% (no enforcement)
- **Purpose:** Baseline configuration to allow build to pass
- **Recommendation:** Increase threshold as test coverage improves

## Usage

### Generate Coverage Report

```bash
# Run tests with coverage
./mvnw clean test

# Full build with coverage and checks
./mvnw clean verify

# Generate report without running tests again
./mvnw jacoco:report
```

### View Coverage Report

After running tests, coverage reports are generated in multiple formats:

**HTML Report (Most User-Friendly):**
```bash
# Location
target/site/jacoco/index.html

# Open in browser
# macOS
open target/site/jacoco/index.html

# Linux
xdg-open target/site/jacoco/index.html

# Windows
start target/site/jacoco/index.html
```

**XML Report (CI/CD Integration):**
```bash
# Location
target/site/jacoco/jacoco.xml

# Useful for SonarQube, Codecov, Coveralls integration
```

**CSV Report (Data Analysis):**
```bash
# Location
target/site/jacoco/jacoco.csv
```

## Coverage Metrics

JaCoCo measures the following coverage metrics:

### 1. **Line Coverage**
- Percentage of code lines executed during tests
- Most common metric for overall coverage

### 2. **Branch Coverage**
- Percentage of decision branches (if/else, switch, loops) executed
- Important for testing conditional logic

### 3. **Instruction Coverage**
- Percentage of Java bytecode instructions executed
- Most granular metric

### 4. **Cyclomatic Complexity**
- Number of independent paths through code
- Higher complexity = more test cases needed

### 5. **Class and Method Coverage**
- Percentage of classes/methods executed
- Helps identify untested components

## Understanding the HTML Report

The HTML report (`target/site/jacoco/index.html`) provides:

1. **Overview Page**
   - Total coverage across all metrics
   - Package-level breakdown
   - Color-coded coverage indicators:
     - Green: Good coverage (typically >80%)
     - Yellow: Moderate coverage (typically 40-80%)
     - Red: Low coverage (typically <40%)

2. **Package Drill-Down**
   - Click any package to see class-level coverage
   - View which classes need more testing

3. **Class Details**
   - Click any class to see source code with coverage highlighting
   - Green: Line executed by tests
   - Red: Line not executed
   - Yellow: Partially covered branch

4. **Coverage Bars**
   - Visual representation of covered vs. missed instructions/branches

## Configuring Coverage Thresholds

### Setting Minimum Coverage Requirements

Edit `pom.xml` at line 327 to enforce minimum coverage:

```xml
<configuration>
    <rules>
        <rule>
            <element>PACKAGE</element>
            <limits>
                <limit>
                    <counter>LINE</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.80</minimum>  <!-- 80% line coverage required -->
                </limit>
                <limit>
                    <counter>BRANCH</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.70</minimum>  <!-- 70% branch coverage required -->
                </limit>
            </limits>
        </rule>
    </rules>
</configuration>
```

### Coverage Levels

Available elements for coverage rules:
- `PACKAGE` - Package-level coverage
- `CLASS` - Class-level coverage
- `METHOD` - Method-level coverage
- `BUNDLE` - Overall project coverage

Available counters:
- `LINE` - Line coverage
- `BRANCH` - Branch coverage
- `INSTRUCTION` - Bytecode instruction coverage
- `COMPLEXITY` - Cyclomatic complexity
- `METHOD` - Method coverage
- `CLASS` - Class coverage

### Build Behavior with Thresholds

When thresholds are set:
- `./mvnw test` - Generates report but doesn't check thresholds
- `./mvnw verify` - Generates report AND validates thresholds
- Build **fails** if coverage is below threshold
- Failure message indicates which packages/classes need more coverage

## Integration with CI/CD

### GitHub Actions

Add to `.github/workflows/maven.yml`:

```yaml
- name: Run tests with coverage
  run: ./mvnw clean verify

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
  with:
    files: ./target/site/jacoco/jacoco.xml
    fail_ci_if_error: true
```

### SonarQube

JaCoCo XML reports are automatically detected by SonarQube:

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=library \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=$SONAR_TOKEN
```

### Codecov / Coveralls

Both services automatically detect and parse `jacoco.xml`:

```yaml
# .codecov.yml
coverage:
  status:
    project:
      default:
        target: 80%
        threshold: 5%
```

## Excluding Code from Coverage

### Exclude Classes/Packages

Edit `pom.xml` to exclude specific code:

```xml
<execution>
    <id>report</id>
    <phase>test</phase>
    <goals>
        <goal>report</goal>
    </goals>
    <configuration>
        <excludes>
            <exclude>**/config/**</exclude>
            <exclude>**/entity/**</exclude>
            <exclude>**/dto/**</exclude>
            <exclude>**/*Application.class</exclude>
        </excludes>
    </configuration>
</execution>
```

### Exclude with Annotations

JaCoCo respects `@Generated` annotation:

```java
@Generated
public class GeneratedCode {
    // This class will be excluded from coverage
}
```

## Current Project Status

### Build Status
⚠️ **Unable to build due to Maven Central connectivity issues**
- Maven Central returned HTTP 503 (Service Unavailable)
- Cannot download dependencies at this time
- JaCoCo plugin configured and ready to use when network access is restored

### Next Steps to Verify Coverage

When network access is available:

```bash
# 1. Build the project
./mvnw clean package

# 2. Run tests with coverage
./mvnw clean test

# 3. View coverage report
open target/site/jacoco/index.html

# 4. Check current coverage percentage
# Look for overall line/branch coverage in the report

# 5. Identify untested code
# Click through packages to find classes with low coverage

# 6. Set appropriate threshold
# Edit pom.xml line 327 based on current coverage
# Recommended: Set threshold 5-10% below current coverage
# Then gradually increase as coverage improves
```

## Best Practices

### 1. **Establish Baseline**
- Run coverage on existing tests
- Note current coverage percentage
- Set threshold slightly below current (e.g., current 75% → threshold 70%)

### 2. **Incremental Improvement**
- Increase threshold by 5-10% every sprint/release
- Focus on critical business logic first
- Don't chase 100% coverage - aim for 80-90%

### 3. **Focus on Meaningful Tests**
- Coverage ≠ Quality
- 80% coverage with good tests > 95% coverage with shallow tests
- Test behavior, not implementation details

### 4. **Exclude Generated Code**
- Configuration classes
- DTOs/Entities (unless they contain business logic)
- Main application class
- Generated code (Lombok, etc.)

### 5. **Monitor Trends**
- Track coverage over time
- CI/CD should enforce minimum thresholds
- Review coverage in code reviews
- Celebrate coverage improvements

## Troubleshooting

### Issue: Coverage Report Not Generated
**Solution:**
```bash
# Ensure tests are running
./mvnw clean test -X  # Debug mode

# Check for JaCoCo agent
# Should see: jacoco-maven-plugin:prepare-agent in output

# Verify report generation
ls -la target/site/jacoco/
```

### Issue: Build Fails Due to Coverage Threshold
**Solution:**
```bash
# Option 1: Add more tests to increase coverage
# Option 2: Temporarily lower threshold in pom.xml
# Option 3: Exclude non-critical packages from coverage
```

### Issue: Coverage Shows 0% Despite Tests Running
**Solution:**
```bash
# Ensure prepare-agent runs before tests
# Check execution order in pom.xml
# Verify no conflicts with other Java agents
```

## Additional Resources

- **JaCoCo Documentation:** https://www.jacoco.org/jacoco/trunk/doc/
- **Maven Plugin Docs:** https://www.jacoco.org/jacoco/trunk/doc/maven.html
- **Coverage Best Practices:** https://www.jacoco.org/jacoco/trunk/doc/implementation.html

## Summary

✅ **Completed:**
- JaCoCo Maven Plugin added to `pom.xml`
- Configured for automatic execution during test/verify phases
- Reports generated in HTML, XML, and CSV formats
- Documentation added to CLAUDE.md
- Coverage threshold configuration ready (currently 0%)

⏳ **Pending (requires network access):**
- Build project and download dependencies
- Run tests to generate initial coverage report
- Determine baseline coverage percentage
- Set appropriate coverage thresholds

📊 **Recommended Next Steps:**
1. Restore network connectivity to Maven Central
2. Build project: `./mvnw clean package`
3. Generate coverage: `./mvnw clean test jacoco:report`
4. Review report: `open target/site/jacoco/index.html`
5. Set realistic threshold based on current coverage
6. Add coverage badge to README.md
7. Integrate with CI/CD pipeline
