package foo.starred.ionic.module.impl.general

import foo.starred.ionic.annotation.impl.IonicModuleScope
import foo.starred.ionic.annotation.impl.LoadableIonicModule
import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.ionic.config.dsl.impl.category.ConfigCategory
import foo.starred.ionic.module.base.Module

@LoadableIonicModule
@IonicModuleScope(islands = [PresetSkyBlockIsland.THE_CATACOMBS])
object WorldBorderInteract : Module(
    "World border interact",
    "Fixes interaction cancel when outside world border (Shadow Assassin, Bonzo)",
    ConfigCategory.GENERAL
)
