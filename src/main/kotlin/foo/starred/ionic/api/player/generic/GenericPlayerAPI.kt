package foo.starred.ionic.api.player.generic

import foo.starred.kontext.abstraction.client.extension.boundKey
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.input.impl.GenericInputState
import net.minecraft.client.KeyMapping
import net.minecraft.world.InteractionHand

//? if >= 26.3
//import net.minecraft.world.item.component.SwingAnimation

object GenericPlayerAPI {
    fun attack() {
        val key = client.options.keyAttack.boundKey()

        GenericInputState.press(key, true)
        KeyMapping.click(key)
        GenericInputState.press(key, false)
    }

    fun use() {
        val key = client.options.keyUse.boundKey()

        GenericInputState.press(key, true)
        KeyMapping.click(key)
        GenericInputState.press(key, false)
    }

    fun swing(overwrite: Boolean = false) {
        val player = client.player ?: return
        //~ if >= 26.3 'player.swinging' -> 'player.isSwinging'
        if (!overwrite && player.swinging) return

        //? if >= 26.3 {
        /*player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false)
        *///? } else {
        player.swingingArm = InteractionHand.MAIN_HAND
        player.swingTime = -1
        player.swinging = true
        //? }
    }
}
