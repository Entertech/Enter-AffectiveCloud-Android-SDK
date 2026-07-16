package cn.entertech.affective.sdk.manager

import kotlin.test.Test
import kotlin.test.assertFalse

class AffectiveManagerInternalArchitectureTest {
    @Test
    fun managerDoesNotKeepSeparateRegistryOrSlotAbstractions() {
        assertFalse(classExists("cn.entertech.affective.sdk.manager.internal.AlgorithmRegistry"))
        assertFalse(classExists("cn.entertech.affective.sdk.manager.internal.AlgorithmSlot"))
    }

    private fun classExists(name: String): Boolean = runCatching { Class.forName(name) }.isSuccess
}
