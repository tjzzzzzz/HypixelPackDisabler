# Hypixel Server Pack Disabler

A small client-side Fabric mod that stops Hypixel's server resource pack from
overriding your textures on SkyBlock.

Hypixel forces a server resource pack, and it sits above everything else in
your pack list. This mod pushes it back down and puts the SkyBlock Legacy pack
on top of it, so the legacy textures actually show up.

## Requires Catharsis

You need [Catharsis](https://modrinth.com/mod/catharsis) installed alongside this mod.

The SkyBlock Legacy pack's `pack.mcmeta` uses `catharsis:config` resource
conditions. Without Catharsis, Minecraft throws while reading the pack metadata
and the pack never even shows up in your resource pack list — you'll see
`Failed to read pack file/SkyBlock Legacy.zip metadata` in the log.

It isn't bundled here, so grab it yourself. If it's missing, this mod will tell
you with a toast and a link in its config screen instead of silently doing
nothing.

## What it does

- Downloads the SkyBlock Legacy pack from Modrinth on first join (verified by SHA-1)
- Drops Hypixel's server pack below it so legacy textures win
- Only does any of this while you're connected to Hypixel — other servers are untouched

## Usage

Install this mod and Catharsis, then join Hypixel. That's the whole setup.

Run `/hypixelpackdisabler` if you want to turn it off. It's on by default, and
your choice is saved to `config/hypixelpackdisabler.json`.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3+
- Fabric API
- Fabric Language Kotlin
- [Catharsis](https://modrinth.com/mod/catharsis)
- Java 25

## Building

```bash
./gradlew build
```

The jar ends up in `build/libs/`. Grab the one *without* `-sources` in the name.

## License

All rights reserved. You're free to download and play with it, but not to
redistribute, modify, or reuse the source. See [LICENSE](LICENSE) for the
details.
