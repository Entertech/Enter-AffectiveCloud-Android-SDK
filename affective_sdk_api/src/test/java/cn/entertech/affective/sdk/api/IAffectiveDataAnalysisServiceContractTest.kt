package cn.entertech.affective.sdk.api

import org.junit.Assert.assertFalse
import org.junit.Test

class IAffectiveDataAnalysisServiceContractTest {
    @Test
    fun companionDoesNotConstructOrDiscoverServiceImplementations() {
        val companionMethods = IAffectiveDataAnalysisService.Companion::class.java.methods
            .map { it.name }
            .toSet()

        assertFalse("initialize" in companionMethods)
        assertFalse("getService" in companionMethods)
    }

    @Test
    fun apiModuleDoesNotExposeServiceDiscoveryContract() {
        val serviceEntryPointExists = runCatching {
            Class.forName("cn.entertech.affective.sdk.api.AffectiveServiceEntryPoint")
        }.isSuccess

        assertFalse(serviceEntryPointExists)
    }
}
