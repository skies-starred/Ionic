@file:Suppress("ObjectPrivatePropertyName")

package foo.starred.ionic.module.impl.general.refill.impl

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.api.messaging.impl.MessagingAPI.displayAsMod
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.MessageEvent
import foo.starred.ionic.module.base.Module
import foo.starred.ionic.module.impl.general.refill.data.AutoRefillItemData
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.chat.command.MinecraftCommands
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver

@LoadableIonicModule
@IonicModuleScope(skyblock = true)
object AutoRefill : Module(
    "Auto refill",
    "Automatically refills selected items",
    ConfigCategory.GENERAL
) {
    private val dungeons by config.group("Dungeons")
    private val `dungeons$start` by dungeons.switch("Refill on dungeon start", true)
    private val `dungeons$puzzle` by dungeons.switch("Get draft on puzzle fail", true)
    private val `dungeons$start$refill` by dungeons.multiSelector("Items to refill", AutoRefillItemData.entries.map { it.display }, AutoRefillItemData.entries.map { it.ordinal })

    private val kuudra by config.group("Kuudra")
    private val `kuudra$pearl` by kuudra.switch("Refill pearls on supply", true)
    private val `kuudra$build` by kuudra.switch("Refill toxic on build end", true)
    private val `kuudra$build$amount` by kuudra.slider("Toxic amount to get", 32, 1, 64)

    private val kuudraRegex = Regex("(?:\\[[^]]*] )?(?<user>\\w+) recovered one of Elle's supplies! \\(\\d+/\\d+\\)")

    init {
        on<MessageEvent.Chat.Receive> {
            when {
                `kuudra$pearl` -> {
                    val name = kuudraRegex.matchEntire(stripped)?.groups["user"]?.value
                    if (name == client.user.name) AutoRefillItemData.ENDER_PEARL.refill()
                }

                `kuudra$build` && stripped == "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> {
                    toxic(`kuudra$build$amount`)
                }
            }
        }.runWhen(PresetSkyBlockIsland.KUUDRA.state)

        on<MessageEvent.Chat.Receive> {
            if (`dungeons$puzzle` && stripped.startsWith("PUZZLE FAIL! ")) {
                "Getting draft on puzzle fail...".displayAsMod()
                MinecraftCommands.client("/gfs architect's first draft 1")
            }

            if (`dungeons$start` && stripped == "[NPC] Mort: Here, I found this map when I first entered the dungeon.") {
                for (entry in AutoRefillItemData.entries) {
                    if (entry.ordinal !in `dungeons$start$refill`) continue
                    entry.refill()
                }
            }
        }.runWhen(PresetSkyBlockIsland.THE_CATACOMBS.state)
    }

    private fun toxic(amount: Int) {
        val count = client.player?.inventory?.find { it.get<GenericKontextItemDataResolver.ID, String>() == "TOXIC_ARROW_POISON" }?.count ?: 0
        if (count >= amount) return

        MinecraftCommands.client("/gfs TOXIC_ARROW_POISON ${amount - count}")
    }
}
