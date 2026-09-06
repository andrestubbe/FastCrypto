@echo off
setlocal
cd /d "%~dp0"

echo ========================================================
echo Running FastCrypto Live Demo
echo ========================================================

set CP=target\classes;examples\Demo\target;%USERPROFILE%\.m2\repository\com\github\andrestubbe\FastCore\0.1.0\FastCore-0.1.0.jar;src\main\resources

if not exist "examples\Demo\target" mkdir "examples\Demo\target"

javac -cp "%CP%" -d examples\Demo\target examples\Demo\src\main\java\fastcrypto\demo\Demo.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Demo compilation failed.
    exit /b %ERRORLEVEL%
)

java -cp "%CP%" fastcrypto.demo.Demo
