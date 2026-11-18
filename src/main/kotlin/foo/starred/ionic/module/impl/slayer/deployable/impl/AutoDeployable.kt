@file:Suppress("UNUSED")

package foo.starred.ionic.module.impl.slayer.deployable.impl

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.command.command
import foo.starred.ionic.api.messaging.impl.MessagingAPI.displayAsMod
import foo.starred.ionic.api.player.generic.GenericPlayerAPI
import foo.starred.ionic.api.time.IonicScheduler
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.SlayerEvent
import foo.starred.ionic.module.base.Module
import foo.starred.ionic.module.impl.slayer.deployable.data.AutoDeployableEntries
import foo.starred.kontext.abstraction.inventory.MinecraftInventory
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.scheduling.scheduler.extensions.start
import foo.starred.kontext.api.scheduling.scheduler.impl.AbstractScheduler

@LoadableIonicModule
@IonicModuleScope(skyblock = true)
object AutoDeployable : Module(
    "Auto deployable",
    "Automatically uses deployables if they're in your inventory.",
    ConfigCategory.SLAYER
) {
    private val swapDelay by config.slider("Swap delay", 2, 1, 5, "ticks")
    private val clickDelay by config.slider("Click delay", 2, 1, 5, "ticks")
    private val delayVariance by config.slider("Delay Variance", 3, 0, 5, "ticks")

    private val _unused by config.information("Command to swap: <red>/ionic auto deployable")

    init {
        command {
            "auto" / "deployable" {
                swap()
                "Swapping to deployable...".displayAsMod()
            }
        }

        on<SlayerEvent.Boss.Spawn> {
            if (!slayerInfo.owned) return@on

            swap()
        }
    }

    private fun swap() {
        val found = AutoDeployableEntries.find() ?: return
        val current = MinecraftInventory.instance?.selectedSlot ?: return
        if (found == current) return

        IonicScheduler.schedule(delay(swapDelay)) {
            MinecraftInventory.selected = found

            IonicScheduler.schedule(delay(clickDelay)) {
                GenericPlayerAPI.use()

                IonicScheduler.schedule(delay(swapDelay)) {
                    if (MinecraftInventory.selected != found) return@schedule

                    MinecraftInventory.selected = current
                }
            }
        }
    }

    private fun delay(delay: Int): AbstractScheduler.ClientTicks {
        return (delay..delay + (0..delayVariance).random()).random().clientTicks.start
    }
}
