@echo off
setlocal
cd /d "%~dp0"

echo =======================================================
echo  Compiling fastcrypto.dll via MSVC x64
echo =======================================================

set "VSWHERE=%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe"
if exist "%VSWHERE%" (
    for /f "usebackq tokens=*" %%i in (`"%VSWHERE%" -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
        set "VS_PATH=%%i"
    )
)
if not defined VS_PATH (
    if exist "C:\Program Files\Microsoft Visual Studio\18\Community\VC\Auxiliary\Build\vcvars64.bat" (
        set "VS_PATH=C:\Program Files\Microsoft Visual Studio\18\Community"
    )
)

call "%VS_PATH%\VC\Auxiliary\Build\vcvars64.bat"

set JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1
if not exist "%JAVA_HOME%" (
    set JAVA_HOME=C:\Program Files\Java\graalvm-25.2.4+7.1
)

if not exist "src\main\resources\native" mkdir "src\main\resources\native"

cl.exe /O2 /LD /std:c++17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    src\main\native\fastcrypto.cpp ^
    /Fe:src\main\resources\native\fastcrypto.dll ^
    bcrypt.lib

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] fastcrypto.dll successfully built in src\main\resources\native\
    del *.obj *.exp *.lib 2>nul
) else (
    echo.
    echo [ERROR] Native build failed with error %ERRORLEVEL%
)
