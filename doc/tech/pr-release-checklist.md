# PR 发布检查清单

## 目标

避免父仓库 PR 已创建，但子仓库 `affective_local_sdk/src/main` 的变更没有对应 PR。

## 固定流程

1. 确认父仓库状态：

   ```bash
   git status --short --branch
   git config user.name
   git config user.email
   ```

2. 确认 GitHub CLI 身份：

   ```bash
   gh auth status
   gh api user --jq '.login'
   ```

3. 检查 submodule 指针相对目标分支是否变化：

   ```bash
   scripts/verify_submodule_prs.sh --base-ref dev_2.0
   ```

4. 如果脚本提示缺少子仓库 PR，先进入子仓库创建 PR：

   ```bash
   cd affective_local_sdk/src/main
   gh pr create --base algorithm-android-dev_2.0 --head "$(git branch --show-current)"
   ```

5. 子仓库 PR 创建并验证后，再创建父仓库 PR：

   ```bash
   gh pr create --base dev_2.0 --head "$(git branch --show-current)"
   ```

6. 父仓库 PR body 必须记录相关子仓库 PR：

   ```md
   ## Submodule PRs

   - affective_local_sdk/src/main: https://github.com/Entertech/AffectiveAlgorithm-android-SDK/pull/...
   ```

7. 创建 PR 后读取元数据确认账号、base/head 和状态：

   ```bash
   gh pr view <number> --json author,baseRefName,headRefName,state,isDraft,url,mergeable,mergeStateStatus
   ```

## 分支映射

| 父仓库 base | 子仓库 base |
| --- | --- |
| `dev_2.0` | `algorithm-android-dev_2.0` |
| `team/<name>` | `algorithm-android-team/<name>` |
| `publish_<version>` | `algorithm-android-publish_<version>` |

## 门禁脚本

`scripts/verify_submodule_prs.sh` 会检查：

- 父仓库 base/head 之间是否有 submodule gitlink 变化。
- 子仓库工作区是否 checkout 到父仓库记录的 submodule commit。
- 子仓库 head 分支是否已 push。
- 子仓库 head -> mapped child base 是否已有 open PR。

`scripts/verify_sdk_architecture.sh` 已接入该检查。
