package foo.starred.ionic.module.impl.render.trajectory.compute.base

import foo.starred.ionic.module.impl.render.trajectory.compute.data.TrajectoryComputeData
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

interface ITrajectoryCompute {
    fun match(stack: ItemStack): Boolean
    fun compute(player: Player, stack: ItemStack, range: Int): List<TrajectoryComputeData>
}
