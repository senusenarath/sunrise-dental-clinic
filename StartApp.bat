@echo off
title Sunrise Dental Clinic - Startup
color 0A

echo.
echo  ============================================
echo   SUNRISE DENTAL CLINIC MANAGEMENT SYSTEM
echo  ============================================
echo.

:: Step 1 - Set Java
set JAVA_HOME=C:\Program Files\Java\jdk-26.0.2
set JRE_HOME=C:\Program Files\Java\jdk-26.0.2

:: Step 2 - Build
echo  [1/3] Building application...
cd /d "D:\ICBT\Advanced Programming\sunrise-dental-clinic"
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo  BUILD FAILED! See errors above.
    pause
    exit /b 1
)
echo  [1/3] Build successful!

:: Step 3 - Stop old Tomcat
echo  [2/3] Restarting Tomcat...
taskkill /IM java.exe /F >nul 2>&1
timeout /t 2 /nobreak >nul

:: Deploy WAR
del "C:\Program Files\Apache Software Foundation\Tomcat 11.0\webapps\sunrise-dental-clinic.war" >nul 2>&1
rmdir /s /q "C:\Program Files\Apache Software Foundation\Tomcat 11.0\webapps\sunrise-dental-clinic" >nul 2>&1
copy "D:\ICBT\Advanced Programming\sunrise-dental-clinic\target\sunrise-dental-clinic.war" "C:\Program Files\Apache Software Foundation\Tomcat 11.0\webapps\"
echo  [2/3] App deployed!

:: Step 4 - Start Tomcat
echo  [3/3] Starting server...
start "" "C:\Program Files\Apache Software Foundation\Tomcat 11.0\bin\startup.bat"
timeout /t 8 /nobreak >nul
echo  [3/3] Server started!

echo.
echo  ============================================
echo   APP IS READY!
echo   http://localhost:8080/sunrise-dental-clinic
echo.
echo   admin        / admin123
echo   receptionist / reception123
echo   dentist      / dentist123
echo  ============================================
echo.

:: Open browser
start "" "http://localhost:8080/sunrise-dental-clinic/login"

echo  Press any key to close this window...
pause >nul