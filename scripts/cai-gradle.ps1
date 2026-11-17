#Requires -Version 5.1
# Cai Gradle 8.8 vao tools\gradle (dung khi shim gradlew.bat bao chua co Gradle).
$ErrorActionPreference = "Stop"
$zip = "$env:TEMP\gradle-8.8-bin.zip"
$url = "https://services.gradle.org/distributions/gradle-8.8-bin.zip"
if (!(Test-Path $zip)) { Invoke-WebRequest -Uri $url -OutFile $zip }
Expand-Archive -Path $zip -DestinationPath (Join-Path $PSScriptRoot "..\tools\gradle") -Force
Write-Host "Xong. Chay .\gradlew.bat --version de kiem tra."
