# Downloads a remote jar in many small, independently retried chunks.
# This network drops a large fraction of connections and throttles bulk transfers,
# so small chunks with per-chunk retry converge where a single request never finishes.
param(
	[string]$Url = 'https://cdn.modrinth.com/data/QvKZhPq3/versions/4jB1wBDN/endless_backrooms-0.4.3.jar',
	[string]$OutFile = "$env:TEMP\eb-0.4.3.jar",
	[int]$ChunkSize = 200KB,
	[int]$AttemptsPerChunk = 30
)

$ErrorActionPreference = 'Continue'

function Get-RangeBytes([string]$url, [long]$start, [long]$end) {
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
		return , $ms.ToArray()
	} catch {
		return $null
	} finally {
		if ($resp) { $resp.Close() }
	}
}

# discover size
$req = [System.Net.HttpWebRequest]::Create($Url)
$req.UserAgent = 'Mozilla/5.0'; $req.Timeout = 30000; $req.AddRange(0, 0)
$resp = $req.GetResponse(); $cr = $resp.Headers['Content-Range']; $resp.Close()
if ($cr -notmatch '/(\d+)$') { Write-Output "cannot determine size"; exit 1 }
$size = [long]$Matches[1]
$chunks = [int][math]::Ceiling($size / $ChunkSize)
Write-Output "url   : $Url"
Write-Output "size  : $size bytes in $chunks chunks of $ChunkSize"

$parts = New-Object 'System.Collections.Generic.List[byte[]]'
$sw = [System.Diagnostics.Stopwatch]::StartNew()
for ($i = 0; $i -lt $chunks; $i++) {
	$start = [long]$i * $ChunkSize
	$end = [math]::Min($start + $ChunkSize - 1, $size - 1)
	$want = $end - $start + 1
	$ok = $false
	for ($a = 1; $a -le $AttemptsPerChunk; $a++) {
		$b = Get-RangeBytes $Url $start $end
		if ($b -and $b.Length -eq $want) { $ok = $true; break }
		Start-Sleep -Milliseconds 300
	}
	if (-not $ok) { Write-Output "GAVE UP on chunk $i/$chunks ($start-$end)"; exit 1 }
	$parts.Add($b)
	if ((($i + 1) % 5) -eq 0 -or $i -eq $chunks - 1) {
		$pct = [math]::Round(100.0 * ($i + 1) / $chunks, 1)
		Write-Output ("  {0,5}%  {1}/{2} chunks  {3}s" -f $pct, ($i + 1), $chunks, [math]::Round($sw.Elapsed.TotalSeconds, 0))
	}
}

$fs = [System.IO.File]::Create($OutFile)
try { foreach ($p in $parts) { $fs.Write($p, 0, $p.Length) } } finally { $fs.Close() }
$final = (Get-Item $OutFile).Length
Write-Output "DONE: $OutFile ($final bytes, expected $size) match=$($final -eq $size)"
