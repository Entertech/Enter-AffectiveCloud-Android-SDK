package cn.entertech.affective.sdk.manager

import cn.entertech.affective.sdk.manager.internal.AlgorithmRegistry
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class AlgorithmRegistryTest {
    @Test
    fun storesIndependentAlgorithmSlots() {
        val registry = AlgorithmRegistry<String>()
        val local = FakeAffectiveService()
        val cloud = FakeAffectiveService()

        registry.register("local", local)
        registry.register("cloud", cloud)

        assertSame(local, registry.get("local")?.service)
        assertSame(cloud, registry.get("cloud")?.service)
    }

    @Test
    fun removeClearsSlotOwnedListeners() {
        val registry = AlgorithmRegistry<String>()
        val slot = registry.register("local", FakeAffectiveService())
        slot.bioDataListener = {}
        slot.affectiveDataListener = {}

        registry.remove("local")

        assertNull(slot.bioDataListener)
        assertNull(slot.affectiveDataListener)
        assertNull(registry.get("local"))
    }
}
