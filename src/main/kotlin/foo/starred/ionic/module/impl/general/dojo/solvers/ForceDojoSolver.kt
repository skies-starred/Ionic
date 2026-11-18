package foo.starred.ionic.module.impl.general.dojo.solvers

import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.event.core.on
import foo.starred.ionic.event.impl.PlayerEvent
import foo.starred.ionic.module.impl.general.dojo.impl.AutoDojo
import foo.starred.kbus.extensions.runWhen
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.monster.zombie.Zombie
import net.minecraft.world.item.Items

@LoadableIonicModule
object ForceDojoSolver {
    private val block by AutoDojo.forceGroup.switch("Block wrong clicks")

    init {
        on<PlayerEvent.Attack.Entity> {
            if (!block) return@on

            val entity = entity as? Zombie ?: return@on
            if (entity.getItemBySlot(EquipmentSlot.HEAD).item != Items.LEATHER_HELMET) return@on

            cancel()
        }.runWhen(AutoDojo.force)
    }
}
