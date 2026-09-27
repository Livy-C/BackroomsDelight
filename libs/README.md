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

## 2. Nested dependencies, unpacked (required for the development environment)

**This part is not optional if you want `runClient` to work.**

Two of the mods above ship dependencies as *nested* (jar-in-jar) jars, and Loom does not load nested
jars for `modImplementation` dependencies
([FabricMC/fabric-loom#1275](https://github.com/FabricMC/fabric-loom/issues/1275)). In a
development environment those mixins and classes never appear, and the game dies on startup:

| Outer mod                | Nested dependency | Failure without unpacking                                                |
| ------------------------ | ----------------- | ------------------------------------------------------------------------ |
| Farmer's Delight Refabricated | Porting Lib  | `No enum constant RecipeBookType.FARMERSDELIGHT_COOKING`                  |
| AppleSkin                | Cloth Config      | `NoClassDefFoundError: me/shedaniel/autoconfig/ConfigData`                 |

Production is *not* affected — the real Fabric Loader does unpack nested jars. This is purely a
development-environment limitation.

The workaround is to unpack those modules here; `build.gradle` picks up every `*.jar` in this
folder (except the mods resolved from maven) and adds it as `modLocalRuntime`. Run:

```powershell
powershell -ExecutionPolicy Bypass -File tools/unpack-nested-deps.ps1
```

The script downloads Porting Lib 2.3.15+1.20.1, unpacks its nested modules, unpacks AppleSkin's
Cloth Config, and drops the duplicate copies that Farmer's Delight nests under different file
names — `base-2.3.15+1.20.1.jar` and `porting_lib_base-2.3.15+1.20.1.jar` are the same mod
(`porting_lib_base`) and loading both makes the loader complain about a duplicate mod id.

It expects the Farmer's Delight and AppleSkin jars to already be in the Gradle cache, so run a
build first (or let it fail on the unpacked dependencies) and then run the script.

Because these entries are `modLocalRuntime`, the published jar and CI are unaffected; they use the
real loader, which handles the nested jars itself.
