@file:Suppress("FunctionName", "Unused")

package foo.starred.ionic.api.skyblock.entity.duck

import foo.starred.kontext.api.entity.extension.get
import foo.starred.kontext.api.entity.resolver.impl.GenericKontextEntityDataResolver
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import java.lang.ref.WeakReference

interface EntityDuck {
    fun `ionic$attachments`(): MutableList<WeakReference<Entity>>
    fun `ionic$attach`(): Entity?
    fun `ionic$attach`(entity: Entity)

    companion object {
        val Entity.parent: Entity?
            get() = (this as? EntityDuck)?.`ionic$attach`()

        val Entity.attached: List<Entity>
            get() = (this as? EntityDuck)?.`ionic$attachments`()?.mapNotNull { it.get() } ?: emptyList()

        val Entity.attachedNames: List<Component>
            get() = attached.mapNotNull { it.customName }

        val Entity.attachedStripped: List<String>
            get() = attached.mapNotNull { it.get<GenericKontextEntityDataResolver.CUSTOM_NAME, String>() }
    }
}
