package foo.starred.ionic.module.impl.render.trajectory.compute.impl

import foo.starred.ionic.module.impl.render.trajectory.compute.base.ITrajectoryCompute
import foo.starred.ionic.module.impl.render.trajectory.compute.data.TrajectoryComputeData
import foo.starred.ionic.module.impl.render.trajectory.impl.Trajectories
import foo.starred.kontext.api.entity.extension.renderVec3
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.item.EnderpearlItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.PI

object PearlTrajectoryCompute : ITrajectoryCompute {
    private val enabled by Trajectories.computeGroup.switch("Pearl trajectory", true).unique($$"pearl$enabled")

    override fun match(stack: ItemStack): Boolean {
        return enabled && stack.item is EnderpearlItem && stack.get<GenericKontextItemDataResolver.ID, String>() != "SPIRIT_LEAP"
    }

    override fun compute(player: Player, stack: ItemStack, range: Int): List<TrajectoryComputeData> {
        return listOf(simulate(player, range))
    }

    private fun simulate(player: Player, range: Int): TrajectoryComputeData {
        val level = player.level()
        val pitch = player.xRot
        val yaw = player.yRot

        val side = if (player.mainArm == HumanoidArm.RIGHT) 0.16 else -0.16
        val x1 = -Mth.cos(yaw * (PI.toFloat() / 180.0)) * side
        val z1 = -Mth.sin(yaw * (PI.toFloat() / 180.0)) * side
        var position = player.renderVec3().add(x1, player.eyeHeight - 0.15, z1)

        val x0 = -Mth.sin(yaw * (PI.toFloat() / 180.0)) * Mth.cos(pitch * (PI.toFloat() / 180.0))
        val y0 = -Mth.sin(pitch * (PI.toFloat() / 180.0))
        val z0 = Mth.cos(yaw * (PI.toFloat() / 180.0)) * Mth.cos(pitch * (PI.toFloat() / 180.0))

        var motion = Vec3(x0.toDouble(), y0.toDouble(), z0.toDouble()).normalize().scale(1.5)
        val points = mutableListOf<Vector3f>()
        points += position.toVector3f()

        var impact: AABB? = null
        var target: Entity? = null

        for (step in 0 until range) {
            val water = level.isWaterAt(BlockPos.containing(position))
            if (water) motion = motion.scale(0.8)

            val next = position.add(motion)
            val block = level.clip(ClipContext(position, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
            val endpoint = if (block.type != HitResult.Type.MISS) block.location else next

            val entity = ProjectileUtil.getEntityHitResult(level, player, position, endpoint, AABB(position, endpoint).inflate(1.0), { it.valid(player) }, 0.3f)
            if (entity != null) {
                points += entity.location.toVector3f()
                target = entity.entity

                break
            }

            if (block.type != HitResult.Type.MISS) {
                points += block.location.toVector3f()
                val normal = block.direction.step()
                val center = block.location.add(normal.x * 0.2, normal.y * 0.2, normal.z * 0.2)
                impact = AABB(center.x - 0.2, center.y - 0.2, center.z - 0.2, center.x + 0.2, center.y + 0.2, center.z + 0.2)

                break
            }

            position = next
            points += position.toVector3f()

            if (!water) motion = motion.scale(0.99)
            motion = motion.subtract(0.0, 0.03, 0.0)
        }

        return TrajectoryComputeData(points, impact, target)
    }

    private fun Entity.valid(player: Player): Boolean {
        if (this == player) return false
        if (this is AbstractArrow) return false
        if (isSpectator) return false
        if (isPassengerOfSameVehicle(player)) return false
        if (!canBeHitByProjectile()) return false

        return true
    }
}
