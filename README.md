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

Building requires **JDK 21 or newer** (Fabric Loom 1.17 needs a Java 21+ runtime); the mod
itself targets Java 17 bytecode.

```sh
./gradlew build          # compile and package
./gradlew runClient      # launch a development client
./gradlew runServer      # launch a development server
```

The built jar is written to `build/libs/`.

For IDE setup instructions, see the [Fabric Documentation](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up)
page for your IDE.

## Layout

```
src/main/java/neo/livy/elbkrdelight/                 common (server + client) code
src/client/java/neo/livy/elbkrdelight/client/       client-only code
src/main/resources/                                 fabric.mod.json, assets
libs/                                               optional local dependency jars
```

The mod id is `endless_backrooms_delight`; the Java package root is `neo.livy.elbkrdelight`.

Mixins use Fabric Loom's split source sets: common mixins belong in
`src/main/java/neo/livy/elbkrdelight/mixin/` and client-only mixins in
`src/client/java/neo/livy/elbkrdelight/client/mixin/`. Add the corresponding
`endless_backrooms_delight.mixins.json` / `endless_backrooms_delight.client.mixins.json`
configs and reference them from the `mixins` array in `fabric.mod.json` when you add the first one.

## License

This project is available under the CC0 license. See [LICENSE](LICENSE).
The required mods above are licensed separately and are not covered by it.
