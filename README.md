NucleusFramework for Spigot 1.9
==================

A plugin framework and rapid development library for Bukkit/Spigot.

Currently not in active development. Do not use if you expect timely updates and bug fixes. May be discontinued
after Minecraft 1.9

Compatible with Spigot 1.9

## Known Issues
The framework is in the process of being updated for Spigot 1.9. The process is not complete and these are the known issues.
 * Views do not work properly due to what seems to be double fired events from Spigot/Minecraft. This may be fixed as Spigot is updated with new patches.
 * Due to changes in Minecraft sounds, the sounds are not fully working yet.

## Goals
 * Provide a centralized framework for common plugin utilities and services.
 * Reduce the amount of redundant code in plugins.
 * Speed up plugin development by abstracting common plugin features.
 * Create a user command interface that has commonality that users can learn to recognize.
 * Reduce server operator work by centralizing features that are used across plugins.

## Documentation
 * [Wiki](https://github.com/JCThePants/NucleusFramework/wiki)
 * [Doxygen](http://jcthepants.github.io/NucleusFramework/annotated.html)

## Resources
 * [NucleusLocalizer](https://github.com/JCThePants/NucleusLocalizer) - A console program used to generate language localization resource files for Nucleus based plugins.
 * [ResourcePackerMC](https://github.com/JCThePants/ResourcePackerMC) - A console program that aids in generating resource packs. Also generates resource-sounds.yml file used by NucleusFramework to define resource pack sounds.
 * [Scripting examples](https://github.com/JCThePants/NucleusScriptExamples) - Examples of cross plugin scripts using NucleusFramework.
 * [NpcTraitPack](https://github.com/JCThePants/NpcTraitPack) - A collection of NPC traits that can be used with a NucleusFramework NPC provider.

## Plugins using NucleusFramework
 * [PV-Star](https://github.com/JCThePants/PV-Star) - Extensible arena framework.
 * [TPRegions](https://github.com/JCThePants/TPRegions) - Portal and region teleport.
 * [PhantomPackets](https://github.com/JCThePants/PhantomPackets) - Sets viewable regions and entities to specific players.
 * [ArborianQuests](https://github.com/JCThePants/ArborianQuests) - Quest scripting plugin.
 * [RentalRooms](https://github.com/JCThePants/RentalRooms) - Rented regions that allow the tenant to only modify the interior of the house/room.
 * [RemoteConsole](https://github.com/JCThePants/RemoteConsole) - A remote console for Bukkit/Spigot servers.

## Providers
 * [CitizensNPCProvider](https://github.com/JCThePants/CitizensNpcProvider) - NPC Provider using [Citizens2](https://github.com/CitizensDev/Citizens2/) as its core.
 * [MySqlProvider](https://github.com/JCThePants/MySqlProvider) - MySql database provider.

## Plugin dependencies
 * [WorldEdit](https://github.com/sk89q/WorldEdit) - soft dependency, not required
 * [Vault](https://github.com/MilkBowl/Vault) - soft dependency, not required

## Build dependencies
See the [gradle script](https://github.com/JCThePants/NucleusFramework/blob/master/build.gradle) for build dependencies.

## Maven repository
You can include the latest NucleusFramework snapshot as a Maven dependency with the following:

    <repositories>
        <repository>
            <id>jcthepants-repo</id>
            <url>https://github.com/JCThePants/mvn-repo/raw/master</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.jcwhatever.bukkit</groupId>
            <artifactId>NucleusFramework</artifactId>
            <version>0.6-SNAPSHOT</version>
        </dependency>
    </dependencies>


