package foo.starred.ionic.api.time

import foo.starred.kontext.api.scheduling.tickable.AbstractTickable

class IonicTickable<T>(ticks: Int = 1, block: () -> T) : AbstractTickable<T>(ticks, { IonicScheduler.ticks.client }, block)
