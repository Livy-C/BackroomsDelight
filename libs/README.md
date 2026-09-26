# libs/

Optional local copies of the required runtime dependencies.

`build.gradle` prefers a jar in this folder when one is present, and otherwise resolves the
dependency from the Modrinth maven repository. Both paths use the same mod, so this folder is
only needed when the Modrinth maven repository is unreachable.

To use it, drop in the jars with exactly these file names:

| File name                                 | Source                                                                   |
| ----------------------------------------- | ------------------------------------------------------------------------ |
| `endless_backrooms-0.4.3.jar`             | https://modrinth.com/mod/endless_backrooms/version/0.4.3                 |
| `FarmersDelight-1.20.1-2.5.7+refabricated.jar` | https://modrinth.com/mod/farmers-delight-refabricated/version/1.20.1-2.5.7 |

The file names and the versions in `gradle.properties` must agree: bump both together.

On a slow or lossy connection, `tools/fetch-libs.ps1` downloads both jars here in resumable
chunks, retrying each chunk independently:

```powershell
powershell -ExecutionPolicy Bypass -File tools/fetch-libs.ps1
```

Mod jars are not committed to this repository. See `LICENSE` for this project's own license;
the dependencies keep their own licenses (Endless Backrooms: ARR, Farmer's Delight: MIT).
