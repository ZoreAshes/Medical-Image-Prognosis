$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$JfxLib = "C:\Users\ASUS TUF\Downloads\openjfx-21.0.10_windows-x64_bin-sdk\javafx-sdk-25.0.2\lib"
$MysqlJar = "C:\Users\ASUS TUF\Downloads\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar"

if (-not (Test-Path $JfxLib)) {
    throw "JavaFX SDK not found at $JfxLib"
}

$OutDir = Join-Path $Root "out-demo"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$Sources = Get-ChildItem "src\application\*.java" | ForEach-Object { $_.FullName }
$ModulePath = $JfxLib
$Cp = "$OutDir;$JfxLib\*;$MysqlJar"

javac `
  --module-path $ModulePath `
  --add-modules javafx.controls,javafx.fxml,javafx.swing `
  -cp "$JfxLib\*;$MysqlJar" `
  -d $OutDir `
  @Sources

java `
  --module-path $ModulePath `
  --add-modules javafx.controls,javafx.fxml,javafx.swing `
  -cp "$Cp;src" `
  application.DemoVideoCapture

python -m pip install --quiet opencv-python-headless pillow numpy
python scripts\build_demo_video.py

Write-Host ""
Write-Host "Done. Video:" (Join-Path $Root "demo-video\xpert_linkedin_demo.mp4")
