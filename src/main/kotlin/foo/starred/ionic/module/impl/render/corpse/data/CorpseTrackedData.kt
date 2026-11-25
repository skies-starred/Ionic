package foo.starred.ionic.module.impl.render.corpse.data

import foo.starred.ionic.api.time.IonicTickable
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.text.parser.impl.parse
import net.minecraft.world.entity.Entity

data class CorpseTrackedData(
    val entity: Entity,
    val type: CorpseTypeData
) {
    val text = IonicTickable(2) {
        val player = client.player ?: return@IonicTickable null
        val distance = player.distanceTo(entity)

        "<${type.color()}>${type.display} <gray>[$distance]".parse()
    }
}
