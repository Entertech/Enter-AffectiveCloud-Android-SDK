package cn.entertech.affective.sdk.manager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class AffectiveManagerMultiAlgorithmTest {
    @Test
    fun explicitRegistrationDoesNotUseDefaultDiscoveryFallback() {
        val manager = object : AffectiveManager<String>() {
            override fun initAffectiveService(serviceType: cn.entertech.affective.sdk.bean.AffectiveServiceWay) =
                error("default discovery fallback must not be called")
        }
        val service = FakeAffectiveService()

        manager.registerAffectiveService("local", service)

        assertSame(service, manager.getAffectiveService("local"))
    }

    @Test
    fun broadcastsEegToEveryRegisteredAlgorithm() {
        val manager = AffectiveManager<String>()
        val local = FakeAffectiveService()
        val cloud = FakeAffectiveService()
        manager.registerAffectiveService("local", local)
        manager.registerAffectiveService("cloud", cloud)

        manager.appendEEGDataToAll(listOf(1, 2, 3), isEar = true)

        assertEquals(listOf(listOf(1, 2, 3) to true), local.eegSamples)
        assertEquals(listOf(listOf(1, 2, 3) to true), cloud.eegSamples)
    }

    @Test
    fun oneAlgorithmFailureDoesNotBlockOtherAlgorithms() {
        val manager = AffectiveManager<String>()
        val failing = FakeAffectiveService().also { it.failOnEeg = true }
        val healthy = FakeAffectiveService()
        manager.registerAffectiveService("failing", failing)
        manager.registerAffectiveService("healthy", healthy)

        manager.appendEEGDataToAll(listOf(7), isEar = false)

        assertEquals(listOf(listOf(7) to false), healthy.eegSamples)
    }

    @Test
    fun finishRetainsServiceUntilTerminalCallback() {
        val manager = AffectiveManager<String>()
        val service = FakeAffectiveService()
        manager.registerAffectiveService("local", service)

        manager.finishService("local")
        assertSame(service, manager.getAffectiveService("local"))

        service.finishListener?.finishSuccess()
        assertNull(manager.unregisterAffectiveService("local"))
    }

    @Test
    fun subscriptionCallbackKeepsOriginatingKey() {
        val manager = AffectiveManager<String>()
        val local = FakeAffectiveService()
        val cloud = FakeAffectiveService()
        val keys = mutableListOf<String>()
        manager.registerAffectiveService("local", local)
        manager.registerAffectiveService("cloud", cloud)
        manager.addSubscribeDataListener(bioDataListener = { key, _ -> keys += key })

        manager.subscribeData("local")
        manager.subscribeData("cloud")
        local.bioListener?.invoke(null)
        cloud.bioListener?.invoke(null)

        assertEquals(listOf("local", "cloud"), keys)
    }
}
