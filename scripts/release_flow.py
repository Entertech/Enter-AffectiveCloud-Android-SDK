#!/usr/bin/env python3
import argparse
import json
import os
import re
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SUBMODULE_PATH = "affective_local_sdk/src/main"
VERSION_FILE = "version.gradle"
DEV_MARKER = "dev_"
PUBLISH_PREFIX = "publish_"
CHILD_PREFIX = "algorithm-android-"
LOCAL_SDK_EXTRA_ARTIFACT_IDS = [
    "breath-affective-offline-sdk",
    "breath-affective-offline-sdk-authentication",
    "flowtime-affective-offline-sdk",
    "flowtime-affective-offline-sdk-authentication",
    "obsession-affective-offline-sdk",
    "obsession-affective-offline-sdk-authentication",
    "sdk-affective-offline-sdk",
    "sdk-affective-offline-sdk-authentication",
]
VERSION_GET_RE = re.compile(r'rootProject\.ext\.get\("(?P<key>[A-Za-z][A-Za-z0-9_]*Version)"\)')
ARTIFACT_ID_RE = re.compile(r'artifactId\s*=\s*"(?P<artifact_id>[^"]+)"')
MODULE_LINE_RE = re.compile(r'include\("(?P<module>:[^"]+)"\)')
MODULE_ONLY_RE = re.compile(r"^:[A-Za-z0-9_.-]+$")
NON_PUBLISHABLE_MODULES = {
    "demo",
    "compatibility-tests",
}


class ReleaseError(Exception):
    pass


