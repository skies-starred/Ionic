/**
 * A rewritten and "improved" version of NoammAddon's PlayerUtils rotation code. The NoammAddons code was used as a reference for some parts.
 * NoammAddons is licensed under Creative Commons Zero v1.0 Universal (https://github.com/Noamm9/NoammAddons/blob/multiversion/LICENSE)
 * Modifications to this file are licensed under BSD-3 Clause License (https://data.starred.foo/LICENSE.txt)
 */

@file:Suppress("LocalVariableName")

package foo.starred.ionic.api.player.rotation

import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.entity.extension.renderVec3
import kotlinx.coroutines.CompletableDeferred
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object RotationPlayerAPI {
    private var pending: CompletableDeferred<Unit>? = null
    private var start = 0L
    private var total = 0L

    private var yaw0 = 0f
    private var pitch0 = 0f

    private var yaw1 = 0f
    private var pitch1 = 0f

    fun frame() {
        val done = pending ?: return
        val progress = if (total <= 0) 1.0 else min((System.currentTimeMillis() - start).toDouble() / total, 1.0)
        val eased = ease(progress).toFloat()

        turn(yaw0 + yaw1 * eased, pitch0 + (pitch1 - pitch0) * eased)

        if (progress < 1.0) return
        pending = null
        done.complete(Unit)
    }

    fun snap(yaw: Float, pitch: Float) {
        pending?.cancel()
        pending = null
        turn(yaw, pitch)
    }

    fun snap(target: Vec3) {
        val delta = delta(target) ?: return
        snap(yaw(delta), pitch(delta))
    }

    fun follow(target: Vec3, factor: Float) {
        val delta = delta(target) ?: return
        follow(yaw(delta), pitch(delta), factor)
    }

    fun follow(yaw: Float, pitch: Float, factor: Float) {
        val player = client.player ?: return

        pending?.cancel()
        pending = null

        turn(player.yRot + Mth.wrapDegrees(yaw - player.yRot) * factor, player.xRot + (pitch - player.xRot) * factor)
    }

    fun smooth(yaw: Float, pitch: Float, time: Long): CompletableDeferred<Unit> {
        val player = client.player ?: return CompletableDeferred(Unit)
        val _yaw0 = Mth.wrapDegrees(player.yRot)
        val _pitch0 = player.xRot.coerceIn(-90f, 90f)

        if (abs(_yaw0 - Mth.wrapDegrees(yaw)) <= 1f && abs(_pitch0 - pitch.coerceIn(-90f, 90f)) <= 1f) {
            return CompletableDeferred(Unit)
        }

        pending?.cancel()
        pending = CompletableDeferred()

        yaw0 = _yaw0
        pitch0 = _pitch0

        yaw1 = Mth.wrapDegrees(yaw - _yaw0)
        pitch1 = pitch.coerceIn(-90f, 90f)

        start = System.currentTimeMillis()
        total = time

        return pending!!
    }

    fun smooth(target: Vec3, time: Long): CompletableDeferred<Unit> {
        val delta = delta(target) ?: return CompletableDeferred(Unit)
        return smooth(yaw(delta), pitch(delta), time)
    }

    private fun turn(yaw: Float, pitch: Float) {
        val player = client.player ?: return
        val sensitivity = client.options.sensitivity().get().toFloat() * 0.6f + 0.2f
        val step = sensitivity * sensitivity * sensitivity * 1.2f

        val horizontal = Mth.wrapDegrees(yaw - player.yRot)
        val vertical = pitch.coerceIn(-90f, 90f) - player.xRot

        val rotated = player.yRot + horizontal - horizontal % step
        val tilted = (player.xRot + vertical - vertical % step).coerceIn(-90f, 90f)

        player.yRot = rotated
        player.xRot = tilted
        player.yHeadRot = rotated
        player.yBodyRot = rotated
        player.forceSetRotation(rotated, false, tilted, false)
    }

    private fun delta(target: Vec3): Vec3? {
        val player = client.player ?: return null
        val origin = player.renderVec3()

        return target.subtract(origin.x, origin.y + player.eyeHeight, origin.z)
    }

    private fun yaw(delta: Vec3): Float {
        return (-atan2(delta.x, delta.z) * (180.0 / PI)).toFloat()
    }

    private fun pitch(delta: Vec3): Float {
        return (-atan2(delta.y, sqrt(delta.x * delta.x + delta.z * delta.z)) * (180.0 / PI)).toFloat()
    }

    private fun ease(progress: Double): Double {
        if (progress < 0.5) return 4.0 * progress.pow(3)
        return 1.0 - (-2.0 * progress + 2.0).pow(3) / 2.0
    }
}
