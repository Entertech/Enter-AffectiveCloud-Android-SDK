#!/usr/bin/env python3
import tempfile
import unittest
from pathlib import Path

import release_flow


class ReleaseFlowVersionGateTest(unittest.TestCase):
    COMPONENTS = [
        {
            "name": "affective_sdk_api",
            "module": ":affective_sdk_api",
            "version_key": "affectiveSdkApiVersion",
            "src_paths": ["affective_sdk_api/src/"],
            "artifact_ids": ["affective_sdk_api"],
        },
        {
            "name": "affective_local_sdk",
            "module": ":affective_local_sdk",
            "version_key": "affectiveLocalSdkVersion",
            "src_paths": ["affective_local_sdk/src/"],
            "artifact_ids": ["affective-offline-sdk", "flowtime-affective-offline-sdk"],
        },
        {
            "name": "affective_sdk_manager",
            "module": ":affective_sdk_manager",
            "version_key": "affectiveSdkManagerVersion",
            "src_paths": ["affective_sdk_manager/src/"],
            "artifact_ids": ["affective-sdk-manager"],
        },
    ]

    def test_bumps_only_component_with_src_change_when_versions_are_equal(self):
        base_versions = {
            "affectiveSdkApiVersion": "2.0.0-svm",
            "affectiveLocalSdkVersion": "2.0.0-svm",
        }
        head_versions = dict(base_versions)

        results, next_versions = release_flow.component_version_gate(
            base_versions,
            head_versions,
            ["affective_sdk_api/src/main/java/Foo.kt"],
            self.COMPONENTS,
        )

        api = next(result for result in results if result["version_key"] == "affectiveSdkApiVersion")
        local = next(result for result in results if result["version_key"] == "affectiveLocalSdkVersion")
        self.assertEqual("bump_patch", api["action"])
        self.assertEqual("2.0.1-svm", api["next_version"])
        self.assertEqual("keep", local["action"])
        self.assertEqual({"affectiveSdkApiVersion": "2.0.1-svm"}, next_versions)

    def test_keeps_equal_version_when_component_src_did_not_change(self):
        base_versions = {"affectiveSdkApiVersion": "2.0.0-svm"}
        head_versions = dict(base_versions)

        results, next_versions = release_flow.component_version_gate(
            base_versions,
            head_versions,
            ["affective_sdk_api/README.md", "affective_sdk_api/api/affective_sdk_api.api"],
            self.COMPONENTS,
        )

        self.assertEqual("keep", results[0]["action"])
        self.assertEqual({}, next_versions)

    def test_keeps_higher_dev_version_even_when_component_src_changed(self):
        base_versions = {"affectiveSdkApiVersion": "2.0.0-svm"}
        head_versions = {"affectiveSdkApiVersion": "2.1.0-svm"}

        results, next_versions = release_flow.component_version_gate(
            base_versions,
            head_versions,
            ["affective_sdk_api/src/main/java/Foo.kt"],
            self.COMPONENTS,
        )

        self.assertEqual("keep", results[0]["action"])
        self.assertEqual("2.1.0-svm", results[0]["next_version"])
        self.assertEqual({}, next_versions)

    def test_rejects_lower_dev_version(self):
        with self.assertRaises(release_flow.ReleaseError):
            release_flow.component_version_gate(
                {"affectiveSdkApiVersion": "2.1.0-svm"},
                {"affectiveSdkApiVersion": "2.0.0-svm"},
                [],
                self.COMPONENTS,
            )

    def test_publish_modules_selects_only_higher_component_versions(self):
        modules, results = release_flow.select_publish_modules(
            {
                "affectiveSdkApiVersion": "2.0.0-svm",
                "affectiveLocalSdkVersion": "2.0.0-svm",
                "affectiveSdkManagerVersion": "2.0.0-svm",
            },
            {
                "affectiveSdkApiVersion": "2.0.1-svm",
                "affectiveLocalSdkVersion": "2.0.0-svm",
                "affectiveSdkManagerVersion": "2.1.0-svm",
            },
            self.COMPONENTS,
        )

        self.assertEqual([":affective_sdk_api", ":affective_sdk_manager"], modules)
        actions = {result["component"]: result["action"] for result in results}
        self.assertEqual("publish", actions["affective_sdk_api"])
        self.assertEqual("skip", actions["affective_local_sdk"])
        self.assertEqual("publish", actions["affective_sdk_manager"])

    def test_publication_tags_use_component_specific_versions(self):
        original_root = release_flow.ROOT
        with tempfile.TemporaryDirectory() as temp_dir:
            try:
                temp_path = Path(temp_dir)
                release_flow.ROOT = temp_path
                (temp_path / "settings.gradle.kts").write_text(
                    'include(":affective_sdk_api")\ninclude(":affective_local_sdk")\n',
                    encoding="utf-8",
                )
                (temp_path / "affective_sdk_api").mkdir()
                (temp_path / "affective_sdk_api" / "build.gradle.kts").write_text(
                    """
val affectiveSdkApiVersion = (rootProject.ext.get("affectiveSdkApiVersion") as? String) ?: "1.0.0"
PublishInfo {
    artifactId = "affective_sdk_api"
    version = affectiveSdkApiVersion
}
""".strip(),
                    encoding="utf-8",
                )
                (temp_path / "affective_local_sdk").mkdir()
                (temp_path / "affective_local_sdk" / "build.gradle.kts").write_text(
                    """
val affectiveLocalSdkVersion = (rootProject.ext.get("affectiveLocalSdkVersion") as? String) ?: "1.0.0"
PublishInfo {
    artifactId = "affective-offline-sdk"
    version = affectiveLocalSdkVersion
}
""".strip(),
                    encoding="utf-8",
                )
                (temp_path / "version.gradle").write_text(
                    """
rootProject.ext {
   affectiveSdkApiVersion = "2.0.1-svm"
   affectiveLocalSdkVersion = "2.0.2-svm"
}
""".strip(),
                    encoding="utf-8",
                )
                maven_group = temp_path / "maven" / "cn" / "entertech" / "android"
                api_dir = maven_group / "affective_sdk_api" / "2.0.1-svm-local"
                local_dir = maven_group / "flowtime-affective-offline-sdk" / "2.0.2-svm-local"
                for artifact_dir in (api_dir, local_dir):
                    artifact_dir.mkdir(parents=True)
                    artifact_id = artifact_dir.parent.name
                    version = artifact_dir.name
                    for suffix in ("aar", "pom", "module"):
                        (artifact_dir / f"{artifact_id}-{version}.{suffix}").write_text("", encoding="utf-8")
                output_file = temp_path / "tags.txt"

                args = type(
                    "Args",
                    (),
                    {
                        "version_file": "version.gradle",
                        "maven_local_dir": str(temp_path / "maven"),
                        "output": str(output_file),
                    },
                )()
                release_flow.publication_tags(args)

                self.assertEqual(
                    [
                        "affective_sdk_api-2.0.1-svm",
                        "flowtime-affective-offline-sdk-2.0.2-svm",
                    ],
                    output_file.read_text(encoding="utf-8").splitlines(),
                )
            finally:
                release_flow.ROOT = original_root

    def test_discovers_new_publishable_component_from_settings_and_build_file(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_path = Path(temp_dir)
            (temp_path / "settings.gradle.kts").write_text(
                'include(":affective_new_component")\ninclude(":demo")\n',
                encoding="utf-8",
            )
            component_dir = temp_path / "affective_new_component"
            component_dir.mkdir()
            (component_dir / "build.gradle.kts").write_text(
                """
val affectiveNewComponentVersion = (rootProject.ext.get("affectiveNewComponentVersion") as? String) ?: "1.0.0"
PublishInfo {
    artifactId = "affective-new-component"
    version = affectiveNewComponentVersion
}
""".strip(),
                encoding="utf-8",
            )
            demo_dir = temp_path / "demo"
            demo_dir.mkdir()
            (demo_dir / "build.gradle.kts").write_text("plugins { id(\"enter.android.application\") }\n", encoding="utf-8")

            self.assertEqual(
                [
                    {
                        "name": "affective_new_component",
                        "module": ":affective_new_component",
                        "version_key": "affectiveNewComponentVersion",
                        "src_paths": ["affective_new_component/src/"],
                        "artifact_ids": ["affective-new-component"],
                    }
                ],
                release_flow.discover_components(temp_path),
            )


if __name__ == "__main__":
    unittest.main()
