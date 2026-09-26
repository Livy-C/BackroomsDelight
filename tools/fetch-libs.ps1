# Resumable chunked downloader for the Modrinth dependency jars.
# The network here throttles cdn.modrinth.com and drops roughly half the connections,
# so each chunk is retried independently and completed chunks are kept on disk.
param(
	[int]$ChunkSize = 4MB,
	[int]$MaxAttemptsPerChunk = 40,
	[int]$Passes = 6
)

$ErrorActionPreference = 'Continue'
$stateDir = Join-Path $env:TEMP 'brd-chunks'
New-Item -ItemType Directory -Force -Path $stateDir | Out-Null

$targets = @(
	@{
		Name = 'endless_backrooms-0.4.3.jar'
		Url  = 'https://cdn.modrinth.com/data/QvKZhPq3/versions/4jB1wBDN/endless_backrooms-0.4.3.jar'
		Size = 9408679
	},
	@{
		Name = 'FarmersDelight-1.20.1-2.5.7+refabricated.jar'
		Url  = 'https://cdn.modrinth.com/data/7vxePowz/versions/7H1g1o5h/FarmersDelight-1.20.1-2.5.7%2Brefabricated.jar'
		Size = 6117761
	}
)

function Get-Chunk([string]$url, [long]$start, [long]$end, [string]$path) {
	$req = [System.Net.HttpWebRequest]::Create($url)
	$req.UserAgent = 'dsh-agent/1.0'
	$req.Timeout = 40000
	$req.ReadWriteTimeout = 40000
	$req.AddRange($start, $end)
	$resp = $req.GetResponse()
	$fs = [System.IO.File]::Create($path)
	try {
		$resp.GetResponseStream().CopyTo($fs)
	} finally {
		$fs.Close()
		$resp.Close()
	}
}

foreach ($t in $targets) {
	$expected = [int][math]::Ceiling($t.Size / $ChunkSize)
	$dir = Join-Path $stateDir ($t.Name -replace '[^\w\.\-]', '_')
	New-Item -ItemType Directory -Force -Path $dir | Out-Null

	for ($pass = 1; $pass -le $Passes; $pass++) {
		$missing = @()
		for ($i = 0; $i -lt $expected; $i++) {
			$start = [long]$i * $ChunkSize
			$end = [math]::Min($start + $ChunkSize - 1, [long]$t.Size - 1)
			$want = $end - $start + 1
			$cp = Join-Path $dir ("{0:D4}.part" -f $i)
			if ((Test-Path $cp) -and (Get-Item $cp).Length -eq $want) { continue }
			$missing += , @($i, $start, $end, $want, $cp)
		}
		if ($missing.Count -eq 0) { break }

		Write-Output "  [$($t.Name)] pass $pass : $($missing.Count)/$expected chunks missing"
		foreach ($m in $missing) {
			$i, $start, $end, $want, $cp = $m
			for ($a = 1; $a -le $MaxAttemptsPerChunk; $a++) {
				try {
					Get-Chunk $t.Url $start $end $cp
					if ((Get-Item $cp).Length -eq $want) { break }
				} catch {
					if ($a -eq $MaxAttemptsPerChunk) { Write-Output "    chunk $i gave up: $($_.Exception.Message)" }
					Start-Sleep -Milliseconds 400
				}
			}
		}
	}

	# assemble
	$have = 0
	for ($i = 0; $i -lt $expected; $i++) {
		$cp = Join-Path $dir ("{0:D4}.part" -f $i)
		if (Test-Path $cp) { $have += (Get-Item $cp).Length }
	}
	if ($have -eq $t.Size) {
		$out = Join-Path 'D:\Projects\BackroomsDelight\libs' $t.Name
		$fs = [System.IO.File]::Create($out)
		try {
			for ($i = 0; $i -lt $expected; $i++) {
				$cp = Join-Path $dir ("{0:D4}.part" -f $i)
				$bytes = [System.IO.File]::ReadAllBytes($cp)
				$fs.Write($bytes, 0, $bytes.Length)
			}
		} finally { $fs.Close() }
		Write-Output "  ASSEMBLED $($t.Name) -> $([math]::Round((Get-Item $out).Length / 1MB, 2)) MB"
	} else {
		Write-Output "  INCOMPLETE $($t.Name): $have / $($t.Size) bytes"
	}
}
