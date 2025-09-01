# Windows Batch Files

This project now includes Windows batch file equivalents for all shell scripts to provide cross-platform compatibility.

## Available Scripts

### Test Execution Scripts

| Linux/macOS | Windows | Description |
|-------------|---------|-------------|
| `./run_instrumented_tests.sh` | `run_instrumented_tests.bat` | Main test runner with UTP error handling and fallback strategies |
| `./utp_fix_demo.sh` | `utp_fix_demo.bat` | Interactive demonstration of UTP error solutions |
| `./validate_utp_fix.sh` | `validate_utp_fix.bat` | Validates that UTP fixes are properly configured |
| `./verify_firebase_setup.sh` | `verify_firebase_setup.bat` | Verifies Firebase testing setup and configuration |

## Usage

### On Windows
```cmd
REM Run instrumented tests with full fallback strategy
run_instrumented_tests.bat

REM Show UTP error solutions
utp_fix_demo.bat

REM Validate UTP configuration
validate_utp_fix.bat

REM Verify Firebase setup
verify_firebase_setup.bat
```

### On Linux/macOS
```bash
# Run instrumented tests with full fallback strategy
./run_instrumented_tests.sh

# Show UTP error solutions
./utp_fix_demo.sh

# Validate UTP configuration
./validate_utp_fix.sh

# Verify Firebase setup
./verify_firebase_setup.sh
```

## Gradle Commands

### Windows
```cmd
gradlew connectedDebugAndroidTest
gradlew connectedTestNoUTP
gradlew connectedTestWithFallback
```

### Linux/macOS
```bash
./gradlew connectedDebugAndroidTest
./gradlew connectedTestNoUTP
./gradlew connectedTestWithFallback
```

## Notes

- The batch files provide identical functionality to their shell script counterparts
- All scripts handle UTP (Unified Test Platform) errors and provide fallback strategies
- The batch files use Windows-specific syntax for file paths (`\` instead of `/`) and environment variables (`%VAR%` instead of `$VAR`)
- Both script types support the same command-line arguments and provide the same output