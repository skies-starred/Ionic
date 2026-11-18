package foo.starred.ionic.module.impl.slayer

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.api.player.generic.GenericPlayerAPI
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.duck.entity.generic.EntityDuck.Companion.attachedNames
import foo.starred.ionic.event.impl.PlayerEvent
import foo.starred.ionic.event.impl.SlayerEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.base.Module
import foo.starred.kontext.abstraction.inventory.MinecraftInventory
import foo.starred.kontext.api.format.extension.string.stripped
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import kotlin.jvm.optionals.getOrNull

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.CRIMSON_ISLE])
object AutoDaggers : Module(
    "Auto daggers",
    "Automatically swaps to the correct dagger for blaze!",
    ConfigCategory.SLAYER
) {
    private val delay by config.slider("Delay", 1, 0, 10, "ticks")
    private val delayVariance by config.slider("Delay variance", 2, 0, 10, "ticks")

    private var last: Attunements? = null
    private var swap: Attunements? = null
    private var wait: Int = -1

    init {
        on<PlayerEvent.Attack.Entity> {
            for (line in entity.attachedNames) {
                val string = line.string
                if (":" !in string) continue
                if ("♨" !in string) continue

                val attunement = Attunements.get(string.stripped())?.takeIf { it != last } ?: continue

                last = attunement
                swap = attunement
                wait = delay + (0..delayVariance).random()

                break
            }
        }

        on<SlayerEvent.Reset.Any> {
            last = null
            swap = null
            wait = -1
        }

        on<TickEvent.Client.Start> {
            val swap = swap ?: return@on
            swap(swap)
        }
    }

    private fun swap(attunement: Attunements) {
        if (wait-- > 0) return
        val inventory = MinecraftInventory.instance ?: return

        val held = inventory.getItem(inventory.selectedSlot)
        val id = held.get<GenericKontextItemDataResolver.ID, String>()
        if (id in attunement.ids) {
            if (held.mode() != attunement.mode) GenericPlayerAPI.use()

            swap = null
            return
        }

        for (i in 0..8) {
            val stack = inventory.getItem(i)
            val id = stack.get<GenericKontextItemDataResolver.ID, String>() ?: continue
            if (id !in attunement.ids) continue
            if (inventory.selectedSlot == i) continue

            inventory.selectedSlot = i
            return
        }

        swap = null
    }

    private fun ItemStack.mode(): Int {
        return getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("td_attune_mode").getOrNull() ?: -1
    }

    private enum class Attunements(val display: String, val ids: Set<String>, val mode: Int) {
        Ashen("ASHEN ♨", setOf("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"), 0),
        Auric("AURIC ♨", setOf("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"), 1),
        Spirit("SPIRIT ♨", setOf("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"), 2),
        Crystal("CRYSTAL ♨", setOf("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"), 3);

        companion object {
            fun get(string: String): Attunements? {
                return entries.firstOrNull { it.display in string }
            }
        }
    }
}
