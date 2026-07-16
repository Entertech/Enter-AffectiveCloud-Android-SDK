# affective_sdk_manager

## 功能

`affective_sdk_manager` 用于按 key 管理多个 `IAffectiveDataAnalysisService` 实例。典型场景：

- 本地算法和云端算法同时运行。
- 多设备或多路算法实例并行。
- 同一份硬件数据广播给多个算法服务。
- 统一监听连接、启动、报表、结束和实时数据回调。

## 版本

- groupId: `cn.entertech.android`
- artifactId: `affective-sdk-manager`
- version: 来自根目录 [`version.gradle`](../version.gradle) 的 `affectiveSdkManagerVersion`

## 使用方式

源码工程依赖：

```groovy
dependencies {
    implementation project(':affective_sdk_api')
    implementation project(':affective_sdk_manager')
}
```

如果要管理本地和云端服务，还需要加入对应实现组件：

```groovy
implementation project(':affective_local_sdk')
implementation project(':affective_cloud_sdk')
```

显式注册服务：

```kotlin
val manager = AffectiveManager<String>()
manager.registerAffectiveService("local", EnterAffectiveLocalService())
manager.registerAffectiveService("cloud", EnterAffectiveCloudService())
```

启动、订阅和广播数据：

```kotlin
manager.connectAndStartAffectiveService("local")
manager.connectAndStartAffectiveService("cloud", cloudConfig)

manager.subscribeData("local")
manager.subscribeData("cloud")

manager.appendEEGDataToAll(bytes)
manager.appendHeartRateDataToAll(heartRate)
```

## 注意事项

- Manager 不再默认自动发现服务。业务必须先 `registerAffectiveService(...)`，或调用 `getAffectiveService(key, default)` 传入默认实例，或在子类中重写 `initAffectiveService(...)`。
- 默认 `initAffectiveService(...)` 会抛 `IllegalStateException`，避免静默 no-op。
- 使用本模块时 Java/Kotlin JVM target 为 17。
- `finishService(key)` 会在终态回调后清理对应 service 状态。
