@file:Suppress("ConstPropertyName")

package foo.starred.ionic

import foo.starred.ionic.annotation.loader.IonicAnnotationLoader
import foo.starred.kbus.impl.KBus
import foo.starred.updater.logic.source.impl.GitHubUpdateSource
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Ionic : ClientModInitializer {
    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(Data.name)

    @JvmField
    val BUS: KBus = KBus()

    override fun onInitializeClient() {
        GitHubUpdateSource("skies-starred/Ionic", Data.id).init(Data.version)
        IonicAnnotationLoader.init()
    }

    object Data {
        const val version: String = /*$ mod_version*/ "0.0.2"
        const val name: String = /*$ mod_name*/ "Ionic"
        const val id: String = /*$ mod_id*/ "ionic"
    }
}
