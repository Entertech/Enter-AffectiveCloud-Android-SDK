package cn.entertech.affectivecloudsdk

import org.junit.Assume.assumeTrue

internal class CloudTestCredentials(val appKey: String, val appSecret: String, val userId: String) {
    companion object {
        fun fromEnvironment(prefix: String): CloudTestCredentials {
            val names = listOf("${prefix}_APP_KEY", "${prefix}_APP_SECRET", "${prefix}_USER_ID")
            val values = names.associateWith { System.getenv(it).orEmpty() }
            val missing = names.filter { values.getValue(it).isBlank() }

            assumeTrue("Set ${names.joinToString()} to run this integration suite", missing.size != names.size)
            check(missing.isEmpty()) { "Missing integration test settings: ${missing.joinToString()}" }

            return CloudTestCredentials(
                values.getValue(names[0]),
                values.getValue(names[1]),
                values.getValue(names[2])
            )
        }
    }
}
