package foo.starred.ionic.module.impl.general.dojo.solvers

import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.player.generic.GenericPlayerAPI
import foo.starred.ionic.event.core.on
import foo.starred.ionic.event.impl.PlayerEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.module.impl.general.dojo.impl.AutoDojo
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.inventory.MinecraftInventory
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.monster.zombie.Zombie
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.phys.EntityHitResult
import kotlin.random.Random

@LoadableIonicModule
object DisciplineDojoSolver {
    private val triggerbot by AutoDojo.disciplineGroup.switch("Trigger bot").description("Automatically hits mobs that you hover over")

    private val swap by AutoDojo.disciplineGroup.switch("Auto swap").description("Disabled when triggerbot is enabled")
    private val block by AutoDojo.disciplineGroup.switch("Block wrong clicks").description("Disabled when triggerbot is enabled")

    private val map = mapOf(Items.LEATHER_HELMET to Items.WOODEN_SWORD, Items.IRON_HELMET to Items.IRON_SWORD, Items.GOLDEN_HELMET to Items.GOLDEN_SWORD, Items.DIAMOND_HELMET to Items.DIAMOND_SWORD)
    private var next = 0L

    init {
        on<TickEvent.Client.Start> {
            if (!triggerbot) return@on

            val player = client.player ?: return@on
            val entity = (client.hitResult as? EntityHitResult)?.entity as? Zombie ?: return@on
            val sword = map[entity.getItemBySlot(EquipmentSlot.HEAD).item] ?: return@on

            if (sword != player.mainHandItem.item) {
                swap(sword)
            }

            val now = System.currentTimeMillis()
            if (now < next) return@on
            next = now + Random.nextLong(80, 141)

            GenericPlayerAPI.attack()
        }.runWhen(AutoDojo.discipline)

        on<PlayerEvent.Attack.Entity> {
            if (triggerbot) return@on
            if (!block && !swap) return@on
            val entity = entity as? Zombie ?: return@on

            val player = client.player ?: return@on
            val sword = map[entity.getItemBySlot(EquipmentSlot.HEAD).item]?.takeIf { it != player.mainHandItem.item } ?: return@on

            if (block) {
                cancel()
            }

            if (swap) {
                swap(sword)
            }
        }.runWhen(AutoDojo.discipline)
    }

    private fun swap(sword: Item) {
        MinecraftInventory.selected = MinecraftInventory.hotbar?.indexOfFirst { it.item == sword }?.takeIf { it != -1 } ?: return
    }
}
