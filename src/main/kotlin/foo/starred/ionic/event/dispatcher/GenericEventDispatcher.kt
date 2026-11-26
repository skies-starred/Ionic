package foo.starred.ionic.event.dispatcher

import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.event.core.on
import foo.starred.ionic.event.impl.EntityEvent
import foo.starred.ionic.event.impl.MessageEvent
import foo.starred.ionic.event.impl.PacketEvent
import foo.starred.ionic.event.impl.ScoreboardEvent
import foo.starred.ionic.event.impl.WorldRenderEvent
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.client.extension.mainThread
import foo.starred.kontext.core.event.impl.KontextEvents
import foo.starred.parallax.api.events.ParallaxEvents
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.world.entity.LivingEntity

@LoadableIonicModule
object GenericEventDispatcher {
    init {
        on<PacketEvent.Receive, ClientboundSystemChatPacket> {
            if (overlay()) return@on

            mainThread {
                MessageEvent.Chat.Receive(content).post()
            }
        }

        on<PacketEvent.Process.Post, ClientboundSetEquipmentPacket> {
            val level = client.level ?: return@on
            val entity = level.getEntity(entity) as? LivingEntity ?: return@on

            EntityEvent.Update.Equipment(entity, slots).post()
        }

        KontextEvents.Scoreboard.UPDATE.register { event ->
            ScoreboardEvent.Update(event.lines0, event.lines1, event.components0, event.components1).post()
        }

        ParallaxEvents.EXTRACT.register {
            WorldRenderEvent.Extract.post()
        }
    }
}
