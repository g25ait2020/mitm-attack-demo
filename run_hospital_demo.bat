@echo off
REM Hospital MITM Attack Demo - Run All Components
REM This script starts the server, MITM attacker, and hospital client GUI

echo ========================================
echo HOSPITAL MITM ATTACK DEMO
echo ========================================
echo.
echo This demo will start:
echo 1. Hospital Server (port 8080)
echo 2. MITM Attacker Proxy (port 8081)
echo 3. Hospital Client GUI
echo.
echo INSTRUCTIONS:
echo - In the Hospital Client GUI, connect to port 8081 (MITM attacker)
echo - Login with: alice / password123
echo - Enter patient health information
echo - Watch the MITM attacker console steal the health data!
echo.
echo To test with encryption:
echo - Check the "Enable Encryption" box before logging in
echo - The attacker will NOT be able to read encrypted data
echo.
pause

REM Check if JAR exists, if not try to build
if not exist "target\mitm-attack-demo-1.0.0.jar" (
    echo JAR file not found. Building project...
    call mvn package -DskipTests -q
    if errorlevel 1 (
        echo Build failed! Please check for errors.
        echo.
        echo If you get "Failed to delete" errors, close any running Java processes and try again.
        pause
        exit /b 1
    )
) else (
    echo Using existing JAR file...
    echo If you made code changes, run: mvn compile
)

REM Start Server in new window
echo Starting Hospital Server...
start "Hospital Server" cmd /k "java -cp target/mitm-attack-demo-1.0.0.jar com.mitm.server.Server"
timeout /t 2 /nobreak >nul

REM Start MITM Attacker in new window
echo Starting MITM Attacker Proxy...
start "MITM Attacker" cmd /k "java -cp target/mitm-attack-demo-1.0.0.jar com.mitm.attacker.MITMProxy"
timeout /t 2 /nobreak >nul

REM Start Hospital Client GUI in new window
echo Starting Hospital Client GUI...
start "Hospital Client GUI" cmd /k "java -cp target/mitm-attack-demo-1.0.0.jar com.mitm.client.HospitalClientGUI localhost 8081"

echo.
echo ========================================
echo All components started!
echo ========================================
echo.
echo Windows opened:
echo 1. Hospital Server (port 8080)
echo 2. MITM Attacker Proxy (port 8081) - intercepting traffic
echo 3. Hospital Client GUI - connect to port 8081
echo.
echo Close this window or press Ctrl+C to stop monitoring.
echo Close individual windows to stop each component.
echo.
pause
