@echo off
setlocal
cd /d "%~dp0"

echo ========================================================
echo Running FastCrypto Benchmark
echo ========================================================

set CP=target\classes;examples\Benchmark\target;%USERPROFILE%\.m2\repository\com\github\andrestubbe\FastCore\0.1.0\FastCore-0.1.0.jar;src\main\resources

if not exist "examples\Benchmark\target" mkdir "examples\Benchmark\target"

javac -cp "%CP%" -d examples\Benchmark\target examples\Benchmark\src\main\java\fastcrypto\benchmark\Benchmark.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Benchmark compilation failed.
    exit /b %ERRORLEVEL%
)

java -cp "%CP%" fastcrypto.benchmark.Benchmark
