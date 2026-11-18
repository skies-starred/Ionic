@file:Suppress("ObjectPrivatePropertyName", "LocalVariableName")

package foo.starred.ionic.module.impl.general

import com.mojang.serialization.Codec
import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.command.command
import foo.starred.ionic.api.messaging.impl.MessagingAPI.displayAsMod
import foo.starred.ionic.api.player.generic.GenericPlayerAPI
import foo.starred.ionic.api.storage.JsonStore
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.InputEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.base.Module
import foo.starred.kontext.abstraction.chat.generic.MinecraftChat
import foo.starred.kontext.abstraction.chat.message.MinecraftMessages
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.abstraction.input.impl.GenericInputState
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import foo.starred.kontext.api.storage.impl.generic.extension.mutableSet
import foo.starred.kontext.api.text.parser.impl.parse
import net.minecraft.world.phys.BlockHitResult

@LoadableIonicModule
@IonicModuleScope(skyblock = true)
object AutoClicker : Module(
    "Auto clicker",
    "Automatically clicks for you!",
    ConfigCategory.GENERAL
) {
    private val left by config.group("Left clicker")
    private val `left$enabled` by left.switch("Enable left clicker")
    private val `left$key` by left.keybind("Left key")
    private val `left$cps` by left.slider("Left CPS", 3, 5, 20)

    private val right by config.group("Right clicker")
    private val `right$enabled` by right.switch("Enable right clicker")
    private val `right$key` by right.keybind("Right key")
    private val `right$cps` by right.slider("Right CPS", 3, 5, 20)

    private val customisation by config.group("Customisation")
    private val jitter by customisation.slider("CPS jitter", 2, 1, 3, "clicks")
    private val breaking by customisation.switch("Allow breaking blocks", true)
    private val breaker by customisation.switch("Block dungeon breaker", true)
    private val whitelist by customisation.switch("Whitelist mode")

    private val json = JsonStore("feature/general/autoClicker")
    private val set1 = json.mutableSet("left", Codec.STRING)
    private val set2 = json.mutableSet("right", Codec.STRING)

    private var delay0: Int = 0
    private var delay1: Int = 0

    init {
        on<TickEvent.Client.Start> {
            if (MinecraftGui.screen != null) return@on

            val player = client.player ?: return@on
            val level = client.level ?: return@on

            if (player.isUsingItem) return@on
            if (client.gameMode?.isDestroying ?: false) return@on

            val held = held() ?: return@on
            if (breaker && held == "DUNGEONBREAKER") return@on

            val _left = !whitelist || held in set1.value
            val _right = !whitelist || held in set2.value
            if (!_left && !_right) return@on

            val left = _left && `left$enabled` && GenericInputState.pressed(`left$key`)
            val right = _right && `right$enabled` && GenericInputState.pressed(`right$key`)

            val hit = client.hitResult as? BlockHitResult
            if (hit != null && !level.getBlockState(hit.blockPos).isAir && breaking) {
                GenericInputState.press(client.options.keyAttack, true)
                return@on
            }

            if (left) {
                delay0 += `left$cps`.delay()
                if (delay0 >= 20) {
                    GenericPlayerAPI.attack()
                    delay0 -= 20
                }
            }

            if (right) {
                delay1 += `right$cps`.delay()
                if (delay1 >= 20) {
                    GenericPlayerAPI.use()
                    delay1 -= 20
                }
            }
        }

        on<InputEvent.Keyboard.Release> {
            GenericInputState.press(client.options.keyAttack, false)
        }
    }

    private fun Int.delay(): Int {
        val value = jitter * 2
        return (this + (-value..value).random()).coerceIn(1, 20)
    }

    private fun held(): String? {
        val held = client.player?.mainHandItem ?: return null
        return held.get<GenericKontextItemDataResolver.UUID, String>() ?: held.get<GenericKontextItemDataResolver.UUID, String>() ?: held.hoverName.string
    }

    init {
        command {
            "ac" / "add" / "left" {
                val h = held() ?: return@invoke "Hold an item to whitelist.".displayAsMod()
                if (h in set1.value) return@invoke "$h is already in left whitelist!".displayAsMod()

                set1.update { add(h) }
                "Added <green>$h<r> to left whitelist!".displayAsMod()
            }

            "ac" / "add" / "right" {
                val h = held() ?: return@invoke "Hold an item to whitelist.".displayAsMod()
                if (h in set2.value) return@invoke "$h is already in right whitelist!".displayAsMod()

                set2.update { add(h) }
                "Added <green>$h<r> to right whitelist!".displayAsMod()
            }

            "ac" / "remove" / "left" {
                val h = held() ?: return@invoke "Hold an item to whitelist.".displayAsMod()
                if (h !in set1.value) return@invoke "$h is not in left whitelist!".displayAsMod()

                set1.update { remove(h) }
                "Removed <green>$h<r> from left whitelist!".displayAsMod()
            }

            "ac" / "remove" / "right" {
                val h = held() ?: return@invoke "Hold an item to whitelist.".displayAsMod()
                if (h !in set2.value) return@invoke "$h is not in right whitelist!".displayAsMod()

                set2.update { remove(h) }
                "Removed <green>$h<r> from right whitelist!".displayAsMod()
            }

            "ac" / "clear" / "left" {
                set1.update { clear() }
                "Cleared left whitelist.".displayAsMod()
            }

            "ac" / "clear" / "right" {
                set2.update { clear() }
                "Cleared right whitelist.".displayAsMod()
            }

            "ac" / "list" {
                val divider = ("<gray>" + MinecraftChat.repeat("-")).parse()

                "Autoclicker whitelist:".displayAsMod()
                MinecraftMessages.display(divider)

                MinecraftMessages.display("Left whitelist:")
                for (s in set1.value) MinecraftMessages.display(" <dark_gray>- <gray>$s".parse())
                MinecraftMessages.display(divider)

                MinecraftMessages.display("Right whitelist:")
                for (s in set2.value) MinecraftMessages.display(" <dark_gray>- <gray>$s".parse())
                MinecraftMessages.display(divider)

                if (!enabled) "Please turn on the feature \"AutoClicker\"".displayAsMod()
            }
        }
    }
}
