package foo.starred.ionic.module.impl.render.corpse.data

import foo.starred.ionic.module.impl.render.corpse.impl.CorpseESP
import foo.starred.kontext.api.format.impl.string.StringFormatter
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.GenericKontextItemDataResolver
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand

enum class CorpseTypeData(val id: String, val color: () -> Int, val enabled: () -> Boolean) {
    LAPIS("LAPIS_ARMOR_HELMET", { CorpseESP.`colors$lapis` }, { CorpseESP.`types$lapis` }),
    TUNGSTEN("MINERAL_HELMET", { CorpseESP.`colors$tungsten` }, { CorpseESP.`types$tungsten` }),
    UMBER("YOG_HELMET", { CorpseESP.`colors$umber` }, { CorpseESP.`types$umber` }),
    VANGUARD("VANGUARD_HELMET", { CorpseESP.`colors$vanguard` }, { CorpseESP.`types$vanguard` }),;

    val display: String = StringFormatter.titleCased(name)

    companion object {
        fun get(entity: Entity): CorpseTypeData? {
            val stand = entity as? ArmorStand ?: return null
            if (!stand.isAlive) return null

            val item = stand.getItemBySlot(EquipmentSlot.HEAD)
            if (item.isEmpty) return null

            val id = item.get<GenericKontextItemDataResolver.ID, String>()
            return entries.find { it.id == id }
        }
    }
}
