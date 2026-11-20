package foo.starred.ionic.api.slayer

import foo.starred.ionic.api.slayer.data.SlayerInfo
import foo.starred.ionic.api.slayer.enums.tier.SlayerTier
import foo.starred.ionic.api.slayer.enums.type.base.ISlayerType
import foo.starred.ionic.api.slayer.enums.type.impl.SlayerBoss
import foo.starred.ionic.api.slayer.enums.type.impl.SlayerDemon
import foo.starred.ionic.api.slayer.enums.type.impl.SlayerMini
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.messaging.impl.MessagingAPI.displayAsDev
import foo.starred.ionic.api.skyblock.entity.duck.EntityDuck.Companion.parent
import foo.starred.ionic.event.core.on
import foo.starred.ionic.event.impl.EntityEvent
import foo.starred.ionic.event.impl.LocationEvent
import foo.starred.ionic.event.impl.MessageEvent
import foo.starred.ionic.event.impl.SlayerEvent
import foo.starred.ionic.event.impl.TickEvent
import net.minecraft.world.entity.Entity
import java.util.*

@LoadableIonicModule
object SlayerAPI {
    private val failRegex = Regex("\\s+SLAYER QUEST FAILED!")
    private val startRegex = Regex("\\s+SLAYER QUEST STARTED!")
    private val completeRegex = Regex("\\s+SLAYER QUEST COMPLETE!")

    val logged: MutableSet<Int> = mutableSetOf()
    val bosses: WeakHashMap<Entity, SlayerInfo> = WeakHashMap()

    var slayer: SlayerInfo? = null
        private set

    init {
        on<MessageEvent.Chat.Receive> {
            when {
                startRegex.matches(stripped) -> {
                    "SlayerAPI: Quest started!".displayAsDev()
                    SlayerEvent.Quest.Start.post()
                }

                completeRegex.matches(stripped) -> {
                    "SlayerAPI: Quest completed!".displayAsDev()
                    SlayerEvent.Quest.End.post()

                    slayer = null
                }

                failRegex.matches(stripped) -> {
                    "SlayerAPI: Quest failed!".displayAsDev()

                    SlayerEvent.Reset.QuestFail.post()
                    SlayerEvent.Reset.Any.post()

                    slayer = null
                }
            }
        }

        on<EntityEvent.Update.Attach> {
            val entity = entity.parent ?: return@on
            val slayerInfo =
                if (stripped.check()) bosses.computeIfAbsent(entity, ::SlayerInfo)
                else bosses[entity] ?: return@on

            if (stripped.contains("Conjoined Brood")) {
                slayerInfo.type = SlayerBoss.Tarantula
                slayerInfo.tier = SlayerTier.Five
                slayerInfo.phase = 2
            }

            if (slayerInfo.type is SlayerBoss && slayerInfo.owner == null) return@on
            if (!logged.add(entity.id)) return@on

            when (slayerInfo.type) {
                is SlayerBoss -> {
                    if (slayerInfo.owned) {
                        slayer = slayerInfo
                    }

                    SlayerEvent.Boss.Spawn(entity, slayerInfo).post()
                    "SlayerAPI: Slayer spawned (owner=${slayerInfo.owner}, tier=${slayerInfo.tier}, tickAge=${entity.tickCount / 20.0}s)".displayAsDev()
                }

                is SlayerMini -> {
                    SlayerEvent.Miniboss.Spawn(entity, slayerInfo).post()
                    "SlayerAPI: Miniboss spawned (tickAge=${entity.tickCount / 20.0}s)".displayAsDev()
                }

                is SlayerDemon -> {
                    SlayerEvent.Demon.Spawn(entity, slayerInfo).post()
                    "SlayerAPI: Demon spawned (tickAge=${entity.tickCount / 20.0}s)".displayAsDev()
                }
            }
        }

        on<EntityEvent.Death> {
            val slayerInfo = bosses.remove(entity) ?: return@on
            logged.remove(entity.id)

            when (slayerInfo.type) {
                is SlayerBoss -> {
                    if (slayerInfo.owned) {
                        slayer = null
                    }

                    SlayerEvent.Boss.Death(entity, slayerInfo).post()
                    "SlayerAPI: Slayer killed (owner=${slayerInfo.owner}, phase=${slayerInfo.phase}, tier=${slayerInfo.tier}, tickAge=${entity.tickCount / 20.0}s)".displayAsDev()
                }

                is SlayerMini -> {
                    SlayerEvent.Miniboss.Death(entity, slayerInfo).post()
                    "SlayerAPI: Miniboss killed (tickAge=${entity.tickCount / 20.0}s)".displayAsDev()
                }

                is SlayerDemon -> {
                    SlayerEvent.Demon.Death(entity, slayerInfo).post()
                    "SlayerAPI: Demon killed (tickAge=${entity.tickCount / 20.0}s)".displayAsDev()
                }
            }
        }

        on<TickEvent.Client.End> {
            if (ticks % 5 != 0) return@on

            bosses.entries.removeIf { (key, value) ->
                (!key.isAlive).also { if (it) logged.remove(key.id) }
            }
        }

        on<LocationEvent.Server.Connect> {
            "SlayerAPI: Cleaning up.".displayAsDev()

            SlayerEvent.Reset.ServerChange.post()
            SlayerEvent.Reset.Any.post()

            reset()
        }
    }

    private fun reset() {
        bosses.clear()
        logged.clear()

        slayer = null
    }

    private fun String.check(): Boolean {
        if (!endsWith("❤") && !endsWith("❤ \uE01A") && !endsWith(" Hits")) return false

        for (name in ISlayerType.Companion.Names.all) {
            if (!contains(name)) continue
            return true
        }

        return false
    }
}
