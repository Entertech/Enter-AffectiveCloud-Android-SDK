package cn.entertech.affectivecloudsdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class EnterAffectiveCloudServiceSuspendTest {

    @Test
    fun suspendFinishCompletesWhenServiceIsAlreadyDisconnected() {
        val result = awaitCompletion {
            EnterAffectiveCloudService().suspendFinishAffectiveService()
        }

        assertTrue(result.isSuccess)
    }

    @Test
    fun suspendReportFailsClearlyWhenManagerIsMissing() {
        val result = awaitCompletion {
            EnterAffectiveCloudService().suspendGetReport(needFinishService = false)
        }

        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message.orEmpty().contains("not connected", ignoreCase = true)
        )
    }

    @Test
    fun oneShotCompletionIgnoresTerminalSignalsAfterTheFirst() {
        val results = mutableListOf<Result<String>>()
        val completion = OneShotContinuation(object : Continuation<String> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<String>) {
                results += result
            }
        })

        completion.resumeWith(Result.success("first"))
        completion.resumeWith(Result.failure(IllegalStateException("second")))

        assertEquals(1, results.size)
        assertEquals("first", results.single().getOrThrow())
    }

    private fun <T> awaitCompletion(block: suspend () -> T): Result<T> {
        val latch = CountDownLatch(1)
        var completion: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                completion = result
                latch.countDown()
            }
        })

        assertTrue("suspend call did not complete", latch.await(1, TimeUnit.SECONDS))
        return completion ?: throw AssertionError("continuation completed without a result")
    }
}
