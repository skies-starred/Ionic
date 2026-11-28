package foo.starred.ionic.module.impl.dungeon

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.api.time.IonicScheduler
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.GuiEvent
import foo.starred.ionic.event.impl.PacketEvent
import foo.starred.ionic.module.base.Module
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.THE_CATACOMBS])
object AutoCloseChest : Module(
    "Auto close chest",
    "Automatically closes chests!",
    ConfigCategory.DUNGEON
) {
    private val mode = config.selector("Close mode", listOf("Auto", "Click")).unique("mode")

    private val minimum by config.slider("Minimum delay", 0, 0, 5, "ticks")
    private val maximum by config.slider("Maximum delay", 1, 0, 5, "ticks")

    private val set = setOf("Chest", "Large Chest")

    init {
        on<PacketEvent.Receive, ClientboundOpenScreenPacket> {
            if (title.string !in set) return@on

            val player = client.player ?: return@on
            it.cancel()

            val delay = (minimum..maximum).random()
            if (delay == 0) {
                player.closeContainer()
                return@on
            }

            IonicScheduler.schedule(delay.clientTicks) {
                player.closeContainer()
            }
        }.runWhen(mode.state.map { it == 0 })

        on<GuiEvent.Input.Mouse.Press> {
            val player = client.player ?: return@on

            val screen = MinecraftGui.container ?: return@on
            if (screen.title.string !in set) return@on

            cancel()
            player.closeContainer()
        }.runWhen(mode.state.map { it == 1 })

        on<GuiEvent.Input.Key.Press> {
            val player = client.player ?: return@on
            if (client.options.keyInventory.matches(keyEvent)) return@on

            val screen = MinecraftGui.container ?: return@on
            if (screen.title.string !in set) return@on

            cancel()
            player.closeContainer()
        }.runWhen(mode.state.map { it == 1 })
    }
}
