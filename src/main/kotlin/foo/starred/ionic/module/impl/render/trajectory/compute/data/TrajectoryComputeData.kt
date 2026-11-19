package foo.starred.ionic.module.impl.render.trajectory.compute.data

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import org.joml.Vector3f

data class TrajectoryComputeData(
    val points: List<Vector3f>,
    val box: AABB? = null,
    val entity: Entity? = null
)
