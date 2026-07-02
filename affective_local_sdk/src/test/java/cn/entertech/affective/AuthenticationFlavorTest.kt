package cn.entertech.affective

import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affectivesdk.BuildConfig
import cn.entertech.affectivesdk.manager.EnterAffectiveLocalService
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test

class AuthenticationFlavorTest {
    @Test
    fun authFlavorRejectsStartWithoutAuthentication() {
        assumeTrue(BuildConfig.REQUIRE_AUTHENTICATION)

        var startFailError: Error? = null
        val service = EnterAffectiveLocalService()

        try {
            service.startAffectiveService(object : IStartAffectiveServiceLister {
                override fun startFail(error: Error?) {
                    startFailError = error
                }
            })
        } catch (_: RuntimeException) {
            // The pre-flavor implementation reaches native initialization instead of failing fast.
        }

        assertEquals("authentication is required", startFailError?.msg)
    }
}
