# Fabrique les deux APK signees avec la cle permanente (hors depot) et calcule leur empreinte.
$cles = "D:\projo-fabrication\cles-cadrans\mots-de-passe.txt"
if (-not (Test-Path $cles)) { throw "Cle de signature introuvable : $cles" }
$c = @{}
Get-Content $cles | ForEach-Object { $k, $v = $_ -split '=', 2; $c[$k] = $v }
$env:CADRANS_KEYSTORE = $c['keystore']
$env:CADRANS_KEYALIAS = $c['alias']
$env:CADRANS_STOREPASS = $c['storepass']
$env:CADRANS_KEYPASS = $c['keypass']
$projet = Split-Path $PSScriptRoot -Parent
& "$PSScriptRoot\gradle.ps1" assembleRelease
if ($LASTEXITCODE -ne 0) { throw "La fabrication a echoue." }
$sortie = Join-Path $projet "livraison"
New-Item -ItemType Directory -Force -Path $sortie | Out-Null
$src = Join-Path $projet "app\build\outputs\apk\release\app-release.apk"
$dst = Join-Path $sortie "Cadrans.apk"
Copy-Item $src $dst -Force
$h = (Get-FileHash $dst -Algorithm SHA256).Hash.ToLower()
"$dst  $([math]::Round((Get-Item $dst).Length/1KB)) Ko  sha256=$h"
