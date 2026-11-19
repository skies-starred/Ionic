package foo.starred.ionic.module.impl.general

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.GuiEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.base.Module
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.api.data.Observable
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.THE_PARK, PresetSkyBlockIsland.PRIVATE_ISLAND])
object AutoHarp : Module(
    "Auto harp",
    "Automatically does Melody's Harp for you!",
    ConfigCategory.GENERAL
) {
    private var state: Observable<Boolean> = Observable(false)
    private var hash: Int = 0

    init {
        on<GuiEvent.Open.Container> {
            if (!stripped.startsWith("Harp - ")) return@on
            if (screen.menu.type != MenuType.GENERIC_9x6) return@on

            hash = 0
            state.value = true
        }

        on<GuiEvent.Close.Any> {
            hash = 0
            state.value = false
        }

        on<TickEvent.Client.Start> {
            val screen = MinecraftGui.container ?: return@on
            val slots = screen.menu.slots

            var hash0 = 0
            var slot = -1

            for (index in 28..43) {
                val stack = slots.getOrNull(index)?.item ?: continue
                hash0 = 31 * hash0 + stack.item.hashCode()

                if (slot != -1) continue
                if (index !in 37..43) continue
                if (stack.item != Items.QUARTZ_BLOCK) continue

                slot = index
            }

            if (hash == hash0) return@on
            hash = hash0

            if (slot == -1) return@on
            MinecraftGui.click(screen.menu.containerId, slot, clickType = ContainerInput.CLONE)
        }.runWhen(state)
    }
}
