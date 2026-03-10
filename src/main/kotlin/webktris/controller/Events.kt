package webktris.controller

import engine.model.events.Event

data class GaugeFull(override val gameId: String) : Event
data class GaugeChanged(override val gameId: String, val gaugeValue: Double) : Event

data class ZoneInput(override val gameId: String) : Event
data class ZoneActivated(override val gameId: String) : Event
data class ZoneDeactivated(override val gameId: String) : Event