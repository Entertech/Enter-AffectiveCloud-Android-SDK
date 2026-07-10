package cn.entertech.affectivecloudsdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CancellationException
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

        CallbackOperationBoundary(
            onFailure = {
                failureCalls += 1
            }
        ).run {
            operationCalls += 1
            throw IllegalStateException("synchronous failure")
        }

        assertEquals(1, operationCalls)
        assertEquals(1, failureCalls)
    }

    @Test
    fun cancellationIsRethrownWithoutInvokingManagerFailureHandler() {
        val expected = CancellationException("cancelled")
        var failureCalls = 0

        val actual = assertThrows(CancellationException::class.java) {
            CallbackOperationBoundary(
                onFailure = {
                    failureCalls += 1
                }
            ).run {
                throw expected
            }
        }

        assertSame(expected, actual)
        assertEquals(0, failureCalls)
    }

    @Test
    fun consumerCallbackExceptionIsRethrownWithoutInvokingManagerFailureHandler() {
        val expected = IllegalStateException("consumer callback failed")
        var failureCalls = 0
        val boundary = CallbackOperationBoundary(
            onFailure = {
                failureCalls += 1
            }
        )

        val actual = assertThrows(IllegalStateException::class.java) {
            boundary.run {
                boundary.callback {
                    throw expected
                }
            }
        }

        assertSame(expected, actual)
        assertEquals(0, failureCalls)
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
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val finished = CountDownLatch(2)
        val successThread = Thread {
            try {
                ready.countDown()
                start.await()
                completion.resumeWith(Result.success("success"))
            } finally {
                finished.countDown()
            }
        }
        val failureThread = Thread {
            try {
                ready.countDown()
                start.await()
                completion.resumeWith(Result.failure(IllegalStateException("failure")))
            } finally {
                finished.countDown()
            }
        }

        successThread.isDaemon = true
        failureThread.isDaemon = true
        try {
            successThread.start()
            failureThread.start()
            assertTrue(
                "competing terminal signals were not ready",
                ready.await(10, TimeUnit.SECONDS)
            )
            start.countDown()

            assertTrue(
                "competing terminal signals did not finish",
                finished.await(10, TimeUnit.SECONDS)
            )
            assertEquals(1, results.size)
        } finally {
            start.countDown()
            try {
                joinOrInterrupt(successThread)
            } finally {
                try {
                    joinOrInterrupt(failureThread)
                } finally {
                    assertTrue(
                        "competing terminal signal threads did not stop",
                        !successThread.isAlive && !failureThread.isAlive
                    )
                }
            }
        }
    }

    private fun joinOrInterrupt(thread: Thread) {
        thread.join(10_000L)
        if (thread.isAlive) {
            thread.interrupt()
            thread.join(10_000L)
        }
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