def run_git(args, cwd=ROOT, check=True):
    result = subprocess.run(
        ["git", *args],
        cwd=cwd,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    if check and result.returncode != 0:
        raise ReleaseError(result.stderr.strip() or result.stdout.strip())
    return result.stdout.strip()


def parse_dev_branch(branch):
    marker_index = branch.rfind(DEV_MARKER)
    if marker_index < 0:
        raise ReleaseError(f"{branch} is not a dev branch; expected dev_V or */dev_V")
    release_key = branch[marker_index + len(DEV_MARKER):]
    if not release_key:
        raise ReleaseError(f"{branch} does not contain a release key after {DEV_MARKER}")
    return release_key


def branch_mapping(parent_branch):
    release_key = parse_dev_branch(parent_branch)
    return {
        "release_key": release_key,
        "parent_dev_branch": parent_branch,
        "parent_publish_branch": f"{PUBLISH_PREFIX}{release_key}",
        "child_dev_branch": f"{CHILD_PREFIX}{parent_branch}",
        "child_publish_branch": f"{CHILD_PREFIX}{PUBLISH_PREFIX}{release_key}",
    }


def print_mapping(args):
    mapping = branch_mapping(args.parent_branch)
    if args.format == "json":
        print(json.dumps(mapping, ensure_ascii=True, indent=2))
    else:
        for key, value in mapping.items():
            print(f"{key}={value}")


def read_file_at_ref(ref, file_path):
    return run_git(["show", f"{ref}:{file_path}"])


def read_file_from_worktree(file_path):
    return (ROOT / file_path).read_text(encoding="utf-8")


def parse_settings_modules(text):
    modules = []
    for line in text.splitlines():
        match = MODULE_LINE_RE.search(line)
        if match:
            modules.append(match.group("module"))
    return modules


def discover_modules(root=ROOT):
    settings_file = root / "settings.gradle.kts"
    if settings_file.exists():
        modules = parse_settings_modules(settings_file.read_text(encoding="utf-8"))
        return [module for module in modules if module.count(":") == 1]
    return [f":{path.parent.name}" for path in sorted(root.glob("*/build.gradle.kts"))]


def discover_components(root=ROOT):
    components = []
    for module in discover_modules(root):
        module_name = module[1:]
        if module_name in NON_PUBLISHABLE_MODULES:
            continue
        build_file = root / module_name / "build.gradle.kts"
        if not build_file.exists():
            continue
        build_text = build_file.read_text(encoding="utf-8")
        version_match = VERSION_GET_RE.search(build_text)
        if not version_match:
            continue
        artifact_ids = ARTIFACT_ID_RE.findall(build_text)
        if module_name == "affective_local_sdk":
            artifact_ids = sorted(set(artifact_ids + LOCAL_SDK_EXTRA_ARTIFACT_IDS))
        if not artifact_ids:
            raise ReleaseError(f"{module_name} uses a component version but does not declare artifactId")
        components.append(
            {
                "name": module_name,
                "module": module,
                "version_key": version_match.group("key"),
                "src_paths": [f"{module_name}/src/"],
                "artifact_ids": artifact_ids,
            }
        )
    return components


VERSION_RE = re.compile(r'(?P<prefix>\b(?P<key>[A-Za-z][A-Za-z0-9_]*Version)\s*=\s*")(?P<version>[^"]+)(?P<suffix>".*)')
SEMVER_RE = re.compile(r"^(?P<major>0|[1-9]\d*)\.(?P<minor>0|[1-9]\d*)\.(?P<patch>0|[1-9]\d*)(?P<label>[-+][0-9A-Za-z.-]+)?$")


def extract_versions(text):
    versions = {}
    for line in text.splitlines():
        match = VERSION_RE.search(line)
        if match:
            versions[match.group("key")] = match.group("version")
    if not versions:
        raise ReleaseError("No component versions were found")
    return versions


def parse_version(version):
    match = SEMVER_RE.match(version)
    if not match:
        raise ReleaseError(f"{version} is not supported; expected x.y.z with optional suffix")
    return (
        int(match.group("major")),
        int(match.group("minor")),
        int(match.group("patch")),
        match.group("label") or "",
    )


def compare_versions(left, right):
    left_tuple = parse_version(left)[:3]
    right_tuple = parse_version(right)[:3]
    if left_tuple < right_tuple:
        return -1
    if left_tuple > right_tuple:
        return 1
    return 0


def bump_patch(version):
    major, minor, patch, label = parse_version(version)
    return f"{major}.{minor}.{patch + 1}{label}"


def replace_worktree_versions(file_path, next_versions):
    path = ROOT / file_path
    text = path.read_text(encoding="utf-8")
    replaced = []
    changed_keys = set()
    for line in text.splitlines(keepends=True):
        match = VERSION_RE.search(line)
        if match and match.group("key") in next_versions:
            line = f'{match.group("prefix")}{next_versions[match.group("key")]}{match.group("suffix")}'
            if not line.endswith("\n"):
                line += "\n"
            changed_keys.add(match.group("key"))
        replaced.append(line)
    missing_keys = set(next_versions) - changed_keys
    if missing_keys:
        raise ReleaseError(f"Version key(s) were not found in {file_path}: {', '.join(sorted(missing_keys))}")
    path.write_text("".join(replaced), encoding="utf-8")


def path_matches_prefix(path, prefix):
    normalized_prefix = prefix.rstrip("/")
    return path == normalized_prefix or path.startswith(f"{normalized_prefix}/")


def component_has_src_change(component, changed_files):
    return any(
        path_matches_prefix(changed_file, src_path)
        for changed_file in changed_files
        for src_path in component["src_paths"]
    )


def changed_files_between(base_ref, head_ref):
    if head_ref == "WORKTREE":
        diff_head = "HEAD"
    else:
        diff_head = head_ref
    output = run_git(["diff", "--name-only", f"{base_ref}...{diff_head}"])
    return [line for line in output.splitlines() if line]


def component_version_gate(base_versions, head_versions, changed_files, components=None):
    components = components or discover_components()
    results = []
    next_versions = {}

    for component in components:
        version_key = component["version_key"]
        if version_key not in base_versions or version_key not in head_versions:
            continue

        base_version = base_versions[version_key]
        head_version = head_versions[version_key]
        comparison = compare_versions(head_version, base_version)
        has_src_change = component_has_src_change(component, changed_files)
        result = {
            "component": component["name"],
            "version_key": version_key,
            "base_version": base_version,
            "head_version": head_version,
            "has_src_change": has_src_change,
            "action": "keep",
            "next_version": head_version,
        }
        if comparison < 0:
            raise ReleaseError(
                f"{component['name']} dev version {head_version} is lower than publish version {base_version}"
            )
        if comparison == 0 and has_src_change:
            next_version = bump_patch(head_version)
            result["action"] = "bump_patch"
            result["next_version"] = next_version
            next_versions[version_key] = next_version
        results.append(result)

    return results, next_versions


def version_gate(args):
    base_versions = extract_versions(read_file_at_ref(args.base_ref, args.version_file))
    head_text = read_file_from_worktree(args.version_file) if args.head_ref == "WORKTREE" else read_file_at_ref(args.head_ref, args.version_file)
    head_versions = extract_versions(head_text)
    changed_files = args.changed_file or changed_files_between(args.base_ref, args.head_ref)
    results, next_versions = component_version_gate(base_versions, head_versions, changed_files, discover_components())
    if args.write_bump and next_versions:
        replace_worktree_versions(args.version_file, next_versions)
    print(json.dumps({"components": results}, ensure_ascii=True, indent=2))


def select_publish_modules(base_versions, head_versions, components=None):
    components = components or discover_components()
    modules = []
    results = []

    for component in components:
        version_key = component["version_key"]
        if version_key not in base_versions or version_key not in head_versions:
            continue
        base_version = base_versions[version_key]
        head_version = head_versions[version_key]
        comparison = compare_versions(head_version, base_version)
        result = {
            "component": component["name"],
            "module": component["module"],
            "base_version": base_version,
            "head_version": head_version,
            "action": "skip",
        }
        if comparison < 0:
            raise ReleaseError(
                f"{component['name']} publish version {head_version} is lower than base version {base_version}"
            )
        if comparison > 0:
            modules.append(component["module"])
            result["action"] = "publish"
        results.append(result)

    return modules, results


def publish_modules(args):
    base_versions = extract_versions(read_file_at_ref(args.base_ref, args.version_file))
    head_text = read_file_from_worktree(args.version_file) if args.head_ref == "WORKTREE" else read_file_at_ref(args.head_ref, args.version_file)
    head_versions = extract_versions(head_text)
    modules, results = select_publish_modules(base_versions, head_versions, discover_components())

    if args.format == "json":
        print(json.dumps({"modules": modules, "components": results}, ensure_ascii=True, indent=2))
    else:
        print(" ".join(modules))


def submodule_sha(parent_ref):
    output = run_git(["ls-tree", parent_ref, SUBMODULE_PATH])
    parts = output.split()
    if len(parts) < 3 or parts[1] != "commit":
        raise ReleaseError(f"{SUBMODULE_PATH} is not recorded as a submodule at {parent_ref}")
    return parts[2]


def verify_submodule(args):
    sha = submodule_sha(args.parent_ref)
    if args.child_repo_dir:
        child_repo = Path(args.child_repo_dir)
        run_git(["fetch", args.remote, args.child_branch], cwd=child_repo)
        branch_ref = f"{args.remote}/{args.child_branch}"
        result = subprocess.run(
            ["git", "merge-base", "--is-ancestor", sha, branch_ref],
            cwd=child_repo,
            text=True,
        )
        if result.returncode != 0:
            raise ReleaseError(f"submodule commit {sha} is not contained in {branch_ref}")
    print(json.dumps({"submodule_path": SUBMODULE_PATH, "submodule_sha": sha}, ensure_ascii=True, indent=2))


def publication_tags(args):
    version_text = read_file_from_worktree(args.version_file)
    source_versions = extract_versions(version_text)
    version_key_by_artifact_id = {
        artifact_id: component["version_key"]
        for component in discover_components()
        for artifact_id in component["artifact_ids"]
    }

    group_id = "cn.entertech.android"
    group_path = Path(args.maven_local_dir) / Path(group_id.replace(".", "/"))
    if not group_path.exists():
        raise ReleaseError(f"Maven local group path does not exist: {group_path}")

    tags = []
    for version_dir in sorted(group_path.glob("*/*")):
        artifact_id = version_dir.parent.name
        version_key = version_key_by_artifact_id.get(artifact_id)
        if not version_key:
            continue
        source_version = source_versions.get(version_key)
        if not source_version:
            raise ReleaseError(f"{version_key} was not found for artifact {artifact_id}")
        publish_version = source_version if source_version.endswith("-local") else f"{source_version}-local"
        if version_dir.name != publish_version:
            continue
        aar = version_dir / f"{artifact_id}-{publish_version}.aar"
        pom = version_dir / f"{artifact_id}-{publish_version}.pom"
        module = version_dir / f"{artifact_id}-{publish_version}.module"
        if aar.exists() and pom.exists() and module.exists():
            tag_version = source_version.removesuffix("-local")
            tags.append(f"{artifact_id}-{tag_version}")

    if not tags:
        raise ReleaseError("No complete Maven AAR publications were found")

    output = "\n".join(tags) + "\n"
    if args.output:
        Path(args.output).write_text(output, encoding="utf-8")
    else:
        print(output, end="")


def main():
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest="command", required=True)

    mapping = subparsers.add_parser("map-branches")
    mapping.add_argument("--parent-branch", required=True)
    mapping.add_argument("--format", choices=["env", "json"], default="env")
    mapping.set_defaults(func=print_mapping)

    gate = subparsers.add_parser("version-gate")
    gate.add_argument("--base-ref", required=True)
    gate.add_argument("--head-ref", default="WORKTREE")
    gate.add_argument("--version-file", default=VERSION_FILE)
    gate.add_argument("--changed-file", action="append", default=[])
    gate.add_argument("--write-bump", action="store_true")
    gate.set_defaults(func=version_gate)

    modules = subparsers.add_parser("publish-modules")
    modules.add_argument("--base-ref", required=True)
    modules.add_argument("--head-ref", default="WORKTREE")
    modules.add_argument("--version-file", default=VERSION_FILE)
    modules.add_argument("--format", choices=["plain", "json"], default="plain")
    modules.set_defaults(func=publish_modules)

    submodule = subparsers.add_parser("verify-submodule")
    submodule.add_argument("--parent-ref", default="HEAD")
    submodule.add_argument("--child-repo-dir")
    submodule.add_argument("--child-branch")
    submodule.add_argument("--remote", default="origin")
    submodule.set_defaults(func=verify_submodule)

    tags = subparsers.add_parser("publication-tags")
    tags.add_argument("--maven-local-dir", required=True)
    tags.add_argument("--version-file", default=VERSION_FILE)
    tags.add_argument("--output")
    tags.set_defaults(func=publication_tags)

    args = parser.parse_args()
    try:
        args.func(args)
    except ReleaseError as error:
        print(f"release-flow: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
