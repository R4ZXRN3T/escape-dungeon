$ErrorActionPreference = 'Stop'

$repoRoot = $PSScriptRoot
$distDir = Join-Path $repoRoot "lwjgl3/build/construo/dist"
$finalDir = Join-Path $repoRoot "final"
$assetsDir = Join-Path $repoRoot "assets"
$releaseVersion = "1.0.0"

function Get-PlatformAndArch([string]$name) {
    $lowerName = $name.ToLowerInvariant()

    $platform = "Unknown"
    if ($lowerName -match "win|windows") {
        $platform = "Windows"
    } elseif ($lowerName -match "linux") {
        $platform = "Linux"
    } elseif ($lowerName -match "mac|macos|osx|darwin") {
        $platform = "MacOS"
    }

    $arch = "x86_64"
    if ($lowerName -match "aarch64|arm64|arm|m1") {
        $arch = "aarch64"
    }

    return @{ Platform = $platform; Arch = $arch }
}

$sevenZipExe = (Get-Command "7z.exe" -ErrorAction SilentlyContinue).Source
if (-not $sevenZipExe) {
    $fallback7z = "C:/Program Files/7-Zip/7z.exe"
    if (Test-Path $fallback7z) {
        $sevenZipExe = $fallback7z
    }
}
if (-not $sevenZipExe) {
    throw "7-Zip executable not found. Install 7-Zip or add 7z.exe to PATH."
}

& (Join-Path $repoRoot "gradlew.bat") lwjgl3:packageMacM1
& (Join-Path $repoRoot "gradlew.bat") lwjgl3:packageMacX64
& (Join-Path $repoRoot "gradlew.bat") lwjgl3:packageLinuxX64
& (Join-Path $repoRoot "gradlew.bat") lwjgl3:packageWinX64

Move-Item -Path (Join-Path $distDir "*.zip") -Destination $repoRoot -Force

if (Test-Path $finalDir) {
    Remove-Item -Path $finalDir -Recurse -Force
}
New-Item -ItemType Directory -Path $finalDir | Out-Null

$zipFiles = Get-ChildItem -Path $repoRoot -Filter "*.zip" -File
foreach ($zip in $zipFiles) {
    $platformInfo = Get-PlatformAndArch $zip.BaseName
    if ($platformInfo.Platform -eq "Unknown") {
        throw "Could not detect target platform from zip name '$($zip.Name)'"
    }

    $artifactBaseName = "EscapeDungeon-$releaseVersion-$($platformInfo.Platform)-$($platformInfo.Arch)"
    $targetDir = Join-Path $finalDir $artifactBaseName
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null

    Expand-Archive -Path $zip.FullName -DestinationPath $targetDir -Force

    $macOsBundleDir = Join-Path $targetDir "EscapeDungeon.app/Contents/MacOS"
    if (Test-Path $macOsBundleDir) {
        $assetsDestination = Join-Path $macOsBundleDir "assets"
    } else {
        $assetsDestination = Join-Path $targetDir "assets"
    }

    if (Test-Path $assetsDestination) {
        Remove-Item -Path $assetsDestination -Recurse -Force
    }
    Copy-Item -Path $assetsDir -Destination $assetsDestination -Recurse -Force

    $repackedZip = Join-Path $finalDir ($artifactBaseName + ".zip")
    if (Test-Path $repackedZip) {
        Remove-Item -Path $repackedZip -Force
    }

    Push-Location $targetDir
    try {
        & $sevenZipExe a "-tzip" "-mm=LZMA" "-mx=9" "-md=3840m" "-mfb=273" "-mmt=3" "--" $repackedZip ".\\*"
        if ($LASTEXITCODE -ne 0) {
            throw "7-Zip failed for $($zip.Name) with exit code $LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }
}

$answer = Read-Host "Do you want to delete build files? This will make the next build take longer. (y/N)"
if ($answer -eq 'y' -or $answer -eq "Y") {
    & (Join-Path $repoRoot "gradlew.bat") clean
}
Remove-Item -Path (Join-Path $repoRoot "*.zip") -Force
