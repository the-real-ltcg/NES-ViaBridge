# NES ViaBridge

A companion mod for **Not Enough Spectators (NES)** that lets spectators running *other*
Minecraft versions connect to a 26.2 host. It bundles the **ViaVersion + ViaBackwards +
ViaRewind** stack and splices Via's translation handlers into the NES spectator server.

> **Note:** The main [Not Enough Spectators Fix](https://github.com/the-real-ltcg/Not-Enough-Spectators-Fix)
> repo now ships this same functionality **embedded** in a single jar (recommended). This standalone
> companion is the two-jar alternative — use it **only** with a *plain* NES build that does **not**
> embed Via, or the two will conflict (both boot ViaVersion, which may only init once).

## How it works

NES runs a small Netty server inside the host's client and replays the host's captured packets
(native version: **26.2**) to spectators. Normally a spectator must also be on 26.2.

ViaBridge treats that server like a **backend Minecraft server with ViaVersion installed**:

1. On startup it boots a minimal Via platform whose *server protocol version* is `26.2`
   (`BridgeInjector#getServerProtocolVersion`), and loads ViaBackwards + ViaRewind for the full
   range of older client versions.
2. A Mixin into `cheeezer.notenoughspectators.server.SpectatorServerNetworkHandler#channelActive`
   inserts a server-side `ViaDecodeHandler` / `ViaEncodeHandler` (with a fresh `UserConnection`)
   into every spectator channel, just before NES's `decoder` / `encoder`.
3. Via auto-detects each spectator's protocol version from its handshake and translates
   `client version <-> 26.2` in both directions.

The Mixin targets NES by string, so this mod does **not** need NES on its compile classpath; it
just needs NES installed at runtime (declared as a dependency).

## Installing

Drop **both** mods in `mods/`:

- `not-enough-spectators-*.jar`
- `nes-viabridge-*.jar`

Do **not** also install ViaFabric / ViaFabricPlus / standalone ViaVersion — ViaBridge is the sole
Via bootstrap, and `Via.init` can only run once. These are declared as `breaks` so the loader will
stop you.

## Using it

1. Host starts sharing as usual: `/notenoughspectators share` (or `/nes share`).
2. A friend on a **different** Minecraft version points their client at the host's address and
   joins normally — ViaBridge translates their protocol to/from the host's 26.2 automatically.
   No extra step for the spectator (they don't need any Via mod themselves).
3. Check who's connected and on what version:

   ```
   /nesvia status
   ```

   Output is like:

   ```
   [NES ViaBridge] host protocol: 26.2 — spectators may join from other versions.
   2 spectator connection(s):
    - Steve: 1.21.4
    - Alex: 1.20.1
   ```

   (`/nesvia` on its own prints the same status.)

## Building

```
./gradlew build      # JDK 25 required (set JAVA_HOME)
```

Output: `build/libs/nes-viabridge-<version>.jar`. The ViaVersion stack and its mapping data are
fat-jarred in (the new non-remapping Loom makes this safe), so the jar is fully self-contained.

## Status / caveats

Bundles ViaVersion / ViaBackwards / ViaRewind (snapshot builds) which include protocol `26.2`.

**Same-version (26.2) spectating works.** Older-version spectating currently gets all the way through
handshake, login, and the entire configuration phase — Via attaches and translates — then fails when
the old client parses 26.2's registry data (`Failed to load registries`). That last step is an
**upstream ViaBackwards limitation** for the brand-new 26.2 registries (`sulfur_cube_archetype`,
`dialog`, `wolf_sound_variant`, …), not a bug in this mod. It will start working once ViaBackwards
ships 26.2 down-conversion — a refreshed rebuild (`./gradlew build --refresh-dependencies`) picks it
up with no code change.

## License

**GPL-3.0** (see [LICENSE](LICENSE)) — required because it bundles the GPL-3.0 Via stack.
Copyright (C) 2026 the-real-ltcg.
