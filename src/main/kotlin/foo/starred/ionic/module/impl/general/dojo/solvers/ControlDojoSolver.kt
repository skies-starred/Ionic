package foo.starred.ionic.module.impl.general.dojo.solvers

import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.player.rotation.RotationPlayerAPI
import foo.starred.ionic.event.core.on
import foo.starred.ionic.event.impl.EntityEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.impl.general.dojo.impl.AutoDojo
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.client.extension.client
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3

@LoadableIonicModule
object ControlDojoSolver {
    private val autoAim by AutoDojo.controlGroup.switch("Auto aim")
    private val prediction by AutoDojo.controlGroup.slider("Predicted ticks", 5, 1, 10, "ticks")

    private val entities: MutableList<WitherSkeleton> = mutableListOf()
    private var last: Long = 0

    init {
        on<EntityEvent.Load> {
            if (!autoAim) return@on
            if (entity !is WitherSkeleton) return@on
            if (entity.getItemBySlot(EquipmentSlot.HEAD).item == Items.REDSTONE_BLOCK) return@on

            val player = client.player ?: return@on
            if (entity.distanceTo(player) > 25) return@on

            entities += entity
        }.runWhen(AutoDojo.control)

        on<EntityEvent.Death> {
            if (!autoAim) return@on
            if (entity !is WitherSkeleton) return@on

            entities -= entity
        }.runWhen(AutoDojo.control)

        on<TickEvent.Client.End> {
            if (ticks % 5 != 0) return@on

            entities.removeIf { !it.isAlive }
        }.runWhen(AutoDojo.control)

        on<TickEvent.Client.Start> {
            if (!autoAim) return@on
            val player = client.player ?: return@on
            val skeleton = entities.minByOrNull { it.distanceToSqr(player) } ?: return@on

            val position0 = skeleton.oldPosition()
            val position1 = skeleton.position()
            if (position0 == position1) return@on

            val position2 = Vec3(position1.x + (position1.x - position0.x) * prediction, position1.y + (position1.y - position0.y) + 1.5, position1.z + (position1.z - position0.z) * prediction)

            val now = System.currentTimeMillis()
            if (now - last < 40) return@on
            last = now

            RotationPlayerAPI.follow(position2, 0.4f)
        }.runWhen(AutoDojo.control)
    }
}
