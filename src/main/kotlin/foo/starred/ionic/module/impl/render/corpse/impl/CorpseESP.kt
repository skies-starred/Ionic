@file:Suppress("ObjectPropertyName", "Unused")

package foo.starred.ionic.module.impl.render.corpse.impl

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.config.theme.impl.catppuccin.MochaColorScheme
import foo.starred.ionic.event.impl.EntityEvent
import foo.starred.ionic.event.impl.LocationEvent
import foo.starred.ionic.event.impl.PlayerEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.event.impl.WorldRenderEvent
import foo.starred.ionic.module.base.Module
import foo.starred.ionic.module.impl.render.corpse.data.CorpseTrackedData
import foo.starred.ionic.module.impl.render.corpse.data.CorpseTypeData
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import foo.starred.parallax.api.primitives.ParallaxBox
import foo.starred.parallax.api.primitives.ParallaxLine
import foo.starred.parallax.api.primitives.ParallaxText
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.Vec3
import java.util.concurrent.ConcurrentHashMap

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.MINESHAFT])
object CorpseESP : Module(
    "Corpse ESP",
    "Highlights unlooted corpses",
    ConfigCategory.RENDER
) {
    private val depth by config.switch("Depth check")
    private val tracer by config.switch("Show tracer")
    private val name by config.switch("Show name")
    private val width by config.slider("Highlight width", 2f, 1f, 10f)

    private val types by config.group("Filter types")
    val `types$lapis` by types.switch("Highlight lapis", true)
    val `types$tungsten` by types.switch("Highlight tungsten", true)
    val `types$umber` by types.switch("Highlight umber", true)
    val `types$vanguard` by types.switch("Highlight vanguard", true)

    private val colors by config.group("Colors")
    val `colors$lapis` by colors.colorPicker("Lapis color", MochaColorScheme.Blue.argb)
    val `colors$tungsten` by colors.colorPicker("Tungsten color", MochaColorScheme.Text.argb)
    val `colors$umber` by colors.colorPicker("Umber color", MochaColorScheme.Maroon.argb)
    val `colors$vanguard` by colors.colorPicker("Vanguard color", MochaColorScheme.Rosewater.argb)

    private val regex = Regex("^\\s+(\\w+) CORPSE LOOT!")

    private val tracked = ConcurrentHashMap.newKeySet<CorpseTrackedData>()
    private val claimed = mutableListOf<Vec3>()

    init {
        on<TickEvent.Client.End> {
            if (ticks % 10 != 0) return@on

            tracked.removeIf { !it.entity.isAlive }
        }

        on<EntityEvent.Update.Equipment> {
            if (!entity.check()) return@on
            if (slots.none { it.first == EquipmentSlot.HEAD }) return@on

            val type = CorpseTypeData.get(entity) ?: return@on
            val position = entity.position()

            if (claimed.any { it.distanceToSqr(position) < 4 }) return@on
            if (tracked.any { it.entity.uuid == entity.uuid }) return@on

            tracked += CorpseTrackedData(entity, type)
        }

        on<PlayerEvent.Interact.Entity> {
            val player = client.player ?: return@on
            if (!entity.check()) return@on

            val type = CorpseTypeData.get(entity) ?: return@on
            val id = player.mainHandItem.get<GenericKontextItemDataResolver.ID, String>() ?: return@on

            if (type.key != null && type.key != id) return@on
            if (!tracked.removeIf { it.entity == entity }) return@on

            claimed += entity.position()
        }

        on<WorldRenderEvent.Extract> {
            if (tracked.isEmpty()) return@on

            for (data in tracked) {
                val entity = data.entity
                val type = data.type

                if (!entity.isAlive) continue
                if (!type.enabled()) continue
                val color = type.color()

                ParallaxBox.frame(entity.boundingBox, color, width, depth)

                if (tracer) {
                    ParallaxLine.tracer(entity.boundingBox.center.toVector3f(), color, width, depth)
                }

                if (name) {
                    ParallaxText.component(data.text.value ?: continue, entity.boundingBox.center, depth = depth, increase = true)
                }
            }
        }

        on<LocationEvent.Server.Connect> {
            claimed.clear()
            tracked.clear()
        }
    }

    private fun Entity.check(): Boolean {
        if (this !is ArmorStand) return false
        if (customName != null) return false
        if (isInvisible) return false

        return true
    }
}
