[English Readme](README_EN.md)

# Enter-AffectiveCloud-Android-SDK

回车情感云 Android SDK 的组件化源码仓库。项目包含统一 API、本地算法、云端算法、多算法管理、实时传输协议、展示数据处理和 Demo。

## 项目结构

| 路径 | 组件 | 说明 |
| --- | --- | --- |
| [`affective_sdk_api`](affective_sdk_api/README.md) | `affective_sdk_api` | 统一公共契约，包含 `IAffectiveDataAnalysisService`、输入类型、结果类型、实时事件和协程/Flow 扩展。 |
| [`affective_local_sdk`](affective_local_sdk/README.md) | `affective_local_sdk` | 本地离线算法组件，提供 `EnterAffectiveLocalService`。算法源码位于 `affective_local_sdk/src/main` 子仓库。 |
| [`affective_cloud_sdk`](affective_cloud_sdk/README.md) | `affective_cloud_sdk` | 云端情感算法组件，提供 `EnterAffectiveCloudService`，并保留旧版云端 Manager/API 兼容入口。 |
| [`affective_sdk_manager`](affective_sdk_manager/README.md) | `affective_sdk_manager` | 按 key 管理多个 `IAffectiveDataAnalysisService` 实例，支持本地/云端并行和广播硬件数据。 |
| [`affective_cloud_protocol`](affective_cloud_protocol/README.md) | `affective_cloud_protocol` | 实时生物数据和情感数据的传输协议编码。 |
| [`affective_data_provider`](affective_data_provider/README.md) | `affective_data_provider` | UI 展示数据的选择、过滤、缓存和滑动窗口处理。 |
| [`demo`](demo/README.md) | `demo` | Android Demo，展示蓝牙采集、本地/云端算法切换、上传、报表和结束流程。 |

## 版本

组件版本统一在 [`version.gradle`](version.gradle) 中维护：

```groovy
rootProject.ext {
   groupId = "cn.entertech.android"
   affectiveSdkApiVersion = "2.0.0-svm"
   affectiveLocalSdkVersion = "2.0.0-svm"
   affectiveCloudSdkVersion = "2.0.0-svm"
   affectiveSdkManagerVersion = "2.0.0-svm"
   affectiveCloudProtocolVersion = "2.0.0-svm"
   affectiveDataProviderVersion = "2.0.0-svm"
}
```

各组件的 README 会列出自己的 artifactId 和版本字段；发布包坐标、仓库地址和交付方式以发布说明或交付文档为准。

## 仓库初始化

本仓库通过 Git submodule 记录本地算法源码：

```bash
git submodule update --init --recursive
```

新设备建议执行 bootstrap，它会初始化子仓库并安装本地分支同步 hooks：

```bash
./scripts/bootstrap.sh
```

Windows 可使用：

```bat
scripts\bootstrap.cmd
```

或：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\bootstrap.ps1
```

注意：Git hooks 是本机配置，不会随 Git 自动启用。每台新设备都需要执行一次 bootstrap。

## 源码工程使用

仓库内 Demo 直接组合全部产品组件。业务工程只添加需要的组件即可；如果通过统一接口操作本地/云端，建议显式依赖 `affective_sdk_api`，并同时依赖会被实际创建的实现组件。

```groovy
dependencies {
    implementation project(':affective_sdk_api')
    implementation project(':affective_local_sdk')
    implementation project(':affective_cloud_sdk')
    implementation project(':affective_sdk_manager')
    implementation project(':affective_cloud_protocol')
    implementation project(':affective_data_provider')
}
```

当前完整组件组合要求 `minSdk 24`。源码构建使用 Android SDK 36；使用 `affective_sdk_manager` 时请将 Java/Kotlin JVM target 配置为 17。

## 推荐调用方式

业务代码优先面向 `IAffectiveDataAnalysisService` 编程。`affective_sdk_api` 只定义公共契约，不负责自动发现或创建实现；业务必须自己创建、注入或注册本地/云端服务实例。

```kotlin
val service: IAffectiveDataAnalysisService = EnterAffectiveLocalService()

lifecycleScope.launch {
    service.awaitConnectAffectiveService(EnterAffectiveConfigProxy())
    service.awaitStartAffectiveService()
    service.append(AffectiveInput.EegBytes(bytes))
    service.append(AffectiveInput.HeartRate(heartRate))
}
```

本地和云端切换时，替换的是 `IAffectiveDataAnalysisService` 背后的实现实例。需要本地和云端同时运行时，使用 `AffectiveManager` 显式注册：

```kotlin
val manager = AffectiveManager<String>()
manager.registerAffectiveService("local", EnterAffectiveLocalService())
manager.registerAffectiveService("cloud", EnterAffectiveCloudService())
```

`AffectiveManager` 默认不会自动发现服务；未注册服务且未重写 `initAffectiveService(...)` 时会抛异常，业务端必须显式处理。

## 权限

云端算法或 Web 实时传输需要网络权限：

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

蓝牙采集相关权限请参考 Demo 和蓝牙 SDK 文档。

## 注意事项

- 不要在 `affective_sdk_api` 中查找服务工厂；它只提供接口、类型和扩展函数。
- 硬件数据推荐通过 `append(AffectiveInput)` 追加；旧的 `appendByteArrayData`、`appendIntData`、`appendIntListData` 仅保留兼容。
- 本地算法源码位于 submodule，修改后需要先提交并推送子仓库，再提交父仓库中的 submodule 指针。
- `affective_sdk_api` 承诺源码/二进制兼容；其他实现组件升级时应锁定同一版本并跑集成测试。
- 云端算法需要有效的 `APP_KEY`、`APP_SECRET` 和已开通服务。详细接入见 [`affective_cloud_sdk/README.md`](affective_cloud_sdk/README.md)。

## 其他文档

- [云端算法接入](affective_cloud_sdk/README.md)
- [本地算法接入](affective_local_sdk/README.md)
- [统一 API](affective_sdk_api/README.md)
- [多算法管理](affective_sdk_manager/README.md)
- [实时传输协议](affective_cloud_protocol/README.md)
- [展示数据处理](affective_data_provider/README.md)
- [Demo 使用说明](demo/README.md)
- [重构设计文档](doc/tech/affective-sdk-refactoring-design.md)
- [发布检查清单](doc/tech/pr-release-checklist.md)
- [更新日志](https://github.com/Entertech/Enter-AffectiveCloud-Android-SDK/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)
