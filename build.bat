@echo off

set DEBUG_MODE=

if "%1" == "debug" (
  set DEBUG_MODE=debug
)

cd com.cdsoftware.lirion.attendance.targetplatform
call .\plugin-builder.bat %DEBUG_MODE% ..\com.cdsoftware.lirion.attendance ..\com.cdsoftware.lirion.attendance.test
cd ..
