package foo.starred.ionic.event.impl

import foo.starred.ionic.event.core.IonicEvent
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

data class BlockUpdateEvent(
    val old: BlockState,
    val new: BlockState,
    val pos: BlockPos
) : IonicEvent()
