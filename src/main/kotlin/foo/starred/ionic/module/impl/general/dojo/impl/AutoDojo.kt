package foo.starred.ionic.module.impl.general.dojo.impl

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.event.impl.MessageEvent
import foo.starred.ionic.module.base.Module
import foo.starred.ionic.module.impl.general.dojo.data.AutoDojoType
import foo.starred.kontext.api.data.Observable
import foo.starred.kontext.api.data.Observable.Companion.and

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.CRIMSON_ISLE])
object AutoDojo : Module(
    "Auto dojo",
    "Automatically does dojo",
    ConfigCategory.GENERAL
) {
    val controlGroup by config.group("Control")
    private val enabled0 = controlGroup.switch("Enable auto-control").unique("enabled0")

    val disciplineGroup by config.group("Discipline")
    private val enabled1 = controlGroup.switch("Enable auto-discipline").unique("enabled1")

    val forceGroup by config.group("Force")
    private val enabled2 = controlGroup.switch("Enable auto-force").unique("enabled2")

    val masteryGroup by config.group("Force")
    private val enabled3 = controlGroup.switch("Enable auto-mastery").unique("enabled3")

    val current: Observable<AutoDojoType?> = Observable(null)

    val control: Observable<Boolean> by lazy {
        observable and enabled0.state and current.map { it == AutoDojoType.CONTROL }
    }

    val discipline: Observable<Boolean> by lazy {
        observable and enabled1.state and current.map { it == AutoDojoType.DISCIPLINE }
    }

    val force: Observable<Boolean> by lazy {
        observable and enabled2.state and current.map { it == AutoDojoType.FORCE }
    }

    val mastery: Observable<Boolean> by lazy {
        observable and enabled3.state and current.map { it == AutoDojoType.MASTERY }
    }

    init {
        on<MessageEvent.Chat.Receive> {
            val trimmed = stripped.trim()

            when {
                trimmed.startsWith("Your Rank: ") -> {
                    current.value = null
                }

                trimmed.endsWith("OBJECTIVES") -> {
                    current.value = AutoDojoType.find(trimmed) ?: return@on
                }
            }
        }
    }
}
