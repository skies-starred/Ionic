package foo.starred.ionic.module.impl.general

import com.mojang.blaze3d.platform.InputConstants
import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.player.generic.GenericPlayerAPI
import foo.starred.ionic.api.time.IonicScheduler
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.InputEvent
import foo.starred.ionic.module.base.Module
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.abstraction.input.impl.GenericInputState
import foo.starred.kontext.api.item.extension.customData
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.scheduling.scheduler.extensions.start
import net.minecraft.world.item.ItemStack

@LoadableIonicModule
@IonicModuleScope(skyblock = true)
object LeftClickEtherwarp : Module(
    "Left click etherwarp",
    "QoL additions for etherwarp",
    ConfigCategory.GENERAL
) {
    private val shift by config.switch("Shift automatically")
    private val array = intArrayOf(2, 3, 4)

    init {
        on<InputEvent.Mouse.Press> {
            if (MinecraftGui.screen != null) return@on
            if (buttonInfo.button() != InputConstants.MOUSE_BUTTON_LEFT) return@on

            val player = client.player ?: return@on
            if (!player.mainHandItem.etherwarp()) return@on

            val crouching = player.isCrouching
            if (!crouching && !shift) return@on

            if (crouching) {
                cancel()
                action()
                return@on
            }

            cancel()
            GenericInputState.press(client.options.keyShift, true)
            IonicScheduler.schedule(array.random().clientTicks.start) {
                action()

                IonicScheduler.schedule(1.clientTicks.start) {
                    GenericInputState.press(client.options.keyShift, false)
                }
            }
        }
    }

    private fun action() {
        GenericPlayerAPI.use()
        GenericPlayerAPI.swing()
    }

    private fun ItemStack.etherwarp(): Boolean {
        return customData()?.getBoolean("ethermerge")?.orElse(false) == true || get<GenericKontextItemDataResolver.UUID, String>() == "ETHERWARP_CONDUIT"
    }
}
