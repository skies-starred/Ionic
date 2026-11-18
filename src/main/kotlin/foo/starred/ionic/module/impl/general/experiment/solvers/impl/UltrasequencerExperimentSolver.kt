package foo.starred.ionic.module.impl.general.experiment.solvers.impl

import foo.starred.ionic.module.impl.general.experiment.impl.AutoExperiment
import foo.starred.ionic.module.impl.general.experiment.solvers.base.IExperimentSolver
import foo.starred.kontext.api.format.extension.string.stripped
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.Items

object UltrasequencerExperimentSolver : IExperimentSolver {
    private val regex = Regex("\\d+")
    private val order = HashMap<Int, Int>()
    private var ready = false
    private var clicks = 0

    override val next: Int?
        get() = if (!ready) order[clicks++] else null

    override val close: Boolean
        get() = AutoExperiment.close && order.size > if (AutoExperiment.max) 20 else 9 - AutoExperiment.serums

    override fun fn(screen: AbstractContainerScreen<*>) {
        val slots = screen.menu.slots
        val center = slots[49].item

        if (center.item == Items.CLOCK) return ::ready.set(false)
        if (center.item != Items.GLOWSTONE) return
        if (ready) return

        order.clear()
        for (slot in slots) {
            if (slot.index !in 9..44) continue
            if (!slot.item.hoverName.stripped().matches(regex)) continue

            order[slot.item.count - 1] = slot.index
        }

        ready = true
        clicks = 0
    }

    override fun reset() {
        order.clear()
        ready = false
        clicks = 0
    }
}
