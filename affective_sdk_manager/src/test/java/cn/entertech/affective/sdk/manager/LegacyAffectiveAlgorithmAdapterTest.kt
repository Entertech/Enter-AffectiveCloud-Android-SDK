package cn.entertech.affective.sdk.manager

import cn.entertech.affective.sdk.api.AffectiveAlgorithmResult
import cn.entertech.affective.sdk.api.AffectiveAlgorithmState
import cn.entertech.affective.sdk.api.AffectiveInput
import cn.entertech.affective.sdk.api.AffectiveRealtimeEvent
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LegacyAffectiveAlgorithmAdapterTest {
    @Test
    fun mapsLegacyLifecycleAndInput() = runBlocking {
        val service = FakeAffectiveService().also { it.autoFinish = true }
        val adapter = LegacyAffectiveAlgorithmAdapter("local", service)

        assertIs<AffectiveAlgorithmResult.Success<Unit>>(
            adapter.connect(EnterAffectiveConfigProxy()),
        )
        assertIs<AffectiveAlgorithmResult.Success<Unit>>(adapter.start())
        assertIs<AffectiveAlgorithmResult.Success<*>>(adapter.report())
        assertIs<AffectiveAlgorithmResult.Success<Unit>>(
            adapter.append(AffectiveInput.EegSamples(listOf(1, 2), isEar = true)),
        )
        assertEquals(listOf(listOf(1, 2) to true), service.eegSamples)
        assertIs<AffectiveAlgorithmResult.Success<Unit>>(adapter.finish())
        assertEquals(AffectiveAlgorithmState.Finished, adapter.state.value)
    }

    @Test
    fun emitsKeyedEventsAndUnsubscribesWhenCollectorStops() = runBlocking {
        val service = FakeAffectiveService()
        val adapter = LegacyAffectiveAlgorithmAdapter("cloud", service)
        val event = async { adapter.realtimeEvents.first() }
        while (service.bioListener == null) yield()

        service.bioListener?.invoke(null)

        val received = assertIs<AffectiveRealtimeEvent.BioData>(event.await())
        assertEquals("cloud", received.sourceKey)
        assertTrue(service.affectiveListener == null || service.bioListener == null)
        assertNull(service.bioListener)
        assertNull(service.affectiveListener)
    }
}
