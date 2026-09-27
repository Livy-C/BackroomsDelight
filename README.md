# Endless Backrooms Delight

A Minecraft mod that adds the Backrooms as an endless, procedurally generated space.

## Requirements

| Component     | Version          |
| ------------- | ---------------- |
| Minecraft     | 1.20.1           |
| Fabric Loader | 0.19.5+          |
| Fabric API    | 0.92.12+1.20.1   |

### Required mods

This mod requires the following mods at runtime. They are declared in the `depends` block of
`fabric.mod.json`, so Fabric Loader will refuse to start if either is missing.

| Mod                             | Mod id             | Version          | Why                                                       |
| ------------------------------- | ------------------ | ---------------- | --------------------------------------------------------- |
| [Endless Backrooms][eb]         | `endless_backrooms`| 0.4.3            | Provides the Backrooms themselves.                        |
| [Farmer's Delight Refabricated][fd] | `farmersdelight` | 1.20.1-2.5.7    | Farming and cooking content this mod builds on.           |

[eb]: https://modrinth.com/mod/endless_backrooms
[fd]: https://modrinth.com/mod/farmers-delight-refabricated

Both are resolved from the [Modrinth maven repository][modrinth-maven] at build time, so they
appear on the development classpath and in a `runClient`/`runServer` session.

> **Farmer's Delight is Forge/NeoForge only.** The official [Farmer's Delight][fd-forge] does not
> support Fabric, so this project depends on **Farmer's Delight Refabricated**, the Fabric port
> maintained by MehVahdJukaar. It keeps the original mod id `farmersdelight`, which is why the
> dependency is declared as `farmersdelight` rather than `farmers-delight`.

[fd-forge]: https://modrinth.com/mod/farmers-delight
[modrinth-maven]: https://api.modrinth.com/maven

The versions live in `gradle.properties` (`endless_backrooms_version`, `farmers_delight_version`)
and must be kept in sync with the `depends` block in `fabric.mod.json`. If the Modrinth maven
repository is unreachable, jars can be placed in `libs/` instead — see [libs/README.md](libs/README.md).

## Development

### JDK 21 is required

The build's **launcher JVM** must be Java 21 or newer, because Fabric Loom 1.17 refuses to
configure on an older JVM:

```
Could not resolve net.fabricmc:fabric-loom:1.17.21
> Dependency requires at least JVM runtime version 21. This build uses a Java 17 JVM.
```

That JVM is whichever one `JAVA_HOME` points at, so set it before building:

```sh
# Windows
set JAVA_HOME=D:\Java\temurin-21
# macOS / Linux
export JAVA_HOME=/path/to/jdk-21
```

The mod itself still targets **Java 17 bytecode** (`options.release = 17`); JDK 21 is only what
runs the build. If Gradle cannot auto-detect a JDK 21, set `java21_home` in `gradle.properties`
to point at one — `build.gradle` validates the path and says so if it is wrong.

### Making the dev environment work

```sh
./gradlew build          # compile and package
./gradlew runClient      # launch a development client
./gradlew runServer      # launch a development server
```

Before the first `runClient`, unpack the nested dependencies:

```powershell
powershell -ExecutionPolicy Bypass -File tools/unpack-nested-deps.ps1
```

Without it, the game dies during startup with
`No enum constant RecipeBookType.FARMERSDELIGHT_COOKING`. This is a Loom limitation, not a mistake
in your setup, and it does **not** affect production — see [libs/README.md](libs/README.md) for the
full explanation. The same thing bites AppleSkin, which bundles Cloth Config the same way and fails
without it on `NoClassDefFoundError: me/shedaniel/autoconfig/ConfigData`.

`runServer` additionally cannot work with this dependency set: Porting Lib registers its recipe
book category through a **client-only** mixin, while Farmer's Delight's initializer reads that
enum on both sides. The server therefore fails the same way even with Porting Lib unpacked. Use
`runClient` for development, and note that a dedicated server needs a different Farmer's Delight
or Porting Lib build.

### Development-only mods

JEI and AppleSkin load in `runClient`/`runServer` but are not required at runtime and are not
bundled into the built jar, so they are declared as `modLocalRuntime` and deliberately kept out of
the `depends` block in `fabric.mod.json`. Their versions live in `gradle.properties`
(`jei_version`, `appleskin_version`).

```sh
./gradlew runClient
```

Then search `royal_ration` in JEI to see the cooking pot recipe, and check the hunger bar preview
from AppleSkin to see what the stew restores.

The built jar is written to `build/libs/`.

For IDE setup instructions, see the [Fabric Documentation](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up)
page for your IDE.

## Layout

```
src/main/java/neo/livy/elbkrdelight/                 common (server + client) code
src/main/java/neo/livy/elbkrdelight/item/            items and their registration
src/client/java/neo/livy/elbkrdelight/client/       client-only code
src/main/resources/                                 fabric.mod.json, assets, data (recipes)
libs/                                               local dependency jars (gitignored)
tools/                                              helper scripts for this project
```

The mod id is `endless_backrooms_delight`; the Java package root is `neo.livy.elbkrdelight`.

Mixins use Fabric Loom's split source sets: common mixins belong in
`src/main/java/neo/livy/elbkrdelight/mixin/` and client-only mixins in
`src/client/java/neo/livy/elbkrdelight/client/mixin/`. Add the corresponding
`endless_backrooms_delight.mixins.json` / `endless_backrooms_delight.client.mixins.json`
configs and reference them from the `mixins` array in `fabric.mod.json` when you add the first one.

### A note on registering content

Endless Backrooms registers its effects lazily (Porting Lib's `LazyRegistrar`), so anything that
needs one of its effects must be prepared for the effect not to exist yet during this mod's
initializer. `ModItems` shows the pattern: look the effect up directly, and fall back to
`RegistryEntryAddedCallback` if it is missing. Note that the callback only fires for entries added
*after* it is registered, so it cannot be the only path.

## License

This project is available under the CC0 license. See [LICENSE](LICENSE).
The required mods above are licensed separately and are not covered by it.
