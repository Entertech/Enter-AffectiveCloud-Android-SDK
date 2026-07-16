# affective_sdk_api

## 功能

`affective_sdk_api` 是 SDK 的公共契约模块。它定义本地算法、云端算法和业务层共同使用的接口与数据类型：

- `IAffectiveDataAnalysisService`：连接、启动、订阅、追加数据、获取报表、结束服务的统一入口。
- `AffectiveInput`：硬件输入的类型化封装，例如 EEG、SCEEG、HR、PEPR、BCG、Gyro。
- `AffectiveServiceResult`：协程 API 的成功/失败结果。
- `AffectiveRealtimeEvent`：实时生物数据和情感数据事件。
- `AffectiveDataAnalysisServiceExtensions`：`awaitConnectAffectiveService`、`awaitStartAffectiveService`、`awaitGetReport`、`awaitFinishAffectiveService`、`realtimeEvents`。

本模块不创建、不发现具体算法实现。业务代码需要直接依赖本地或云端组件，并显式创建对应 service。

## 版本

- groupId: `cn.entertech.android`
- artifactId: `affective_sdk_api`
- version: 来自根目录 [`version.gradle`](../version.gradle) 的 `affectiveSdkApiVersion`

## 使用方式

源码工程依赖：

```groovy
dependencies {
    implementation project(':affective_sdk_api')
}
```

通常还需要依赖一个实现组件：

```groovy
implementation project(':affective_local_sdk')
// 或
implementation project(':affective_cloud_sdk')
```

## 示例

```kotlin
val service: IAffectiveDataAnalysisService = EnterAffectiveLocalService()

lifecycleScope.launch {
    val connected = service.awaitConnectAffectiveService(EnterAffectiveConfigProxy())
    if (connected is AffectiveServiceResult.Failure) return@launch

    val started = service.awaitStartAffectiveService()
    if (started is AffectiveServiceResult.Failure) return@launch

    service.append(AffectiveInput.EegBytes(bytes))
    service.append(AffectiveInput.HeartRate(heartRate))
}
```

实时订阅：

```kotlin
val job = lifecycleScope.launch {
    service.realtimeEvents(sourceKey = "local").collect { event ->
        // AffectiveRealtimeEvent.BioData / AffectiveRealtimeEvent.AffectiveData
    }
}
```

## 注意事项

- `IAffectiveDataAnalysisService.getService(...)` 已不再作为服务发现入口使用；业务侧必须显式创建实现。
- `append(AffectiveInput)` 是推荐输入入口；旧的按 `IDataType` 分发方法仅用于兼容。
- 该模块是公共兼容边界，变更 public/protected API 时必须更新 `affective_sdk_api/api/affective_sdk_api.api` 并运行 `apiCheck`。
