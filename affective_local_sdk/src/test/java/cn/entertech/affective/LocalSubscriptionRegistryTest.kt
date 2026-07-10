package cn.entertech.affective

import cn.entertech.affectivesdk.manager.internal.LocalSubscriptionRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalSubscriptionRegistryTest {
    @Test
    fun deduplicatesAndRemovesBioListeners() {
        val registry = LocalSubscriptionRegistry()
        var calls = 0
        val listener: (cn.entertech.affective.sdk.bean.RealtimeBioData?) -> Unit = { calls++ }

        registry.addBioListener(listener)
        registry.addBioListener(listener)
        registry.dispatchBio(null)
        assertEquals(1, calls)

        registry.removeBioListener(listener)
        registry.dispatchBio(null)
        assertEquals(1, calls)
    }

    @Test
    fun clearRemovesEveryListenerType() {
        val registry = LocalSubscriptionRegistry()
        registry.addBioListener {}
        registry.addAffectiveListener {}
        registry.addConnectionListener {}
        registry.addDisconnectListener {}
        assertFalse(registry.isEmpty)

        registry.clear()

        assertTrue(registry.isEmpty)
    }
}
