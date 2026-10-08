@echo off
rem ============================================================================
rem OSCPlay - unattended start for an installation machine.
rem
rem Brings OSCPlay up with a project already loaded, so the proxy is listening
rem and every output is running without anyone touching the UI.
rem
rem Works with either way of having OSCPlay on the machine:
rem   - the Windows installer (OSCPlay.exe, which bundles its own Java), or
rem   - a shaded JAR built from source.
rem The installed OSCPlay.exe wins when both are present.
rem
rem Setup:
rem   1. Edit PROJECT below.
rem   2. Press Win+R, run  shell:startup
rem   3. Put a SHORTCUT to this script in the folder that opens.
rem
rem      With the installer, the script finds OSCPlay.exe on its own and may
rem      live anywhere. Running from a JAR instead, leave the script in the
rem      OSCPlay directory next to target\ and use a shortcut rather than a
rem      copy, since it looks for the JAR relative to its own directory.
rem
rem Run it by hand once before trusting it to a power cycle. A wrong project
rem name does not fall back to the project picker: OSCPlay reports "Project file
rem not found" and the proxy never starts.
rem ============================================================================

rem --- Settings ---------------------------------------------------------------

rem Project to load. Must match a folder in Documents\OSCPlay\Projects.
set PROJECT=interlaceblink

rem Installed launcher. Leave blank to look in the usual install folders, or set
rem it if the installer's folder chooser was pointed somewhere else.
set OSCPLAY_EXE=

rem OSCPlay directory, for a JAR built from source. Defaults to wherever this
rem script lives. Ignored once an installed OSCPlay.exe is found.
if "%OSCPLAY_DIR%"=="" set OSCPLAY_DIR=%~dp0
rem %~dp0 ends in a backslash; a hand-set OSCPLAY_DIR might not.
if not "%OSCPLAY_DIR:~-1%"=="\" set OSCPLAY_DIR=%OSCPLAY_DIR%\

rem Extra options. The MCP server for agents already runs by default on port
rem 7770; pass --no-mcp here to turn it off.
set EXTRA_OPTS=

rem Where to record what happened, so a failed boot can be diagnosed. Kept in
rem the data directory because the install folder is not writable.
set LOG_DIR=%USERPROFILE%\Documents\OSCPlay
set LOG=%LOG_DIR%\startup.log
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%" 2>nul

rem --- Find the installed launcher --------------------------------------------

rem Each candidate is tried on its own line: a parenthesised if block would
rem choke on the brackets in %ProgramFiles(x86)%.
call :candidate "%ProgramFiles%\OSCPlay\OSCPlay.exe"
call :candidate "%ProgramFiles(x86)%\OSCPlay\OSCPlay.exe"
call :candidate "%LOCALAPPDATA%\OSCPlay\OSCPlay.exe"
call :candidate "%OSCPLAY_DIR%OSCPlay.exe"

if "%OSCPLAY_EXE%"=="" goto fromjar

echo [%DATE% %TIME%] Starting "%OSCPLAY_EXE%" with project %PROJECT% >> "%LOG%"
start "" "%OSCPLAY_EXE%" --project "%PROJECT%" %EXTRA_OPTS%
exit /b 0

rem --- Otherwise fall back to a shaded JAR ------------------------------------

:fromjar

cd /d "%OSCPLAY_DIR%" 2>nul
if errorlevel 1 (
    echo [%DATE% %TIME%] Error: no installed OSCPlay.exe, and cannot enter "%OSCPLAY_DIR%" >> "%LOG%"
    exit /b 1
)

rem Match the JAR with a glob; the version lives in pom.xml, not in here.
set JAR_FILE=
for %%f in (target\osc-play-*-shaded.jar) do set JAR_FILE=%%f
if "%JAR_FILE%"=="" for %%f in (osc-play-*-shaded.jar) do set JAR_FILE=%%f

if "%JAR_FILE%"=="" (
    echo [%DATE% %TIME%] Error: no installed OSCPlay.exe and no osc-play shaded JAR under "%OSCPLAY_DIR%" >> "%LOG%"
    exit /b 1
)

echo [%DATE% %TIME%] Starting %JAR_FILE% with project %PROJECT% >> "%LOG%"

rem javaw, not java, so no console window stays open for the life of the show.
rem JavaFX is bundled in the shaded JAR, so JFX_SDK is not needed.
start "" javaw -jar "%JAR_FILE%" --project "%PROJECT%" %EXTRA_OPTS%
exit /b 0

rem --- Helpers ----------------------------------------------------------------

:candidate
if not "%OSCPLAY_EXE%"=="" goto :eof
if exist %1 set OSCPLAY_EXE=%~1
goto :eof
