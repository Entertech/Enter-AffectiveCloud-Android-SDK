package cn.entertech.affectivecloudsdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
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
    fun synchronousOperationFailureInvokesTerminalHandlerExactlyOnce() {
        var operationCalls = 0
        var failureCalls = 0

        runCallbackOperation(
            operation = {
                operationCalls += 1
                throw IllegalStateException("synchronous failure")
            },
            onFailure = {
                failureCalls += 1
            }
        )

        assertEquals(1, operationCalls)
        assertEquals(1, failureCalls)
    }

    @Test
    fun oneShotCompletionResumesOnceWhenTerminalSignalsRace() {
        val results = CopyOnWriteArrayList<Result<String>>()
        val completion = OneShotContinuation(object : Continuation<String> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<String>) {
                results += result
            }
        })
        val start = CountDownLatch(1)
        val finished = CountDownLatch(2)
        val successThread = Thread {
            try {
                start.await()
                completion.resumeWith(Result.success("success"))
            } finally {
                finished.countDown()
            }
        }
        val failureThread = Thread {
            try {
                start.await()
                completion.resumeWith(Result.failure(IllegalStateException("failure")))
            } finally {
                finished.countDown()
            }
        }

        successThread.start()
        failureThread.start()
        start.countDown()

        assertTrue("competing terminal signals did not finish", finished.await(1, TimeUnit.SECONDS))
        assertEquals(1, results.size)
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
