package foo.starred.ionic.module.impl.render

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.config.theme.impl.catppuccin.MochaColorScheme
import foo.starred.ionic.event.impl.LocationEvent
import foo.starred.ionic.event.impl.PacketEvent
import foo.starred.ionic.event.impl.TickEvent
import foo.starred.ionic.event.impl.WorldRenderEvent
import foo.starred.ionic.module.base.Module
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.entity.extension.renderVec3
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import foo.starred.parallax.api.primitives.ParallaxBox
import foo.starred.parallax.api.primitives.ParallaxLine
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import java.util.concurrent.ConcurrentHashMap

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.HUB])
object RatESP : Module(
    "Rat ESP",
    "Shows an ESP for rats in Hub.",
    ConfigCategory.RENDER
) {
    private const val RAT = "ewogICJ0aW1lc3RhbXAiIDogMTYxODQxOTcwMTc1MywKICAicHJvZmlsZUlkIiA6ICI3MzgyZGRmYmU0ODU0NTVjODI1ZjkwMGY4OGZkMzJmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJCdUlJZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThhYmI0NzFkYjBhYjc4NzAzMDExOTc5ZGM4YjQwNzk4YTk0MWYzYTRkZWMzZWM2MWNiZWVjMmFmOGNmZmU4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="

    private val tracer by config.switch("Tracer")
    private val width by config.slider("Width", 2f, 1f, 10f)
    private val color by config.colorPicker("Color", MochaColorScheme.Peach.argb)
    private val entities = ConcurrentHashMap.newKeySet<Entity>()

    init {
        on<TickEvent.Client.End> {
            if (ticks % 20 != 0) return@on

            entities.removeIf { !it.isAlive }
        }

        on<PacketEvent.Process.Post, ClientboundSetEntityDataPacket> {
            val entity = client.level?.getEntity(id) as? Display.ItemDisplay ?: return@on
            if (entities.any { it.uuid == entity.uuid }) return@on
            if (entity.itemStack.get<GenericKontextItemDataResolver.TEXTURE, String>() != RAT) return@on

            entities += entity
        }

        on<WorldRenderEvent.Extract> {
            for (entity in entities) {
                if (!entity.isAlive) continue
                val position = entity.renderVec3().add(-0.5, 1.0, -0.5)

                ParallaxBox.frame(AABB.unitCubeFromLowerCorner(position), color, width, false)

                if (tracer) {
                    ParallaxLine.tracer(position.toVector3f(), color, width, false)
                }
            }
        }

        on<LocationEvent.Server.Connect> {
            entities.clear()
        }
    }
}
