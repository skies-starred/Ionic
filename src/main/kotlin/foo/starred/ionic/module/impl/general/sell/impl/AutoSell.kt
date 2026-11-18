@file:Suppress("UNUSED")

package foo.starred.ionic.module.impl.general.sell.impl

import com.mojang.serialization.Codec
import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.command.command
import foo.starred.ionic.api.messaging.impl.MessagingAPI.displayAsMod
import foo.starred.ionic.api.storage.JsonStore
import foo.starred.ionic.api.time.IonicScheduler
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.GuiEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.base.Module
import foo.starred.ionic.module.impl.general.sell.ui.impl.AutoSellInventoryUI
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.chat.message.MinecraftMessages
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.api.data.Observable
import foo.starred.kontext.api.format.extension.string.stripped
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import foo.starred.kontext.api.scheduling.scheduler.extensions.serverTicks
import foo.starred.kontext.api.storage.impl.generic.extension.mutableSet
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Items

@LoadableIonicModule
@IonicModuleScope(skyblock = true)
object AutoSell : Module(
    "Auto sell",
    "Automatically sells items",
    ConfigCategory.GENERAL
) {
    private val delay by config.slider("Delay", 6, 2, 10, "ticks")
    private val jitter by config.slider("Jitter", 1, 0, 5, "ticks")
    private val click by config.button("Click to open manager") { AutoSellInventoryUI.open() }
    private val defaults by config.button("Add default items to list") { allowed.value.defaults() }
    private val info by config.information("Command: <red>/ionic autoSell [add|remove|list|clear]")

    private val filters by config.group("Filters")
    private val skip50 by filters.switch("Skip 50/50 items", true)

    private val json = JsonStore("feature/general/autoSell")
    val allowed = json.mutableSet("allowed", Codec.STRING)
    val blocked = json.mutableSet("blocked", Codec.STRING)

    private val suffixRegex = Regex(" x\\d+$")
    private val state = Observable(false)

    private var last = 0L
    private var next = 0L

    init {
        on<GuiEvent.Open.Container> {
            IonicScheduler.schedule(2.serverTicks) {
                val item = screen.menu.slots.getOrNull(49)?.item ?: return@schedule
                val bool0 = item.item == Items.HOPPER && item.hoverName.stripped() == "Sell Item"
                val bool1 = item.get<GenericKontextItemDataResolver.LORE, List<Component>>()?.lastOrNull()?.stripped() == "Click to buyback!"
                if (!bool0 && !bool1) return@schedule

                state.value = true
            }
        }

        on<GuiEvent.Close.Any> {
            state.value = false
        }

        on<TickEvent.Client.Start> {
            val menu = MinecraftGui.container?.menu ?: return@on
            val now = System.currentTimeMillis()
            if (now - last < next) return@on

            for (slot in menu.slots) {
                if (slot.container !is Inventory) continue

                val stack = slot.item.takeIf { !it.isEmpty } ?: continue
                val name = stack.hoverName.stripped().replace(suffixRegex, "")

                val quality = stack.get<GenericKontextItemDataResolver.QUALITY, Int>()
                if (quality == 50 && skip50) continue

                if (allowed.value.none { name in it }) continue
                if (blocked.value.any { name in it }) continue

                MinecraftGui.click(menu.containerId, slot.index, clickType = ContainerInput.QUICK_MOVE)
                last = now
                next = ((delay + (0..jitter).random()) * 50).toLong()

                break
            }
        }.runWhen(state)
    }

    init {
        command {
            "autoSell".then {
                "add" / "allowed" / string("name") {
                    val name = string("name")
                    if (allowed.value.any { it.equals(name, true) }) return@string "Already exists in the allowed list!".displayAsMod()

                    allowed.update { add(name) }
                    "Added \"$name\" to allowed list!".displayAsMod()
                }

                "add" / "blocked" / string("name") {
                    val name = string("name")
                    if (blocked.value.any { it.equals(name, true) }) return@string "Already exists in the blocked list!".displayAsMod()

                    blocked.update { add(name) }
                    "Added \"$name\" to blocked list!".displayAsMod()
                }

                "remove" / "allowed" / string("name") {
                    val name = string("name")
                    if (allowed.value.none { it.equals(name, true) }) return@string "Does not exist in the allowed list!".displayAsMod()

                    allowed.update { remove(name) }
                    "Removed \"$name\" from allowed list!".displayAsMod()
                }

                "remove" / "blocked" / string("name") {
                    val name = string("name")
                    if (blocked.value.none { it.equals(name, true) }) return@string "Does not exist in the blocked list!".displayAsMod()

                    blocked.update { remove(name) }
                    "Removed \"$name\" from blocked list!".displayAsMod()
                }

                "list" / "allowed" {
                    "Auto sell allowed list (${allowed.value.size}):".displayAsMod()

                    for (item in allowed.value) {
                        MinecraftMessages.display(" <dark_gray>- <aqua>${item}")
                    }
                }

                "list" / "blocked" {
                    "Auto sell blocked list (${blocked.value.size}):".displayAsMod()

                    for (item in blocked.value) {
                        MinecraftMessages.display(" <dark_gray>- <aqua>${item}")
                    }
                }

                "clear" / "allowed" {
                    allowed.update { clear() }
                    "Cleared allowed list!".displayAsMod()
                }

                "clear" / "blocked" {
                    blocked.update { clear() }
                    "Cleared blocked list!".displayAsMod()
                }
            }
        }
    }

    private fun MutableCollection<String>.defaults() {
        addAll(arrayOf(
            "enchanted ice", "superboom tnt", "rotten", "skeleton master", "skeleton grunt", "cutlass",
            "skeleton lord", "skeleton soldier", "zombie soldier", "zombie knight", "zombie commander", "zombie lord",
            "skeletor", "super heavy", "heavy", "sniper helmet", "dreadlord", "earth shard", "zombie commander whip",
            "machine gun", "sniper bow", "soulstealer bow", "silent death", "training weight",
            "beating heart", "premium flesh", "mimic fragment", "enchanted rotten flesh", "sign",
            "enchanted bone", "defuse kit", "optical lens", "tripwire hook", "button", "carpet", "lever", "diamond atom",
            "healing viii splash potion", "healing 8 splash potion", "candycomb"
        ))
    }
}
