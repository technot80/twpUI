@echo off
set CONTROL_FILE=.server-control
set STATUS_FILE=.server-status

:loop
echo running> %STATUS_FILE%
java -jar paper.jar nogui
if exist %CONTROL_FILE% (
  set /p cmd=<%CONTROL_FILE%
  del %CONTROL_FILE%
  if /I "%cmd%"=="stop" (
    echo stopped> %STATUS_FILE%
    exit /b 0
  )
  if /I "%cmd%"=="restart" (
    echo restarting> %STATUS_FILE%
    goto loop
  )
)
ping 127.0.0.1 -n 2 > nul
echo restarting> %STATUS_FILE%
goto loop
