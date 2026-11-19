@file:Suppress("ObjectPrivatePropertyName")

package foo.starred.ionic.module.impl.slayer

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.slayer.SlayerAPI
import foo.starred.ionic.api.time.IonicScheduler
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.SlayerEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.base.Module
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.chat.command.MinecraftCommands
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.scheduling.scheduler.extensions.start
import net.minecraft.world.entity.Entity

@LoadableIonicModule
@IonicModuleScope(skyblock = true)
object AutoTwilight : Module(
    "Auto twilight",
    "Automatically gets twilight arrow poison for your slayer bosses.",
    ConfigCategory.SLAYER
) {
    private val general by config.group("General", false)
    private val spawn = general.switch("Get on spawn").unique("spawn")
    private val `spawn$amount` by general.slider("Amount to get", 2, 1, 64)

    private val enderman by config.group("Enderman", false)
    private val hits = enderman.switch("Get on hit phase start").unique("hits")
    private val `hits$amount` by enderman.slider("Amount to get", 2, 1, 64)

    private var vehicle: Entity? = null
    private var triggered = false

    init {
        on<SlayerEvent.Boss.Spawn> {
            if (!slayerInfo.owned) return@on

            val random = (0..2).random()
            if (random == 0) return@on refill(`spawn$amount`)

            IonicScheduler.schedule(random.clientTicks.start) {
                refill(`spawn$amount`)
            }
        }.runWhen(spawn.state)

        on<TickEvent.Client.End> {
            val slayer = SlayerAPI.slayer?.entity ?: return@on
            val current = slayer.vehicle

            if (current != null && current !== vehicle) {
                vehicle = current
                triggered = false
            }

            val tracked = vehicle ?: return@on
            if (triggered) return@on
            if (current === tracked && tracked.tickCount < 164) return@on

            triggered = true
            refill(`hits$amount`)
        }.runWhen(hits.state)
    }

    private fun refill(amount: Int) {
        val count = client.player?.inventory?.find { it.get<GenericKontextItemDataResolver.ID, String>() == "TWILIGHT_ARROW_POISON" }?.count ?: 0
        if (count >= amount) return

        MinecraftCommands.client("/gfs TWILIGHT_ARROW_POISON ${amount - count}")
    }
}
