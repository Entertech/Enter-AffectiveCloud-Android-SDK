#!/usr/bin/env python3
import argparse
import os
import subprocess
import sys
from pathlib import Path


def repo_root():
    configured_root = os.environ.get("SUBMODULE_SYNC_ROOT")
    if configured_root:
        return Path(configured_root).resolve()
    return Path(__file__).resolve().parents[1]


ROOT = repo_root()
SUBMODULE_PATH = "affective_local_sdk/src/main"
CHILD_BRANCH_PREFIX = "algorithm-android-"
BASE_BRANCHES = {"svm-main"}


class SyncError(Exception):
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
        raise SyncError(result.stderr.strip() or result.stdout.strip())
    return result.stdout.strip()


def current_branch(cwd):
    branch = run_git(["branch", "--show-current"], cwd=cwd)
    return branch or None


def child_branch_for_parent(parent_branch):
    if parent_branch in BASE_BRANCHES:
        return parent_branch
    if parent_branch.startswith(CHILD_BRANCH_PREFIX):
        return parent_branch
    return f"{CHILD_BRANCH_PREFIX}{parent_branch}"


def parent_recorded_submodule_sha(parent_ref="HEAD"):
    output = run_git(["ls-tree", parent_ref, SUBMODULE_PATH])
    parts = output.split()
    if len(parts) < 3 or parts[1] != "commit":
        raise SyncError(f"{SUBMODULE_PATH} is not recorded as a submodule at {parent_ref}")
    return parts[2]


def ensure_clean_child(child_dir):
    status = run_git(["status", "--porcelain"], cwd=child_dir)
    if status:
        raise SyncError(f"{SUBMODULE_PATH} has uncommitted changes; commit or stash them before syncing branches")


def local_branch_exists(branch, cwd):
    return subprocess.run(
        ["git", "show-ref", "--verify", "--quiet", f"refs/heads/{branch}"],
        cwd=cwd,
    ).returncode == 0


def remote_branch_exists(branch, cwd, remote):
    return subprocess.run(
        ["git", "ls-remote", "--exit-code", "--heads", remote, branch],
        cwd=cwd,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    ).returncode == 0


def checkout_child_branch(child_dir, branch, recorded_sha, remote, create_missing):
    run_git(["fetch", remote, "--prune"], cwd=child_dir)
    ensure_clean_child(child_dir)

    if local_branch_exists(branch, child_dir):
        run_git(["switch", branch], cwd=child_dir)
    elif remote_branch_exists(branch, child_dir, remote):
        run_git(["switch", "--track", "-c", branch, f"{remote}/{branch}"], cwd=child_dir)
    elif create_missing:
        run_git(["switch", "-c", branch, recorded_sha], cwd=child_dir)
    else:
        raise SyncError(f"child branch {branch} does not exist")


def stage_parent_submodule():
    run_git(["add", SUBMODULE_PATH])


def sync_checkout(args):
    parent_branch = args.parent_branch or current_branch(ROOT)
    if not parent_branch:
        return

    child_dir = ROOT / SUBMODULE_PATH
    if not child_dir.exists():
        run_git(["submodule", "update", "--init", "--recursive", SUBMODULE_PATH])

    recorded_sha = parent_recorded_submodule_sha("HEAD")
    child_branch = child_branch_for_parent(parent_branch)
    checkout_child_branch(
        child_dir=child_dir,
        branch=child_branch,
        recorded_sha=recorded_sha,
        remote=args.remote,
        create_missing=args.create_missing,
    )

    child_head = run_git(["rev-parse", "HEAD"], cwd=child_dir)
    if args.stage_parent and child_head != recorded_sha:
        stage_parent_submodule()

    print(f"parent={parent_branch}")
    print(f"child={child_branch}")
    print(f"recorded_sha={recorded_sha}")
    print(f"child_head={child_head}")


def record_child(args):
    child_dir = ROOT / SUBMODULE_PATH
    child_branch = current_branch(child_dir) or "DETACHED"
    child_head = run_git(["rev-parse", "HEAD"], cwd=child_dir)
    stage_parent_submodule()
    print(f"recorded {SUBMODULE_PATH} {child_branch}@{child_head} in parent index")


def print_mapping(args):
    print(child_branch_for_parent(args.parent_branch))


def main():
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest="command", required=True)

    mapping = subparsers.add_parser("map")
    mapping.add_argument("parent_branch")
    mapping.set_defaults(func=print_mapping)

    checkout = subparsers.add_parser("sync-checkout")
    checkout.add_argument("--parent-branch")
    checkout.add_argument("--remote", default="origin")
    checkout.add_argument("--create-missing", action="store_true")
    checkout.add_argument("--stage-parent", action="store_true")
    checkout.set_defaults(func=sync_checkout)

    record = subparsers.add_parser("record-child")
    record.set_defaults(func=record_child)

    args = parser.parse_args()
    try:
        args.func(args)
    except SyncError as error:
        print(f"submodule-branch-sync: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
