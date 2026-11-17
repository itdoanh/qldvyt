#Requires -Version 5.1
# QA screenshot: cài app lên emulator rồi chụp từng màn hình (không cần điện thoại thật).
param(
  [string]$AvdName = "Medium_Phone_API_37.0",
  [string]$ShotDir = (Join-Path $PSScriptRoot "..\tools\qa\screenshots")
)
$ErrorActionPreference = "Stop"
$Sdk = "$env:LOCALAPPDATA\Android\Sdk"
$Adb = Join-Path $Sdk "platform-tools\adb.exe"
$Emu = Join-Path $Sdk "emulator\emulator.exe"

New-Item -ItemType Directory -Force -Path $ShotDir | Out-Null
Write-Host "== QLDVYT QA: kiem tra emulator =="
& $Adb start-server | Out-Null
$devices = & $Adb devices | Out-String
Write-Host $devices
if ($devices -notmatch "emulator-") {
  Write-Host "Chua co emulator, dang khoi dong $AvdName ..."
  Start-Process -FilePath $Emu -ArgumentList "-avd", $AvdName -WindowStyle Minimized
  & $Adb wait-for-device
  Start-Sleep -Seconds 25
}
Write-Host "== Cai APK debug =="
$Apk = Join-Path $PSScriptRoot "..\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $Apk) {
  & $Adb install -r $Apk
} else {
  Write-Warning "Chua co APK ($Apk). Hay chay: .\gradlew.bat :app:assembleDebug truoc."
}
Write-Host "== Mo app =="
& $Adb shell am start -n vn.rinso.qldvyt/.MainActivity | Out-String -Width 200
Start-Sleep -Seconds 4
$Screens = @("01-tong-quan", "02-don-hang", "03-nhiem-vu", "04-nhan-vien", "05-luong", "06-kho", "07-sao-luu")
foreach ($s in $Screens) {
  $out = Join-Path $ShotDir "$s.png"
  & $Adb exec-out screencap -p > $out
  Write-Host "Da chup: $out"
}
Write-Host "== XONG. Xem anh trong $ShotDir + tick tools/qa/checklist-ui.md =="
