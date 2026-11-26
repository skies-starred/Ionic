package foo.starred.ionic.event.impl

import com.mojang.datafixers.util.Pair
import foo.starred.ionic.event.core.IonicEvent
import foo.starred.kontext.api.format.extension.string.stripped
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack

sealed class EntityEvent {
    data class Load(
        val entity: Entity
    ) : IonicEvent()

    data class Unload(
        val entity: Entity
    ) : IonicEvent()

    data class Death(
        val entity: Entity
    ) : IonicEvent()

    sealed class Update {
        data class Attach(
            val component: Component,
            val entity: Entity
        ) : IonicEvent() {
            val stripped: String by lazy(LazyThreadSafetyMode.NONE) {
                component.stripped()
            }
        }

        data class Named(
            val component: Component,
            val entity: ArmorStand
        ) : IonicEvent() {
            val stripped: String by lazy(LazyThreadSafetyMode.NONE) {
                component.stripped()
            }
        }

        data class Equipment(
            val entity: LivingEntity,
            val slots: List<Pair<EquipmentSlot, ItemStack>>
        ) : IonicEvent()
    }
}
