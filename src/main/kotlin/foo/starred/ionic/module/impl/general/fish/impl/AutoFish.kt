@file:Suppress("ObjectPrivatePropertyName")

package foo.starred.ionic.module.impl.general.fish.impl

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.messaging.impl.MessagingAPI.displayAsMod
import foo.starred.ionic.api.player.generic.GenericPlayerAPI
import foo.starred.ionic.api.player.movement.MovementPlayerAPI
import foo.starred.ionic.api.player.rotation.RotationPlayerAPI
import foo.starred.ionic.api.time.IonicScheduler
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.EntityEvent
import foo.starred.ionic.event.impl.MessageEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.base.Module
import foo.starred.ionic.module.impl.general.fish.action.AutoFishSwapAction
import foo.starred.ionic.module.impl.general.fish.data.AutoFishSeaCreatureData
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.inventory.MinecraftInventory
import foo.starred.kontext.api.format.extension.string.stripped
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.scheduling.scheduler.extensions.start
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.Items
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import kotlin.random.Random

@LoadableIonicModule
@IonicModuleScope(skyblock = true)
object AutoFish : Module(
    "Auto fish",
    "Helper features for fishing.",
    ConfigCategory.GENERAL
) {
    private val pull by config.group("Auto pull", false)
    private val `pull$enabled` by pull.switch("Enable auto pull", true)
    private val `pull$delay` by pull.slider("Delay", 1, 0, 5, "ticks")
    private val `pull$variance` by pull.slider("Delay variance", 0, 0, 3, "ticks")

    private val recast by config.group("Auto recast", false)
    private val `recast$enabled` by recast.switch("Enable auto recast").description("Checks if the fishing rod is already being used, and uses it if not")
    private val `recast$check` by recast.switch("Recast check")
    private val `recast$delay` by recast.slider("Recast delay", 1, 0, 10, "ticks")
    private val `recast$variance` by recast.slider("Delay variance", 0, 0, 5, "ticks")

    private val ability by config.group("Auto ability", false)
    private val `ability$enabled` = ability.switch("Enable auto ability").description("Automatically uses items like Hyperion").unique($$"hyperion$enabled")
    private val `ability$type` by ability.selector("Selected ability", AutoFishSwapAction.entries.map { it.display })
    private val `ability$clicks` by ability.slider("Number of clicks", 2, 1, 5)
    private val `ability$spread` by ability.slider("Look back variation", 2, 0, 10, "degrees")
    private val `ability$recast` by ability.slider("Recast delay", 4, 0, 20, "ticks")

    private val fb by config.group("Auto FB", false)
    private val `fb$enabled` = fb.switch("Enable auto FB").description("Moves the player if you're wearing FB").unique($$"fb$enabled")
    private val `fb$move` by fb.slider("Move every", 20, 5, 40, "seconds")

    private val blacklist = setOf("SOUL_WHIP", "FLAMING_FLAY")

    private var bobber: FishingHook? = null
    private var `fb$last` = 0

    init {
        on<EntityEvent.Load> {
            val entity = entity as? FishingHook ?: return@on
            val player = client.player ?: return@on

            if (entity.owner != player) return@on
            if (!check()) return@on

            bobber = entity
        }

        on<EntityEvent.Update.Named> {
            if (!`pull$enabled`) return@on

            val hook = bobber ?: return@on
            if (component.stripped() != "!!!") return@on
            if (entity.distanceTo(hook) > 2f) return@on

            val delay = (`pull$delay` + (0..`pull$variance`).random()).coerceAtLeast(0)
            IonicScheduler.schedule(delay.clientTicks.start) {
                GenericPlayerAPI.use()
                recast()
            }
        }

        on<EntityEvent.Unload> {
            val entity = entity as? FishingHook ?: return@on
            if (entity != bobber) return@on

            bobber = null
        }

        on<MessageEvent.Chat.Receive> {
            if (AutoFishSeaCreatureData.find(stripped) == null) return@on

            val selected = AutoFishSwapAction.entries.getOrNull(`ability$type`) ?: return@on
            val slot = selected.tryFind() ?: return@on

            selected.action(slot) { yaw, pitch, last ->
                use(`ability$clicks`, selected.rotate, last, yaw, pitch)
            }
        }.runWhen(`ability$enabled`.state)

        on<TickEvent.Client.Start> {
            if (++`fb$last` < `fb$move` * 20) return@on
            `fb$last` = 0

            val player = client.player ?: return@on
            if (player.getItemBySlot(EquipmentSlot.HEAD).get<GenericKontextItemDataResolver.ID, String>() != "FROZEN_BLAZE_HELMET") return@on

            val boolean = Random.nextBoolean()
            val first = if (boolean) MovementPlayerAPI::left else MovementPlayerAPI::right
            val second = if (boolean) MovementPlayerAPI::right else MovementPlayerAPI::left
            IonicScheduler.schedule((1..3).random().clientTicks.start) {
                first(true)

                val random = (1..3).random().clientTicks.start
                IonicScheduler.schedule(random) {
                    first(false)
                    second(true)

                    IonicScheduler.schedule(random) {
                        second(false)
                    }
                }
            }
        }.runWhen(`fb$enabled`.state)

        IonicScheduler.repeat((15 * 20).clientTicks.start) {
            if (!enabled) return@repeat
            if (!`recast$enabled`) return@repeat
            if (!`recast$check`) return@repeat
            if (bobber?.isAlive == true) return@repeat
            if (!check()) return@repeat

            GenericPlayerAPI.use()
        }
    }

    private fun check(): Boolean {
        val player = client.player ?: return false

        val held = player.mainHandItem.takeIf { !it.isEmpty } ?: return false
        if (held.item != Items.FISHING_ROD) return false

        val id = held.get<GenericKontextItemDataResolver.ID, String>() ?: return false
        if (id in blacklist) return false

        return true
    }

    private fun use(remaining: Int, rotate: Boolean, slot: Int, yaw: Float, pitch: Float) {
        GenericPlayerAPI.use()

        if (remaining > 1) {
            IonicScheduler.schedule((2..5).random().clientTicks.start) {
                use(remaining - 1, rotate, slot, yaw, pitch)
            }

            return
        }

        IonicScheduler.schedule((2..5).random().clientTicks.start) {
            MinecraftInventory.selected = slot

            if (rotate) {
                RotationPlayerAPI.smooth(yaw + (Random.nextFloat() * 2f - 1f) * `ability$spread`, pitch + (Random.nextFloat() * 2f - 1f) * `ability$spread`, 150)
            }

            IonicScheduler.schedule(`ability$recast`.clientTicks.start) {
                recast()
            }
        }
    }

    private fun recast() {
        if (!`recast$enabled`) return

        val recast = 2 + `recast$delay` + (0..`recast$variance`).random()
        IonicScheduler.schedule(recast.clientTicks.start) {
            if (check()) GenericPlayerAPI.use()
        }
    }
}
