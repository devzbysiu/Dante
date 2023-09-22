package dev.zbysiu.tracking

import dev.zbysiu.tracking.event.DanteTrackingEvent
import dev.zbysiu.tracking.properties.BaseProperty
import timber.log.Timber

class DebugTracker : Tracker() {

    override fun trackEvent(event: DanteTrackingEvent) {
        Timber.d("Event: ${event.name} - ${createTrackEventData(event.props)}")
    }

    private fun createTrackEventData(props: List<BaseProperty<Any>>): Map<String, Any> {
        return props.associateTo(mutableMapOf()) { p ->
            p.getKey() to p.value
        }
    }
}