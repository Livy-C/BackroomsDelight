# libs/

Local mod jars used by the build.

Mod jars are not committed to this repository. See `LICENSE` for this project's own license; the
dependencies keep their own licenses (Endless Backrooms: ARR, Farmer's Delight: MIT, Porting Lib:
LGPL).

## 1. The two required mods (optional offline fallback)

`build.gradle` prefers a jar in this folder when one is present, and otherwise resolves the
dependency from the Modrinth maven repository. Both paths use the same mod, so these two are only
needed when the Modrinth maven repository is unreachable.

| File name                                      | Source                                                                     |
| ---------------------------------------------- | -------------------------------------------------------------------------- |
| `endless_backrooms-0.4.3.jar`                  | https://modrinth.com/mod/endless_backrooms/version/0.4.3                   |
| `FarmersDelight-1.20.1-2.5.7+refabricated.jar` | https://modrinth.com/mod/farmers-delight-refabricated/version/1.20.1-2.5.7 |

The file names and the versions in `gradle.properties` must agree: bump both together.

On a slow or lossy connection, `tools/fetch-libs.ps1` downloads both jars here in resumable
chunks, retrying each chunk independently:

```powershell
powershell -ExecutionPolicy Bypass -File tools/fetch-libs.ps1
```

## 2. Porting Lib, unpacked (required for the development environment)

**This part is not optional if you want `runClient`/`runServer` to work.**

Farmer's Delight Refabricated depends on Porting Lib, which ships as an umbrella jar full of
nested jars. Loom does not load nested jars for `modImplementation` dependencies
([FabricMC/fabric-loom#1275](https://github.com/FabricMC/fabric-loom/issues/1275)), so in a
development environment Porting Lib's mixins never apply and Farmer's Delight crashes on startup:

```
java.lang.IllegalArgumentException: No enum constant
    net.minecraft.world.inventory.RecipeBookType.FARMERSDELIGHT_COOKING
    at vectorwing.farmersdelight.FarmersDelight.<clinit>
```

Production is *not* affected — the real Fabric Loader does unpack nested jars. This is purely a
development-environment limitation.

The workaround is to unpack Porting Lib's modules here; `build.gradle` picks up every `*.jar` in
this folder (except the two above) and adds it as `modLocalRuntime`. Run:

```powershell
powershell -ExecutionPolicy Bypass -File tools/unpack-porting-lib.ps1
```

The script downloads Porting Lib 2.3.15+1.20.1, unpacks its nested modules here, and drops the
duplicate copies that Farmer's Delight nests under different file names — `base-2.3.15+1.20.1.jar`
and `porting_lib_base-2.3.15+1.20.1.jar` are the same mod (`porting_lib_base`) and loading both
makes the loader complain about a duplicate mod id.

Because these entries are `modLocalRuntime`, the published jar and CI are unaffected; they use the
real loader, which handles the nested jars itself.
