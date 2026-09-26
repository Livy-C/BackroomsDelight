# Endless Backrooms Delight

A Minecraft mod that adds the Backrooms as an endless, procedurally generated space.

## Requirements

| Component   | Version   |
| ----------- | --------- |
| Minecraft   | 1.20.1    |
| Fabric Loader | 0.19.5+ |
| Fabric API  | 0.92.12+1.20.1 |

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
```

The mod id is `endless_backrooms_delight`; the Java package root is `neo.livy.elbkrdelight`.

Mixins use Fabric Loom's split source sets: common mixins belong in
`src/main/java/neo/livy/elbkrdelight/mixin/` and client-only mixins in
`src/client/java/neo/livy/elbkrdelight/client/mixin/`. Add the corresponding
`endless_backrooms_delight.mixins.json` / `endless_backrooms_delight.client.mixins.json`
configs and reference them from the `mixins` array in `fabric.mod.json` when you add the first one.

## License

This project is available under the CC0 license. See [LICENSE](LICENSE).
