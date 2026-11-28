@file:Suppress("ObjectPrivatePropertyName", "Unused")

package foo.starred.ionic.module.impl.dungeon

import com.mojang.serialization.Codec
import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.command.command
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.api.messaging.impl.MessagingAPI.displayAsMod
import foo.starred.ionic.api.player.generic.GenericPlayerAPI
import foo.starred.ionic.api.storage.JsonStore
import foo.starred.ionic.api.time.IonicScheduler
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.PlayerEvent
import foo.starred.ionic.module.base.Module
import foo.starred.kontext.abstraction.chat.message.MinecraftMessages
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.abstraction.inventory.MinecraftInventory
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.scheduling.scheduler.extensions.start
import foo.starred.kontext.api.storage.impl.generic.extension.mutableSet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.BlockHitResult

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.THE_CATACOMBS])
object AutoSuperboom : Module(
    "Auto superboom",
    "Automatically swaps to and uses the superboom if clicking on a breakable wall.",
    ConfigCategory.DUNGEON
) {
    private val _unused by config.information("Command: <red>/ionic superboom [add|remove]")

    private val minDelay by config.slider("Minimum delay", 1, 1, 5, "ticks")
    private val maxDelay by config.slider("Maximum delay", 3, 1, 5, "ticks")

    private val swapBack by config.switch("Swap back")
    private val `swapBack$minDelay` by config.slider("Minimum delay", 1, 1, 5, "ticks")
    private val `swapBack$maxDelay` by config.slider("Maximum delay", 3, 1, 5, "ticks")
    private val `swapBack$type` by config.selector("Swap to", listOf("Original slot", "Custom slot"))
    private val `swapBack$custom` by config.slider("Custom slot number", 1, 1, 9)

    private val json = JsonStore("feature/dungeon/autoSuperboom")
    private val breakable = json.mutableSet("breakable", Codec.STRING, mutableSetOf("minecraft:cracked_stone_bricks", "minecraft:barrier"))

    private val set = setOf("SUPERBOOM_TNT", "INFINITE_SUPERBOOM_TNT")

    init {
        on<PlayerEvent.Attack.Block> {
            if (MinecraftGui.screen != null) return@on
            val inventory = MinecraftInventory.instance ?: return@on

            val block = client.level?.getBlockState(pos)?.block ?: return@on
            if (BuiltInRegistries.BLOCK.getKey(block).toString() !in breakable.value) return@on

            val held = inventory.selectedSlot
            val tnt = MinecraftInventory.hotbar?.indexOfFirst { it.get<GenericKontextItemDataResolver.ID, String>() in set }?.takeIf { it != -1 } ?: return@on

            cancel()
            IonicScheduler.schedule((minDelay..maxDelay).random().clientTicks.start) {
                inventory.selectedSlot = tnt

                IonicScheduler.schedule(1.clientTicks.start) {
                    GenericPlayerAPI.attack()

                    if (!swapBack) return@schedule
                    IonicScheduler.schedule((`swapBack$minDelay`..`swapBack$maxDelay`).random().clientTicks.start) {
                        inventory.selectedSlot = if (`swapBack$type` == 0) held else `swapBack$custom` - 1
                    }
                }
            }
        }

        command {
            "superboom" / "add" {
                val hit = client.hitResult as? BlockHitResult ?: return@invoke "Not looking at a block!".displayAsMod()
                val block = client.level?.getBlockState(hit.blockPos)?.block ?: return@invoke

                val id = BuiltInRegistries.BLOCK.getKey(block)
                if (id.toString() in breakable.value) return@invoke "Block already in breakable list!".displayAsMod()

                breakable.update { add(id.toString()) }
                "Added \"${id.path}\" to the breakable block list!".displayAsMod()
            }

            "superboom" / "add" / string("block") {
                val it = "minecraft:${string("block")}"
                if (Identifier.tryParse(it) == null) return@string "Invalid block id, or format! Try the command \"/nebulune superboom add\" while looking at the block.".displayAsMod()
                if (it in breakable.value) return@string "Block already in breakable list!".displayAsMod()

                breakable.update { add(it) }
                "Added \"${it.substringAfter(":")}\" to the breakable block list!".displayAsMod()
            }

            "superboom" / "remove" {
                val hit = client.hitResult as? BlockHitResult ?: return@invoke "Not looking at a block!".displayAsMod()
                val block = client.level?.getBlockState(hit.blockPos)?.block ?: return@invoke

                val id = BuiltInRegistries.BLOCK.getKey(block)
                if (id.toString() !in breakable.value) return@invoke "Block not in breakable list!".displayAsMod()

                breakable.update { remove(id.toString()) }
                "Removed \"${id.path}\" from the breakable block list!".displayAsMod()
            }

            "superboom" / "remove" / string("block") {
                val it = "minecraft:${string("block")}"
                if (Identifier.tryParse(it) == null) return@string "Invalid block id, or format! Try the command \"/nebulune superboom add\" while looking at the block.".displayAsMod()
                if (it !in breakable.value) return@string "Block not in breakable list!".displayAsMod()

                breakable.update { remove(it) }
                "Removed \"${it.substringAfter(":")}\" from the breakable block list!".displayAsMod()
            }

            "supeboom" / "list" {
                "Breakable block list:".displayAsMod()

                for (block in breakable.value) {
                    val a = block.substringBefore(":")
                    val b = block.substringAfter(":")

                    MinecraftMessages.display(" <dark_gray>- <gray>$a:<r>$b")
                }
            }
        }
    }
}
