# Copia o codigo nativo e libera a permissao de musicas no projeto Android
$dest = "android\app\src\main\java\com\verde\player"
if (-not (Test-Path $dest)) { Write-Host "Pasta android nao encontrada. Rode antes: npx cap add android"; exit 1 }
Copy-Item "native\MainActivity.java" $dest -Force
Copy-Item "native\MusicLibraryPlugin.java" $dest -Force
$m = "android\app\src\main\AndroidManifest.xml"
$t = Get-Content $m -Raw
if ($t -notmatch "READ_MEDIA_AUDIO") {
  $nl = [Environment]::NewLine
  $add = '    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />' + $nl + '    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />' + $nl + '</manifest>'
  $t = $t.Replace("</manifest>", $add)
  Set-Content $m $t -Encoding UTF8
}
Write-Host "Pronto! Agora rode: npx cap sync android"
