package foo.starred.ionic.module.impl.general.fish.action

import foo.starred.ionic.api.player.rotation.RotationPlayerAPI
import foo.starred.ionic.api.time.IonicScheduler
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.inventory.MinecraftInventory
import foo.starred.kontext.api.format.impl.string.StringFormatter
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.scheduling.scheduler.extensions.start

enum class AutoFishSwapAction(val rotate: Boolean) {
    HYPERION(true),
    FIRE_VEIL(false),
    SOUL_WHIP(false),
    FLAMING_FLAY(false);

    val display: String = StringFormatter.titleCased(name)

    fun tryFind(): Int? {
        return MinecraftInventory.hotbar?.indexOfFirst { it.get<GenericKontextItemDataResolver.ID, String>() == name }?.takeIf { it != -1 }
    }

    fun action(slot: Int, block: (yaw: Float, pitch: Float, slot: Int) -> Unit) {
        action(this, slot, block)
    }

    companion object {
        fun action(action: AutoFishSwapAction, slot: Int, block: (yaw: Float, pitch: Float, slot: Int) -> Unit) {
            val player = client.player ?: return

            val yaw = player.yRot
            val pitch = player.xRot

            val last = MinecraftInventory.selected
            MinecraftInventory.selected = slot

            if (!action.rotate) {
                IonicScheduler.schedule((2..4).random().clientTicks.start) {
                    block.invoke(yaw, pitch, last)
                }

                return
            }

            RotationPlayerAPI.smooth(yaw, 90f, 150).invokeOnCompletion {
                if (it != null) return@invokeOnCompletion

                IonicScheduler.schedule((2..4).random().clientTicks.start) {
                    block.invoke(yaw, pitch, last)
                }
            }
        }
    }
}
