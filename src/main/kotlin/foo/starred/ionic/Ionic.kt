@file:Suppress("ConstPropertyName")

package foo.starred.ionic

import foo.starred.ionic.annotation.loader.IonicAnnotationLoader
import foo.starred.ionic.api.command.command
import foo.starred.ionic.api.messaging.impl.MessagingAPI.displayAsMod
import foo.starred.ionic.config.ConfigManager
import foo.starred.ionic.config.theme.impl.catppuccin.MochaColorScheme
import foo.starred.kbus.impl.KBus
import foo.starred.kontext.abstraction.chat.generic.MinecraftChat
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

        command {
            "toggle" / "feature" / string("key") {
                val key = string("key")

                val boolean = ConfigManager.get(key) as? Boolean ?: return@string "Not a valid feature!".displayAsMod()
                ConfigManager.update(key, !boolean)

                val name = key.replace(Regex("(?<!^)([A-Z])"), " $1").lowercase().replaceFirstChar { it.uppercase() }
                "<${MochaColorScheme.Lavender.argb}>$name <gray>➤ ${if (boolean) "<red>Disabled" else "<green>Enabled"}".displayAsMod()
            }

            "debug" / "clear" / "chat" {
                MinecraftChat.instance.clearMessages(false)
            }
        }
    }

    object Data {
        const val version: String = /*$ mod_version*/ "0.0.2"
        const val name: String = /*$ mod_name*/ "Ionic"
        const val id: String = /*$ mod_id*/ "ionic"
    }
}
