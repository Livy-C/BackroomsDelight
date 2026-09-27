# Unpacks Porting Lib's nested modules into libs/ so Farmer's Delight starts in a development
# environment. See libs/README.md for why this is needed.
#
# Loom does not load nested jars for modImplementation dependencies
# (FabricMC/fabric-loom#1275), so Porting Lib's mixins never apply in dev and Farmer's Delight
# dies on "No enum constant RecipeBookType.FARMERSDELIGHT_COOKING". Production is unaffected.
param(
	[string]$PortingLibVersion = '2.3.15+1.20.1',
	[string]$OutDir = (Join-Path $PSScriptRoot '..\libs')
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$OutDir = [System.IO.Path]::GetFullPath($OutDir)

function Get-RangeBytes([string]$url, [long]$start, [long]$end, [int]$attempts = 30) {
	$want = $end - $start + 1
	for ($a = 1; $a -le $attempts; $a++) {
		$resp = $null
		try {
			$req = [System.Net.HttpWebRequest]::Create($url)
			$req.UserAgent = 'Mozilla/5.0'
			$req.Timeout = 30000
			$req.ReadWriteTimeout = 30000
			$req.AddRange($start, $end)
			$resp = $req.GetResponse()
			$ms = New-Object System.IO.MemoryStream
			$resp.GetResponseStream().CopyTo($ms)
			$bytes = $ms.ToArray()
			if ($bytes.Length -eq $want) { return , $bytes }
		} catch {
		} finally {
			if ($resp) { $resp.Close() }
		}
		Start-Sleep -Milliseconds 400
	}
	throw "Giving up on bytes $start-$end after $attempts attempts"
}

# 1. resolve the umbrella jar
Write-Output "Resolving Porting Lib $PortingLibVersion ..."
$headers = @{ 'User-Agent' = 'dsh-agent/1.0' }
$versions = Invoke-RestMethod -Uri "https://api.modrinth.com/v2/project/porting_lib/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%221.20.1%22%5D" -Headers $headers
$target = $versions | Where-Object { $_.version_number -eq $PortingLibVersion } | Select-Object -First 1
if (-not $target) { throw "Porting Lib $PortingLibVersion not found on Modrinth" }

$url = $target.files[0].url
$size = $target.files[0].size
$umbrella = Join-Path $OutDir ("porting_lib-{0}.jar" -f $PortingLibVersion)

# 2. download it in small retried chunks; this CDN throttles hard and drops connections
if (-not (Test-Path $umbrella) -or (Get-Item $umbrella).Length -ne $size) {
	Write-Output "Downloading $size bytes in chunks ..."
	$chunkSize = 200KB
	$chunks = [int][math]::Ceiling($size / $chunkSize)
	$parts = New-Object 'System.Collections.Generic.List[byte[]]'

	for ($i = 0; $i -lt $chunks; $i++) {
		$start = [long]$i * $chunkSize
		$end = [math]::Min($start + $chunkSize - 1, $size - 1)
		$parts.Add((Get-RangeBytes $url $start $end))
		if ((($i + 1) % 8) -eq 0 -or $i -eq $chunks - 1) {
			Write-Output ("  {0,5}%  {1}/{2} chunks" -f [math]::Round(100.0 * ($i + 1) / $chunks, 1), ($i + 1), $chunks)
		}
	}

	$fs = [System.IO.File]::Create($umbrella)
	try { foreach ($p in $parts) { $fs.Write($p, 0, $p.Length) } } finally { $fs.Close() }
	Write-Output "Saved $umbrella"
}

# 3. unpack the nested modules
Write-Output "Unpacking nested modules ..."
$zip = [System.IO.Compression.ZipFile]::OpenRead($umbrella)
$count = 0
try {
	foreach ($entry in $zip.Entries) {
		if ($entry.FullName -match '^META-INF/jars/(.+\.jar)$') {
			$name = $Matches[1]
			[System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, (Join-Path $OutDir $name), $true)
			$count++
		}
	}
} finally { $zip.Dispose() }
Write-Output "  unpacked $count modules"

# 4. drop Farmer's Delight's duplicate copies: they are the same mods under different file names
$fdJar = Get-ChildItem (Join-Path $env:USERPROFILE '.gradle\caches\modules-2\files-2.1\maven.modrinth\farmers-delight-refabricated') -Recurse -Filter '*.jar' -ErrorAction SilentlyContinue |
		Where-Object { $_.Name -notmatch 'sources' } | Select-Object -First 1

if ($fdJar) {
	Write-Output "Removing duplicates also nested inside $($fdJar.Name) ..."
	$zip = [System.IO.Compression.ZipFile]::OpenRead($fdJar.FullName)
	try {
		foreach ($entry in $zip.Entries) {
			if ($entry.FullName -match '^META-INF/jars/(.+\.jar)$') {
				$dupe = Join-Path $OutDir $Matches[1]

				# Never delete the umbrella jar we just downloaded.
				if ($dupe -eq $umbrella) { continue }

				if (Test-Path $dupe) { Remove-Item $dupe -Force; Write-Output "  removed $($Matches[1])" }
			}
		}
	} finally { $zip.Dispose() }
} else {
	Write-Output "Note: Farmer's Delight jar not in the Gradle cache yet, skipped duplicate cleanup."
	Write-Output "      Run a build once, then run this script again."
}

Write-Output ""
Write-Output "Done. libs/ now holds $((Get-ChildItem $OutDir -Filter '*.jar').Count) jars."
Write-Output "These are gitignored; re-run this script after a fresh clone."
