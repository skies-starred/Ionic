@file:Suppress("Unused")

package foo.starred.ionic.module.impl.general.wardrobe.keybind.impl

import com.mojang.blaze3d.platform.InputConstants
import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.time.IonicScheduler
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.GuiEvent
import foo.starred.ionic.module.base.Module
import foo.starred.ionic.module.impl.general.wardrobe.keybind.data.WardrobeKeybindSlot
import foo.starred.kbus.data.event.traits.KBusCancellableTrait
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.client.extension.boundKey
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.abstraction.input.impl.GenericInputState
import foo.starred.kontext.abstraction.input.impl.KeyboardInputState
import foo.starred.kontext.abstraction.input.impl.MouseInputState
import foo.starred.kontext.api.data.Observable
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.scheduling.scheduler.extensions.start

@LoadableIonicModule
@IonicModuleScope(skyblock = true)
object WardrobeKeybinds : Module(
    "Wardrobe keybinds",
    "Keybinds for wardrobe slots!",
    ConfigCategory.GENERAL
) {
    private val preventUnequip by config.switch("Prevent unequip")
    private val autoClose by config.switch("Auto close UI")
    private val cancelOther by config.switch("Cancel all other clicks")
    private val override by config.keybind("Key override", InputConstants.KEY_LCONTROL)
    private val ping by config.slider("Ping", 250, 10, 1000, "ms").description("Ping is used to estimate internal calculations.")

    private val keybinds by config.group("General keybinds")
    private val hotbar by keybinds.switch("Use hotbar binds", true)
    private val previous by keybinds.keybind("Previous page")
    private val next by keybinds.keybind("Next page")

    private val swaps by config.group("Swap keybinds")
    private val swapKey by swaps.switch("Swap key")
    private val swapKeybind by swaps.keybind("Swap keybind")
    private val swapKey1 by swaps.selector("Swap slot 1", listOf("Slot 1", "Slot 2", "Slot 3", "Slot 4", "Slot 5", "Slot 6", "Slot 7", "Slot 8", "Slot 9"))
    private val swapKey2 by swaps.selector("Swap slot 2", listOf("Slot 1", "Slot 2", "Slot 3", "Slot 4", "Slot 5", "Slot 6", "Slot 7", "Slot 8", "Slot 9"))

    private val slots0 by config.group("Slot keybinds")
    val key0 by slots0.keybind("Slot 1", InputConstants.UNKNOWN.value)
    val key1 by slots0.keybind("Slot 2", InputConstants.UNKNOWN.value)
    val key2 by slots0.keybind("Slot 3", InputConstants.UNKNOWN.value)
    val key3 by slots0.keybind("Slot 4", InputConstants.UNKNOWN.value)
    val key4 by slots0.keybind("Slot 5", InputConstants.UNKNOWN.value)
    val key5 by slots0.keybind("Slot 6", InputConstants.UNKNOWN.value)
    val key6 by slots0.keybind("Slot 7", InputConstants.UNKNOWN.value)
    val key7 by slots0.keybind("Slot 8", InputConstants.UNKNOWN.value)
    val key8 by slots0.keybind("Slot 9", InputConstants.UNKNOWN.value)

    private val menuRegex: Regex = Regex("^\\((?<cur>\\d)/(?<max>\\d)\\) Armor Sets$")
    private val open: Observable<Boolean> = Observable(false)

    private var current: Int = 0
    private var max: Int = 0
    private var last: Long = 0

    init {
        on<GuiEvent.Open.Container> {
            val (i0, i1) = menuRegex.find(stripped)?.destructured ?: return@on

            open.value = true
            current = i0.toInt()
            max = i1.toInt()
        }

        on<GuiEvent.Close.Container> {
            reset()
        }

        on<GuiEvent.Input.Key.Press> {
            fn(KeyboardInputState.vanilla(keyEvent.key))
        }.runWhen(open)

        on<GuiEvent.Input.Mouse.Press> {
            fn(MouseInputState.vanilla(keyEvent.button()))
        }.runWhen(open)
    }

    private fun KBusCancellableTrait.fn(key: InputConstants.Key) {
        val bool0 = GenericInputState.pressed(override)
        val bool1 = key == client.options.keyInventory.boundKey()
        val bool2 = key.value == InputConstants.KEY_ESCAPE
        if (cancelOther && !bool0 && !bool1 && !bool2) cancel()

        if (System.currentTimeMillis() - last < ping) return
        val player = client.player ?: return

        if (key == previous) {
            if (current > 1) click(player.containerMenu.containerId, 45, false)
            return
        }

        if (key == next) {
            if (current < max) click(player.containerMenu.containerId, 53, false)
            return
        }

        if (swapKey && key == swapKeybind) {
            if (swapKey1 == swapKey2) return
            val slot1 = WardrobeKeybindSlot.ALL.find { it.index == swapKey1 + 36 }?.takeIf { it.slot?.item?.isEmpty == false } ?: return
            val slot2 = WardrobeKeybindSlot.ALL.find { it.index == swapKey2 + 36 }?.takeIf { it.slot?.item?.isEmpty == false } ?: return
            val index = if (slot1.equipped) slot2.index else slot1.index

            click(player.containerMenu.containerId, index)
            return
        }

        val slot = WardrobeKeybindSlot.ALL.find { it.value(hotbar) == key }?.takeIf { it.slot?.item?.isEmpty == false } ?: return // slot can be empty on high ping, yay!
        if (slot.equipped && preventUnequip) return

        click(player.containerMenu.containerId, slot.index)
    }

    private fun KBusCancellableTrait.click(id: Int, index: Int, close: Boolean = true) {
        MinecraftGui.click(id, index)
        last = System.currentTimeMillis()
        cancel()

        if (!close) return
        if (!autoClose) return
        IonicScheduler.schedule((1..3).random().clientTicks.start) {
            val player = client.player ?: return@schedule
            if (player.containerMenu.containerId != id) return@schedule

            player.closeContainer()
        }
    }

    private fun reset() {
        open.value = false
        current = 0
        max = 0
        last = 0
    }
}
