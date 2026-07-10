import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

internal object ModuleDependencyRules {
    private val relevantConfigurations = setOf(
        "api",
        "implementation",
        "debugApi",
        "debugImplementation",
        "releaseApi",
        "releaseImplementation",
    )

    private val allowedEdges = setOf(
        ":demo" to ":affective_local_sdk",
        ":demo" to ":affective_cloud_sdk",
        ":demo" to ":affective_sdk_manager",
        ":demo" to ":affective_cloud_protocol",
        ":demo" to ":affective_data_provider",
        ":demo" to ":affective_sdk_api",
        ":affective_local_sdk" to ":affective_sdk_api",
        ":affective_cloud_sdk" to ":affective_sdk_api",
        ":affective_cloud_sdk" to ":affective_cloud_protocol",
        ":affective_sdk_manager" to ":affective_sdk_api",
        ":affective_cloud_protocol" to ":affective_sdk_api",
        ":affective_data_provider" to ":affective_sdk_api",
        ":compatibility-tests:legacy-consumer" to ":affective_sdk_api",
    )

    fun isAllowed(from: String, to: String): Boolean = from to to in allowedEdges

    fun isRelevantConfiguration(name: String): Boolean = name in relevantConfigurations
}

class ModuleDependencyAnalysisPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        require(target == target.rootProject) {
            "enter.module-dependency-analysis must be applied to the root project"
        }

        target.tasks.register("analyzeModuleDependencies") {
            group = "verification"
            description = "Validates the documented SDK project dependency boundaries."

            doLast {
                val violations = target.subprojects
                    .flatMap { project ->
                        project.configurations
                            .filter { configuration ->
                                ModuleDependencyRules.isRelevantConfiguration(configuration.name)
                            }
                            .flatMap { configuration ->
                            configuration.dependencies
                                .withType(ProjectDependency::class.java)
                                .map { dependency -> project.path to dependency.path }
                        }
                    }
                    .distinct()
                    .filterNot { (from, to) -> from == to }
                    .filterNot { (from, to) -> ModuleDependencyRules.isAllowed(from, to) }
                    .sortedWith(compareBy<Pair<String, String>> { it.first }.thenBy { it.second })

                if (violations.isNotEmpty()) {
                    throw GradleException(
                        violations.joinToString(
                            prefix = "Forbidden SDK module dependencies:\n",
                            separator = "\n",
                        ) { (from, to) -> " - $from -> $to" },
                    )
                }
            }
        }
    }
}
