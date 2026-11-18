package foo.starred.ionic.module.impl.general.dojo.solvers

import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.player.rotation.RotationPlayerAPI
import foo.starred.ionic.event.core.on
import foo.starred.ionic.event.impl.BlockUpdateEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.impl.general.dojo.impl.AutoDojo
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.input.impl.GenericInputState
import foo.starred.kontext.abstraction.inventory.MinecraftInventory
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.level.block.Blocks
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@LoadableIonicModule
object MasteryDojoSolver {
    private val offset by AutoDojo.masteryGroup.slider("Shoot offset", 500, 0, 1000, "ms")

    private val blocks = mutableMapOf<BlockPos, Long>()

    private var target: BlockPos? = null
    private var aimed: BlockPos? = null

    init {
        on<BlockUpdateEvent> {
            when {
                //~ if >= 26.2 'Blocks.RED_WOOL' -> 'Blocks.WOOL.red()'
                old.block == Blocks.RED_WOOL -> {
                    blocks -= pos
                }

                //~ if >= 26.2 'Blocks.LIME_WOOL' -> 'Blocks.WOOL.lime()'
                new.block == Blocks.LIME_WOOL -> {
                    val player = client.player ?: return@on
                    if (player.distanceToSqr(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) > 400) return@on

                    blocks[pos] = System.currentTimeMillis() + 6500
                }
            }
        }.runWhen(AutoDojo.mastery)

        on<TickEvent.Client.Start> {
            val player = client.player ?: return@on
            val now = System.currentTimeMillis()
            blocks.entries.removeIf { it.value < now }

            if (MinecraftInventory.selected != 0) {
                MinecraftInventory.selected = 0
                return@on
            }

            if (target !in blocks) {
                target = pick(player, now)
                aimed = null
            }

            val current = target ?: return@on
            if (aimed != current) {
                val route = plan(player, current)
                RotationPlayerAPI.smooth(route[0].toFloat(), route[1].toFloat(), 150)
                aimed = current
            }

            if (!player.isUsingItem) {
                GenericInputState.press(client.options.keyUse, true)
                return@on
            }

            if (player.ticksUsingItem < 20) return@on
            if ((blocks[current] ?: return@on) - now - plan(player, current)[2] * 50 > offset) return@on

            GenericInputState.press(client.options.keyUse, false)
            blocks -= current
            target = null
            aimed = null
        }.runWhen(AutoDojo.mastery)
    }

    private fun pick(player: LocalPlayer, now: Long): BlockPos? {
        return blocks.entries
            .map { Triple(it.key, it.value - now, plan(player, it.key)) }
            .filter { it.second > it.third[2] * 50 }
            .minByOrNull { it.second + abs(Mth.wrapDegrees(it.third[0].toFloat() - player.yRot)) * 6 }
            ?.first
    }

    private fun plan(player: LocalPlayer, position: BlockPos): DoubleArray {
        val eye = player.eyePosition
        val x1 = position.x + 0.5 - eye.x
        val z1 = position.z + 0.5 - eye.z
        val angle = solve(hypot(x1, z1), position.y + 0.75 - eye.y)

        return doubleArrayOf(Math.toDegrees(-atan2(x1, z1)), -angle[0], angle[1])
    }

    private fun solve(distance: Double, height: Double): DoubleArray {
        var low = -30.0
        var high = 60.0

        repeat(12) {
            val middle = (low + high) / 2
            if (arc(middle, distance)[0] < height) low = middle else high = middle
        }

        return doubleArrayOf(high, arc(high, distance)[1])
    }

    private fun arc(angle: Double, distance: Double): DoubleArray {
        val radians = Math.toRadians(angle)
        var x0 = 0.0
        var y0 = 0.0
        var x1 = cos(radians) * 3.0
        var y1 = sin(radians) * 3.0
        var ticks = 0

        while (x0 < distance && ticks < 200) {
            x0 += x1
            y0 += y1
            x1 *= 0.99
            y1 = y1 * 0.99 - 0.05
            ticks++
        }

        return doubleArrayOf(y0, ticks.toDouble())
    }
}
