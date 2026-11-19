package foo.starred.ionic.module.impl.render.trajectory.impl

import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.config.theme.impl.catppuccin.MochaColorScheme
import foo.starred.ionic.event.impl.WorldRenderEvent
import foo.starred.ionic.module.base.Module
import foo.starred.ionic.module.impl.render.trajectory.compute.impl.BowTrajectoryCompute
import foo.starred.ionic.module.impl.render.trajectory.compute.impl.PearlTrajectoryCompute
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.entity.extension.renderAABB
import foo.starred.kontext.api.format.extension.color.alpha
import foo.starred.parallax.api.primitives.ParallaxBox
import foo.starred.parallax.api.primitives.ParallaxLine

@LoadableIonicModule
object Trajectories : Module(
    "Trajectories",
    "Renders trajectories of projectiles",
    ConfigCategory.RENDER
) {
    val computeGroup by config.group("Compute configuration", false)

    private val customisation by config.group("Visual customisation", false)
    private val lines by customisation.switch("Show lines", true)
    private val boxes by customisation.switch("Show boxes", true)
    private val entities by customisation.switch("Show entities", true)
    private val range by customisation.slider("Simulation range", 60, 20, 120, "ticks")
    private val width by customisation.slider("Line width", 2f, 1f, 5f)
    private val color by customisation.colorPicker("Color", MochaColorScheme.Sapphire.argb)
    private val depth by customisation.switch("Depth check", true)

    private val computers = listOf(BowTrajectoryCompute, PearlTrajectoryCompute)

    init {
        on<WorldRenderEvent.Extract> {
            val player = client.player ?: return@on
            val held = player.mainHandItem.takeIf { !it.isEmpty } ?: return@on
            val computer = computers.firstOrNull { it.match(held) } ?: return@on

            val trajectories = computer.compute(player, held, range)
            if (trajectories.isEmpty()) return@on

            val color0 = color
            val color1 = color.alpha(0.25f)
            for ((points, box, entity) in trajectories) {
                if (lines && points.size > 1) {
                    ParallaxLine.multiple(points, color0, width, depth)
                }

                if (boxes) {
                    box?.let {
                        ParallaxBox.frame(it, color0, width, depth)
                        ParallaxBox.fill(it, color1, depth)
                    }
                }

                if (entities) {
                    entity?.let {
                        val bounds = it.renderAABB()

                        ParallaxBox.frame(bounds, color0, width, depth)
                        ParallaxBox.fill(bounds, color1, depth)
                    }
                }
            }
        }
    }
}
