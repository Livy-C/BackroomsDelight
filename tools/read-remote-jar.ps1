# Extracts a few small text entries from a remote jar via HTTP range requests.
param(
	[string]$Url = 'https://cdn.modrinth.com/data/QvKZhPq3/versions/4jB1wBDN/endless_backrooms-0.4.3.jar',
	[string]$OutDir = "$env:TEMP\eb-extract",
	[string[]]$Entries = @('fabric.mod.json'),
	[int]$Attempts = 40
)

$ErrorActionPreference = 'Continue'
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

function Get-RangeRetry([string]$url, [long]$start, [long]$end, [int]$attempts) {
	$want = $end - $start + 1
	for ($a = 1; $a -le $attempts; $a++) {
		try {
			$req = [System.Net.HttpWebRequest]::Create($url)
			$req.UserAgent = 'Mozilla/5.0'
			$req.Timeout = 30000
			$req.ReadWriteTimeout = 30000
			$req.AddRange($start, $end)
			$resp = $req.GetResponse()
			try {
				$ms = New-Object System.IO.MemoryStream
				$resp.GetResponseStream().CopyTo($ms)
				$b = $ms.ToArray()
			} finally { $resp.Close() }
			if ($b.Length -eq $want) { return , $b }
		} catch { }
		Start-Sleep -Milliseconds 400
	}
	return $null
}

function Expand-RawDeflate([byte[]]$data) {
	# skip the 2-byte zlib header, then inflate the raw deflate stream
	$ms = New-Object System.IO.MemoryStream
	$ms.Write($data, 2, $data.Length - 2)
	$ms.Position = 0
	$ds = New-Object System.IO.Compression.DeflateStream($ms, [System.IO.Compression.CompressionMode]::Decompress)
	$out = New-Object System.IO.MemoryStream
	$buf = New-Object byte[] 8192
	while (($n = $ds.Read($buf, 0, $buf.Length)) -gt 0) { $out.Write($buf, 0, $n) }
	$ds.Close(); $ms.Close()
	return , $out.ToArray()
}

# read EOCD -> central directory -> entry list
$req = [System.Net.HttpWebRequest]::Create($Url)
$req.UserAgent = 'Mozilla/5.0'; $req.Timeout = 30000; $req.AddRange(0, 0)
$resp = $req.GetResponse(); $cr = $resp.Headers['Content-Range']; $resp.Close()
if ($cr -notmatch '/(\d+)$') { Write-Output "cannot get size"; exit 1 }
$size = [long]$Matches[1]

$tailLen = [math]::Min(65557, $size)
$tail = Get-RangeRetry $Url ($size - $tailLen) ($size - 1) $Attempts
$eocd = -1
for ($i = $tail.Length - 22; $i -ge 0; $i--) {
	if ($tail[$i] -eq 0x50 -and $tail[$i+1] -eq 0x4B -and $tail[$i+2] -eq 0x05 -and $tail[$i+3] -eq 0x06) { $eocd = $i; break }
}
$cdSize = [BitConverter]::ToUInt32($tail, $eocd + 12)
$cdOffset = [BitConverter]::ToUInt32($tail, $eocd + 16)
$cd = Get-RangeRetry $Url $cdOffset ($cdOffset + $cdSize - 1) $Attempts

$all = @{}
$p = 0
while ($p -lt $cd.Length - 46) {
	if (-not ($cd[$p] -eq 0x50 -and $cd[$p+1] -eq 0x4B -and $cd[$p+2] -eq 0x01 -and $cd[$p+3] -eq 0x02)) { break }
	$method   = [BitConverter]::ToUInt16($cd, $p + 10)
	$compSize = [BitConverter]::ToUInt32($cd, $p + 20)
	$nameLen  = [BitConverter]::ToUInt16($cd, $p + 28)
	$extraLen = [BitConverter]::ToUInt16($cd, $p + 30)
	$cmtLen   = [BitConverter]::ToUInt16($cd, $p + 32)
	$localOff = [BitConverter]::ToUInt32($cd, $p + 42)
	$name = [System.Text.Encoding]::UTF8.GetString($cd, $p + 46, $nameLen)
	$all[$name] = [pscustomobject]@{ Method = $method; CompSize = $compSize; LocalOffset = $localOff }
	$p += 46 + $nameLen + $extraLen + $cmtLen
}
Write-Output "index: $($all.Count) entries"

foreach ($want in $Entries) {
	if (-not $all.ContainsKey($want)) { Write-Output "  MISSING entry: $want"; continue }
	$e = $all[$want]
	$lh = Get-RangeRetry $Url $e.LocalOffset ($e.LocalOffset + 29) $Attempts
	if (-not $lh) { Write-Output "  FAIL local header $want"; continue }
	$lNameLen  = [BitConverter]::ToUInt16($lh, 26)
	$lExtraLen = [BitConverter]::ToUInt16($lh, 28)
	$dataStart = $e.LocalOffset + 30 + $lNameLen + $lExtraLen
	$raw = Get-RangeRetry $Url $dataStart ($dataStart + $e.CompSize - 1) $Attempts
	if (-not $raw) { Write-Output "  FAIL data $want"; continue }
	if ($e.Method -eq 8) {
		try { $bytes = Expand-RawDeflate $raw } catch { Write-Output "  INFLATE FAIL $want : $($_.Exception.Message)"; continue }
	} else { $bytes = $raw }
	$outPath = Join-Path $OutDir (($want -replace '[\\/]', '_'))
	[System.IO.File]::WriteAllBytes($outPath, $bytes)
	Write-Output "  saved $want ($($bytes.Length) bytes) -> $outPath"
}
