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


VERSION_RE = re.compile(r'(?P<prefix>\blibraryVersion\s*=\s*")(?P<version>[^"]+)(?P<suffix>".*)')
SEMVER_RE = re.compile(r"^(?P<major>0|[1-9]\d*)\.(?P<minor>0|[1-9]\d*)\.(?P<patch>0|[1-9]\d*)(?P<label>[-+][0-9A-Za-z.-]+)?$")


def extract_version(text):
    for line in text.splitlines():
        match = VERSION_RE.search(line)
        if match:
            return match.group("version")
    raise ReleaseError("libraryVersion was not found")


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


def replace_worktree_version(file_path, next_version):
    path = ROOT / file_path
    text = path.read_text(encoding="utf-8")
    replaced = []
    changed = False
    for line in text.splitlines(keepends=True):
        match = VERSION_RE.search(line)
        if match and not changed:
            line = f'{match.group("prefix")}{next_version}{match.group("suffix")}'
            if not line.endswith("\n"):
                line += "\n"
            changed = True
        replaced.append(line)
    if not changed:
        raise ReleaseError(f"libraryVersion was not found in {file_path}")
    path.write_text("".join(replaced), encoding="utf-8")


def version_gate(args):
    base_version = extract_version(read_file_at_ref(args.base_ref, args.version_file))
    head_text = read_file_from_worktree(args.version_file) if args.head_ref == "WORKTREE" else read_file_at_ref(args.head_ref, args.version_file)
    head_version = extract_version(head_text)
    comparison = compare_versions(head_version, base_version)
    result = {
        "base_version": base_version,
        "head_version": head_version,
        "action": "keep",
        "next_version": head_version,
    }
    if comparison < 0:
        raise ReleaseError(f"dev version {head_version} is lower than publish version {base_version}")
    if comparison == 0:
        next_version = bump_patch(head_version)
        result["action"] = "bump_patch"
        result["next_version"] = next_version
        if args.write_bump:
            replace_worktree_version(args.version_file, next_version)
    print(json.dumps(result, ensure_ascii=True, indent=2))


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
    source_version = extract_version(version_text)
    if source_version.endswith("-local"):
        publish_version = source_version
    else:
        publish_version = f"{source_version}-local"

    group_id = "cn.entertech.android"
    group_path = Path(args.maven_local_dir) / Path(group_id.replace(".", "/"))
    if not group_path.exists():
        raise ReleaseError(f"Maven local group path does not exist: {group_path}")

    tags = []
    for version_dir in sorted(group_path.glob(f"*/{publish_version}")):
        artifact_id = version_dir.parent.name
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
    gate.add_argument("--write-bump", action="store_true")
    gate.set_defaults(func=version_gate)

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
