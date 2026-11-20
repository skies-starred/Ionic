@file:Suppress("PropertyName")

package foo.starred.ionic.module.impl.general.loadout.auto.data

import com.mojang.blaze3d.platform.InputConstants
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key0
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key1
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key10
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key11
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key2
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key3
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key4
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key5
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key6
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key7
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key8
import foo.starred.ionic.module.impl.general.loadout.auto.impl.AutoLoadout.key9
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.format.extension.string.stripped
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot

data class AutoLoadoutSlot(
    val _index: Int,
    val index: Int,
    val keybind: () -> InputConstants.Key
) {
    val slot: Slot?
        get() = client.player?.containerMenu?.slots?.getOrNull(index)

    val equipped: Boolean
        get() {
            val lore = slot?.item?.get<GenericKontextItemDataResolver.LORE, List<Component>>() ?: return false
            return lore.getOrNull(lore.lastIndex - 1)?.stripped()?.isEmpty() ?: false
        }

    companion object {
        val ALL = listOf(
            AutoLoadoutSlot(0, 14) { key0 },
            AutoLoadoutSlot(1, 15) { key1 },
            AutoLoadoutSlot(2, 16) { key2 },
            AutoLoadoutSlot(3, 23) { key3 },
            AutoLoadoutSlot(4, 24) { key4 },
            AutoLoadoutSlot(5, 25) { key5 },
            AutoLoadoutSlot(6, 32) { key6 },
            AutoLoadoutSlot(7, 33) { key7 },
            AutoLoadoutSlot(8, 34) { key8 },
            AutoLoadoutSlot(9, 41) { key9 },
            AutoLoadoutSlot(10, 42) { key10 },
            AutoLoadoutSlot(11, 43) { key11 }
        )
    }
}
