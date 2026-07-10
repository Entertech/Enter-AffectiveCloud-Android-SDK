import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModuleDependencyRulesTest {
    @Test
    fun allowsDocumentedEdges() {
        assertTrue(ModuleDependencyRules.isAllowed(":demo", ":affective_local_sdk"))
        assertTrue(ModuleDependencyRules.isAllowed(":demo", ":affective_cloud_sdk"))
        assertTrue(ModuleDependencyRules.isAllowed(":affective_cloud_sdk", ":affective_cloud_protocol"))
        assertTrue(ModuleDependencyRules.isAllowed(":affective_sdk_manager", ":affective_sdk_api"))
    }

    @Test
    fun rejectsHorizontalAndReverseEdges() {
        assertFalse(ModuleDependencyRules.isAllowed(":affective_sdk_manager", ":affective_local_sdk"))
        assertFalse(ModuleDependencyRules.isAllowed(":affective_data_provider", ":affective_sdk_manager"))
        assertFalse(ModuleDependencyRules.isAllowed(":affective_sdk_api", ":affective_local_sdk"))
    }

    @Test
    fun inspectsOnlyDeclarableProductionConfigurations() {
        assertTrue(ModuleDependencyRules.isRelevantConfiguration("implementation"))
        assertTrue(ModuleDependencyRules.isRelevantConfiguration("debugImplementation"))
        assertFalse(ModuleDependencyRules.isRelevantConfiguration("debugRuntimeClasspath"))
        assertFalse(ModuleDependencyRules.isRelevantConfiguration("releaseSourcesElements"))
        assertFalse(ModuleDependencyRules.isRelevantConfiguration("testImplementation"))
    }
}
