@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%install_submodule_branch_hooks.ps1" %*
exit /b %ERRORLEVEL%
