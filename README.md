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

### If the wrapper cannot download Gradle

On a network that intercepts TLS, the wrapper fails with `PKIX path building failed` before Gradle
even starts, because it downloads its own distribution. Either add the intercepting root CA to the
JDK truststore, or point the wrapper at a mirror **locally without committing it**:

```sh
git update-index --skip-worktree gradle/wrapper/gradle-wrapper.properties
# then edit distributionUrl in that file
```

`git update-index --no-skip-worktree` undoes that. The committed value stays on the official
`services.gradle.org` URL on purpose: pointing a public repository at a third-party mirror makes
every fresh clone depend on that mirror staying up and staying in sync.

Once Gradle has run once, the distribution is cached in `~/.gradle/wrapper/dists` and the wrapper
no longer downloads it.

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

Then search the item names in JEI to see the cooking pot recipes, and check the hunger bar preview
from AppleSkin to see what each meal restores.

The built jar is written to `build/libs/`.

For IDE setup instructions, see the [Fabric Documentation](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up)
page for your IDE.

## Content

Five items. The four bowl meals stack to 16 and leave an empty bowl behind; fried carpet is a
hand-held snack with nothing left over. Meal definitions live in one place, the `MEALS` list in
`ModItems`.

| Item                             | Made in        | Recipe                                                        | Nutrition | Effects                                                    |
| -------------------------------- | -------------- | ------------------------------------------------------------- | --------- | ---------------------------------------------------------- |
| Royal Ration Stewed Moth Jelly   | cooking pot    | royal rations + moth jelly + sugar                             | 30        | Regeneration II 10s, Saturation 15s, Moth Pheromone 5min    |
| Dark Stew                        | cooking pot    | corruption liquid + almond water + carpet + wallpaper + onion + minced beef | 16 | Regeneration II 10s, Saturation 15s, **Darkness 15s**, **Nausea 10s** |
| Dried Shrimp Mushroom Stew       | cooking pot    | raw scit + mushroom stew + almond water + onion                 | 14        | Regeneration II 10s, Saturation 15s                         |
| Carrot Blue Almond Water Stew    | cooking pot    | carrot + blue almond water                                     | 12        | Night Vision 3min                                           |
| Fried Carpet                     | skillet        | Level 0 carpet                                                 | 4         | none - it is a greasy snack, not a meal                     |

Dark Stew is the one genuine trade in the set: six ingredients, the highest nutrition here, and it
then takes your sight away for fifteen seconds. It is the only item that inflicts a debuff.

All five are addictive, matching Endless Backrooms' own royal rations and moth jelly: every serving
raises the withdrawal amplifier one step, capped, and refreshes its duration.

| Item                            | Withdrawal duration | Cap |
| ------------------------------- | ------------------- | --- |
| Royal Ration Stewed Moth Jelly  | 6 minutes           | IV  |
| Dark Stew                       | 6 minutes           | IV  |
| Dried Shrimp Mushroom Stew      | 3 minutes           | III |
| Carrot Blue Almond Water Stew   | 3 minutes           | III |
| Fried Carpet                    | 90 seconds          | II  |

The royal version uses royal rations' own values; the milder items use shorter, lower-capped ones.
Moth Pheromone is a *buff*: Endless Backrooms' deathmoths check it in `DeathmothEntity.shouldIgnoreTarget`
and refuse to attack the player while it is active. Only the royal version grants it, because moth
jelly is one of its ingredients.

Food effects are listed in each item's tooltip, the way Farmer's Delight does it, via
`FoodEffectTooltip`. That is needed because vanilla only renders those lines for a few hard-coded
items.

The skillet is not a recipe type in Farmer's Delight; it is a hand-held tool that cooks vanilla
campfire recipes, which is why Fried Carpet uses `minecraft:campfire_cooking`.

> Vanilla clamps the player's food level to 20 (`Math.min` in `FoodData.eat`), so the royal
> version's 30 nutrition is partly wasted when eaten from low hunger; the extra shows up as
> saturation instead, which is clamped separately.

Textures live in this mod's own assets folder under `textures/item/`, one file per item, so real art
can be dropped in by overwriting a single file without touching any model. The royal version has its
own art (`royal_ration_stewed_moth_jelly.png`) and Fried Carpet reuses a copy of Level 0's carpet
texture. The two remaining stews currently hold a copy of the vanilla mushroom stew texture as a
placeholder. All are 16x16.

## Adding more content

[`docs/ingredients.md`](docs/ingredients.md) catalogues what vanilla, Endless Backrooms and Farmer's
Delight offer as ingredients, including which tags to prefer over hard-coded item ids — a tag makes
one recipe accept a whole category of items, which is what Farmer's Delight itself does.

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
