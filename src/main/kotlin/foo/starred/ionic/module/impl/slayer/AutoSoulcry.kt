package foo.starred.ionic.module.impl.slayer

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.api.player.generic.GenericPlayerAPI
import foo.starred.ionic.api.slayer.SlayerAPI
import foo.starred.ionic.api.slayer.enums.type.impl.SlayerBoss
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.PlayerEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.base.Module
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import net.minecraft.world.phys.EntityHitResult

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.THE_END])
object AutoSoulcry : Module(
    "Auto soulcry",
    "Automatically uses the soulcry ability of your katana!",
    ConfigCategory.SLAYER,
) {
    private val hitbox by config.switch("Check boss hitbox", true)
    private val minDelay by config.slider("Min delay", 1, 0, 5, "ticks")
    private val maxDelay by config.slider("Max delay", 3, 0, 5, "ticks")
    private val detectType = config.multiSelector("Detection type", listOf("Tick based", "Attack based")).unique("detectType")
    private val otherBosses by config.switch("Work on other's bosses").description("The option <red>\"Work on other's bosses\"<r> requires you to have <red>Attack based<r> selected in Detection type!")

    private val ids = setOf("VOIDEDGE_KATANA", "VORPAL_KATANA", "ATOMSPLIT_KATANA")
    private var tick = -1

    init {
        on<TickEvent.Client.Start> {
            val slayer = SlayerAPI.slayer
            if (slayer?.type != SlayerBoss.Voidgloom) return@on reset()
            if (MinecraftGui.screen != null) return@on reset()

            val player = client.player ?: return@on reset()
            val item = player.mainHandItem

            if (item.get<GenericKontextItemDataResolver.UUID, String>() !in ids) return@on reset()
            if (hitbox && (client.hitResult as? EntityHitResult)?.entity != slayer.entity) return@on
            if (player.cooldowns.isOnCooldown(item)) return@on

            if (tick == -1) return@on ::tick.set((minDelay..maxDelay.coerceAtLeast(minDelay)).random())
            if (tick-- > 0) return@on

            GenericPlayerAPI.use()
            reset()
        }.runWhen(detectType.state.map { 0 in it })

        on<PlayerEvent.Attack.Entity> {
            val info = SlayerAPI.bosses[entity] ?: return@on
            if (!info.owned && !otherBosses) return@on

            val player = client.player ?: return@on reset()
            val item = player.mainHandItem

            if (item.get<GenericKontextItemDataResolver.UUID, String>() !in ids) return@on
            if (player.cooldowns.isOnCooldown(item)) return@on

            GenericPlayerAPI.use()
        }.runWhen(detectType.state.map { 1 in it })
    }

    private fun reset() {
        tick = -1
    }
}
