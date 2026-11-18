package foo.starred.ionic.api.player.movement

import foo.starred.kontext.abstraction.client.option.MinecraftOptions

object MovementPlayerAPI {
    fun forward(pressed: Boolean = true) {
        MinecraftOptions.instance.keyUp.isDown = pressed
    }

    fun backward(pressed: Boolean = true) {
        MinecraftOptions.instance.keyDown.isDown = pressed
    }

    fun left(pressed: Boolean = true) {
        MinecraftOptions.instance.keyLeft.isDown = pressed
    }

    fun right(pressed: Boolean = true) {
        MinecraftOptions.instance.keyRight.isDown = pressed
    }

    fun jump(pressed: Boolean = true) {
        MinecraftOptions.instance.keyJump.isDown = pressed
    }

    fun sprint(pressed: Boolean = true) {
        MinecraftOptions.instance.keySprint.isDown = pressed
    }

    fun sneak(pressed: Boolean = true) {
        MinecraftOptions.instance.keyShift.isDown = pressed
    }

    fun reset() {
        val options = MinecraftOptions.instance

        options.keyUp.isDown = false
        options.keyDown.isDown = false
        options.keyLeft.isDown = false
        options.keyRight.isDown = false
        options.keyJump.isDown = false
        options.keySprint.isDown = false
        options.keyShift.isDown = false
    }
}
