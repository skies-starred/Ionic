package foo.starred.ionic.module.impl.general.experiment.solvers.impl

import foo.starred.ionic.module.impl.general.experiment.impl.AutoExperiment
import foo.starred.ionic.module.impl.general.experiment.solvers.base.IExperimentSolver
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object ChronomatronExperimentSolver : IExperimentSolver {
    private val order = mutableListOf<Int>()
    private var last = -1
    private var ready = false
    private var clicks = 0
    private var bool = false

    override val next: Int?
        get() = if (ready && clicks < order.size) order[clicks++] else null

    override val close: Boolean
        get() = AutoExperiment.close && bool && clicks >= order.size

    override fun fn(screen: AbstractContainerScreen<*>) {
        val slots = screen.menu.slots
        val center = slots[49].item

        if (last != -1 && center.item == Items.GLOWSTONE && !slots[last].item.glint()) {
            val number = if (AutoExperiment.max) 15 else 11
            bool = order.size > number - AutoExperiment.serums
            ready = false

            return
        }

        if (ready) {
            return
        }

        if (center.item != Items.CLOCK) {
            return
        }

        val slot = slots.firstOrNull { it.index in 10..43 && it.item.glint() } ?: return

        order.add(slot.index)
        last = slot.index
        ready = true
        clicks = 0
    }

    override fun reset() {
        order.clear()
        last = -1
        ready = false
        clicks = 0
        bool = false
    }

    private fun ItemStack.glint(): Boolean {
        return components.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE) != null
    }
}
