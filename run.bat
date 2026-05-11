@echo off
if exist .env (
    for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
        if not "%%A"=="" if not "%%A:~0,1%"=="#" set "%%A=%%B"
    )
)
java -jar target\message-logger-1.0.0.jar --config config\application.yaml %*
