package cn.entertech.affective

import cn.entertech.affectivesdk.manager.internal.LocalAlgorithmState
import cn.entertech.affectivesdk.manager.internal.LocalSessionStateOwner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalSessionStateOwnerTest {
    @Test
    fun rejectsRunningBeforeConnectedAndStarting() {
        val owner = LocalSessionStateOwner()

        assertFailsWith<IllegalStateException> { owner.markRunning() }
        owner.markConnected()
        assertFailsWith<IllegalStateException> { owner.markRunning() }
        owner.markStarting()
        owner.markRunning()

        assertEquals(LocalAlgorithmState.Running, owner.state)
        assertTrue(owner.isConnected)
        assertTrue(owner.isRunning)
    }

    @Test
    fun disconnectResetsConnectionAndRunningFlags() {
        val owner = LocalSessionStateOwner()
        owner.markConnected()
        owner.markStarting()
        owner.markRunning()

        owner.markDisconnected()

        assertEquals(LocalAlgorithmState.Disconnected, owner.state)
        assertFalse(owner.isConnected)
        assertFalse(owner.isRunning)
    }
}
