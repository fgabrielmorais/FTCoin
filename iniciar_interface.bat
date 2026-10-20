@echo off
chcp 65001 > nul
title FTCoin Studio - Inicializando Interface Grafica

set "JAVA_BIN=C:\Program Files\Java\zulu21.44.17-ca-jdk21.0.8-win_x64\bin\java.exe"

if not exist "%JAVA_BIN%" (
    if defined JAVA_HOME (
        set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
    ) else (
        set "JAVA_BIN=java"
    )
)

echo ========================================================
echo   Iniciando FTCoin Studio - GUI (Java 25 LTS)
echo ========================================================
echo.

"%JAVA_BIN%" -cp "FTCoin-main\bin;FTCoin-main\src" br.com.ftcoin.gui.main.FTCoinGuiApp

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Ocorreu um erro ao inicializar a interface.
    pause
)
