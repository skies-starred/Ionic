package foo.starred.ionic.event.dispatcher

import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.event.impl.*
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.world.InteractionResult

@LoadableIonicModule
object FabricEventDispatcher {
    init {
        ClientTickEvents.START_CLIENT_TICK.register { client ->
            TickEvent.Client.Start.post()
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            TickEvent.Client.End.post()
        }

        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            EntityEvent.Load(entity).post()
        }

        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ ->
            EntityEvent.Unload(entity).post()
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { component, bool ->
            if (bool) return@register true
            !MessageEvent.Chat.Intercept(component).post()
        }

        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            ScreenMouseEvents.allowMouseClick(screen).register { _, event ->
                !GuiEvent.Input.Mouse.Press(event).post()
            }

            ScreenMouseEvents.allowMouseRelease(screen).register { _, event ->
                !GuiEvent.Input.Mouse.Release(event).post()
            }

            ScreenMouseEvents.allowMouseScroll(screen).register { _, _, _, _, amount ->
                !GuiEvent.Input.Mouse.Scroll(amount).post()
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, event ->
                !GuiEvent.Input.Key.Press(event).post()
            }

            ScreenKeyboardEvents.allowKeyRelease(screen).register { _, event ->
                !GuiEvent.Input.Key.Release(event).post()
            }
        }

        UseItemCallback.EVENT.register { _, _, _ ->
            val a = PlayerEvent.Interact.None.post()
            val b = PlayerEvent.Interact.Any.post()

            if (!a && !b) return@register InteractionResult.PASS
            InteractionResult.FAIL
        }

        UseBlockCallback.EVENT.register { _, _, _, a ->
            val a = PlayerEvent.Interact.Block(a.blockPos).post()
            val b = PlayerEvent.Interact.Any.post()

            if (!a && !b) return@register InteractionResult.PASS
            InteractionResult.FAIL
        }

        UseEntityCallback.EVENT.register { _, _, _, entity, _ ->
            val a = PlayerEvent.Interact.Entity(entity).post()
            val b = PlayerEvent.Interact.Any.post()

            if (!a && !b) return@register InteractionResult.PASS
            InteractionResult.FAIL
        }
    }
}
