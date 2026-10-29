MalisisCore
=============

MalisisCore is a framework dedicated to simplifying many processes required during mod development. It is licensed under the [MIT License].

This is AlmuraDev's MalisisCore for Minecraft 1.12.2, based on [Ordinastie's MalisisCore][Upstream]. It is used by [Almura], AlmuraDev's [MalisisDoors] and [SGCraft].

* Issues: [GitHub issues](https://github.com/AlmuraDev/MalisisCore/issues)

## Versions
| Branch  | Version | Status |
|:-------:|:-------:|:-------|
| `6.6.x` | 6.6.0 (snapshot) | Default branch. Ordinastie's reworked GUI system (from his unreleased `1.12` branch) with every 6.5.x change carried over, and a compatibility layer for code written for 6.5.x. Being tested in Almura, MalisisDoors and SGCraft. |
| `6.5.x` | 6.5.x | The previous GUI system. What released builds of the mods above use until 6.6.0 is released. |

## Features
* ASM and Mixin manipulation (MalisisCore is a coremod)
* Rendering
  * Shape manipulation
  * Animations
  * Graphical User Interface
* Enhanced Inventory System
* Linked GUI Settings System
* Utility Classes (Entity, Items, etc)

## Requirements
| | |
|:--|:--|
| Minecraft | 1.12.2 |
| Forge | 14.23.5.2838 (MCP mappings `snapshot_20180808`) |
| [Java] | 8 (JDK) |
| [Gradle] | Use the included wrapper (`gradlew`, Gradle 4.1); ForgeGradle 2.3 |

## Cloning
`git clone https://github.com/AlmuraDev/MalisisCore.git` (the default branch is `6.6.x`)

## Setup
__Note:__ Use `./gradlew` on Unix systems or Git Bash, and `gradlew.bat` in a Windows command prompt, in place of any `gradle` command.

  1. Run `gradlew setupDecompWorkspace --refresh-dependencies`
  2. __For [IntelliJ]__: open the folder (or **build.gradle**) as a Gradle project, then run `gradlew genIntellijRuns`  
     __For [Eclipse]__: run `gradlew eclipse`, then import MalisisCore as an existing project

## Running
`genIntellijRuns` creates **Minecraft Client** and **Minecraft Server** run configurations. The Gradle start classes (`GradleStart` / `GradleStartServer`) already load the MalisisCore coremod, so no VM options are needed. The working directory is `./run`.

When launching the server for the first time, it will shut down by itself: set `online-mode=false` in server.properties and `eula=true` in eula.txt (this means you agree to the Mojang EULA).

## Building
Run `gradlew build install`. The JARs are in `./build/libs`:

| JAR | Use |
|:----|:----|
| `malisiscore-1.12.2-6.6.0-SNAPSHOT.jar` | Obfuscated, for a normal Forge install (the mods folder of a client or server). Includes Mixin. |
| `malisiscore-1.12.2-6.6.0-SNAPSHOT-dev.jar` | Deobfuscated, for development environments |
| `-sources` / `-javadoc` | Sources and Javadoc |

`install` also puts these in your local Maven repository (`~/.m2`), where the projects using MalisisCore pick them up.

## Using
To use MalisisCore as a dependency, add the following to your build.gradle.

```
repositories {
    mavenLocal()
}

dependencies {
    deobfProvided 'com.almuradev:malisiscore:1.12.2-6.6.0-SNAPSHOT'
}
```

MalisisCore is a coremod that also contains a mod. In your mod's `@Mod` annotation, require it, and for 6.6.0 require at least the snapshot:

```
dependencies = "required-after:malisiscore@[1.12.2-6.6.0-SNAPSHOT,)"
```

Forge compares versions the way Maven does, where `-SNAPSHOT` sorts *before* its release, so a lower bound of `1.12.2-6.6.0` would turn away the 6.6.0 snapshot.

## Upgrading from 6.5.x
6.6.0 keeps a compatibility layer, so most 6.5.x code needs only mechanical changes:

* Components no longer take the GUI in their constructors: `new UILabel(this, text)` becomes `new UILabel(text)` (likewise `UIButtonBuilder`, `BasicForm`, `BasicContainer`, `BasicList`, `UICheckBox`, `UIRadioButton`, `UIInventory`, `UIPlayerInventory`). The old constructors are kept as `@Deprecated`.
* `UISeparator(gui)` becomes `UISeparator(false)` (horizontal); a single line `UITextField(gui, text, false)` becomes `UITextField(text)`.
* `MalisisGui.update(mouseX, mouseY, partialTick)` becomes `update()`; the mouse position is `MalisisGui.MOUSE_POSITION`. `onClick(x, y)` becomes `onClick()`, and a `mouseClicked` override becomes `mousePressed(UIComponent, int)`.
* `UIComponent` and `UIContainer` no longer take a type parameter.
* `GuiTexture`, `GuiIcon` and `GuiRenderer` are in `net.malisis.core.client.gui.render`. `texture.getIcon(x, y, w, h)` becomes `new GuiIcon(texture, x, y, w, h)`; a whole image is `new GuiIcon(new GuiTexture(location, 1, 1), 0F, 0F, 1F, 1F)`.
* `UIWindow` is gone: use `UIContainer.window()` and add a title label.
* `UISelect.setLabelPattern(...)` becomes `setLabelFunction(...)`.
* 6.6 setters and `register` don't chain.

Kept from 6.5.x by the compatibility layer: `Anchor`, `setPosition(x, y, anchor)`, `setSize(width, height)` (zero or less is relative to the parent), `getX()`, `getWidth()`, `UIButton.ClickEvent`, and the `Basic*` components.

Layout differences to check in game:
* `setSize(width, height)` on a button is the least it may be: buttons grow to fit their text, as in 6.5.x. For an exact size use `setSize(Size.of(width, height))`.
* Checkboxes are 13 pixels high (10 in 6.5.x).
* A `UITabGroup` resets its tab containers to its window's top left at full size when attached, and tabs on the left are centred: position them again after `attachTo`.
* A read only `BasicTextArea` greys its text; set its font options back if it should read normally.
* Draw anything of your own (models, entities) after `super.drawScreen(...)`, outside MalisisCore's renderer, or bind textures yourself: binding one through the renderer flushes what it is drawing.

## FAQ
__I installed a new 6.6.0 snapshot, but my mod's dev client still runs the old one__
>ForgeGradle keeps the deobfuscated copy of a dependency under `~/.gradle/caches/minecraft/deobfedDeps`, and a snapshot keeps the same version. Delete `deobfedDeps/deobf/com/almuradev/malisiscore/1.12.2-6.6.0-SNAPSHOT`, then run a build of your mod (and reload Gradle in your IDE) to make a new one.

__Why do I get `Zip file rt.jar failed to read properly` in IntelliJ?__
>This is the result of Forge attempting to classload the Java runtime JAR. It doesn't cause any harm to your development and can be ignored.

__A dependency was added, but my IDE is missing it! How do I add it?__
>Reload the Gradle project in your IDE, or restart it, and the Gradle plugin will pull in the new dependencies.

__Help! Things are not working!__
>Some issues are fixed by deleting the `.gradle` folder in your user directory and running through the setup steps again. Otherwise, open a [GitHub issue](https://github.com/AlmuraDev/MalisisCore/issues).

[Almura]: https://github.com/AlmuraDev/Almura
[Eclipse]: http://www.eclipse.org/
[Gradle]: http://www.gradle.org/
[IntelliJ]: http://www.jetbrains.com/idea/
[Java]: http://java.oracle.com/
[MalisisDoors]: https://github.com/AlmuraDev/MalisisDoors
[MIT License]: http://www.tldrlegal.com/license/mit-license
[SGCraft]: https://github.com/AlmuraDev/SGCraft
[Upstream]: https://github.com/Ordinastie/MalisisCore
