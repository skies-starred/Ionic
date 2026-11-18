package foo.starred.ionic.event.impl

import foo.starred.ionic.event.core.IonicEvent
import foo.starred.kbus.data.event.traits.KBusCancellableTrait
import net.minecraft.core.BlockPos

sealed class PlayerEvent {
    sealed class Interact {
        data object None : IonicEvent(), KBusCancellableTrait

        data class Block(val pos: BlockPos)  : IonicEvent(), KBusCancellableTrait

        data class Entity(val entity: net.minecraft.world.entity.Entity) : IonicEvent(), KBusCancellableTrait

        data object Any : IonicEvent(), KBusCancellableTrait
    }

    sealed class Attack {
        data class Block(val pos: BlockPos) : IonicEvent(), KBusCancellableTrait

        data class Entity(val entity: net.minecraft.world.entity.Entity) : IonicEvent(), KBusCancellableTrait

        data object Any : IonicEvent(), KBusCancellableTrait
    }
}
