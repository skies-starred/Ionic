@file:Suppress("ObjectPrivatePropertyName", "Unused")

package foo.starred.ionic.module.impl.general.experiment.impl

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.GuiEvent
import foo.starred.ionic.event.impl.PacketEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.base.Module
import foo.starred.ionic.module.impl.general.experiment.solvers.base.IExperimentSolver
import foo.starred.ionic.module.impl.general.experiment.solvers.impl.ChronomatronExperimentSolver
import foo.starred.ionic.module.impl.general.experiment.solvers.impl.UltrasequencerExperimentSolver
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.gui.MinecraftGui
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.inventory.ContainerInput

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.PRIVATE_ISLAND, PresetSkyBlockIsland.THE_PARK])
object AutoExperiment : Module(
    "Auto experiment",
    "Automatically does the experiment minigames for you!",
    ConfigCategory.GENERAL
) {
    private val _unused0 by config.information("SkyHanni's experiment solver may break this!")
    private val `delay$min` by config.slider("Min click delay", 200, 100, 1000, "ms")
    private val `delay$max` by config.slider("Max click delay", 250, 100, 1000, "ms")
    val close by config.switch("Auto close")
    val serums by config.slider("Serums applied", 0, 0, 3)
    val max by config.switch("Get max")

    private var current: IExperimentSolver? = null
    private var click: Long = 0

    init {
        on<TickEvent.Client.Start> {
            val solver = current ?: return@on
            val player = client.player ?: return@on
            val screen = MinecraftGui.container ?: return@on

            val now = System.currentTimeMillis()
            if (now - click < delay()) return@on

            val slot = solver.next
            if (slot != null) {
                MinecraftGui.click(screen.menu.containerId, slot, clickType = ContainerInput.CLONE)
                click = now
            }

            if (!solver.close) return@on
            player.closeContainer()
            current = null
        }

        on<GuiEvent.Input.Mouse.Press> {
            if (current == null) return@on

            cancel()
        }

        on<GuiEvent.Open.Container> {
            current = when {
                stripped.startsWith("Chronomatron (") -> ChronomatronExperimentSolver
                stripped.startsWith("Ultrasequencer (") -> UltrasequencerExperimentSolver
                else -> null
            }

            current?.reset()
        }

        on<PacketEvent.Receive, ClientboundContainerSetSlotPacket> {
            current?.fn(MinecraftGui.container ?: return@on)
        }
    }

    private fun delay(): Long {
        return (`delay$min`..`delay$max`.coerceAtLeast(`delay$min`)).random().toLong()
    }
}
