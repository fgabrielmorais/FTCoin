@echo off
setlocal
chcp 65001 > nul
title FTCoin Studio - Inicializando Interface Grafica

if defined JAVA_HOME (
    set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_BIN=java"
)

if defined MAVEN_HOME (
    set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
) else (
    set "MAVEN_CMD=mvn.cmd"
)

echo ========================================================
echo   FTCoin Studio - GUI - Projeto configurado para Java 21
echo ========================================================
echo.

"%JAVA_BIN%" -version > nul 2>&1
if errorlevel 1 (
    echo Java nao encontrado. Configure JAVA_HOME ou o PATH.
    goto :erro
)

call "%MAVEN_CMD%" -version > nul 2>&1
if errorlevel 1 (
    echo Maven nao encontrado. Configure MAVEN_HOME ou o PATH.
    goto :erro
)

pushd "%~dp0FTCoin-main"
if errorlevel 1 (
    echo Nao foi possivel abrir a pasta FTCoin-main.
    goto :erro
)

echo Compilando a interface e preparando as dependencias...
echo.

call "%MAVEN_CMD%" compile dependency:copy-dependencies "-DincludeScope=runtime"
if errorlevel 1 (
    popd
    echo.
    echo A compilacao ou a preparacao das dependencias falhou.
    goto :erro
)

echo.
echo Iniciando a interface...
echo.

"%JAVA_BIN%" -cp "target\classes;target\dependency\*" br.com.ftcoin.gui.main.FTCoinGuiApp
set "RESULTADO=%ERRORLEVEL%"

popd

if not "%RESULTADO%"=="0" (
    echo.
    echo Ocorreu um erro ao executar a interface.
    goto :erro
)

endlocal
exit /b 0

:erro
echo.
pause
endlocal
exit /b 1