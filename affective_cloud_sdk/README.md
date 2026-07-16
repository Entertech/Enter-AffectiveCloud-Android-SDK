# affective_cloud_sdk

`affective_cloud_sdk` 是回车情感云 Android 云端算法组件，提供 `IAffectiveDataAnalysisService` 的云端实现 `EnterAffectiveCloudService`，也保留既有的 `EnterAffectiveCloudManager` / `EnterAffectiveCloudApi` 兼容入口。

回车情感云可以根据脑波、心率等数据进行云端情绪情感分析，返回放松度、注意力、愉悦值、压力值、睡眠、心流等结果。正式接入前请先阅读[回车情感云开发文档](https://docs.affectivecloud.com)，确认需要开通的基础服务和情感服务，并联系管理员申请 `APP_KEY`、`APP_SECRET`。

## 版本

- groupId: `cn.entertech.android`
- artifactId: `affective_cloud_sdk`
- version: 来自根目录 [`version.gradle`](../version.gradle) 的 `affectiveCloudSdkVersion`

## 使用方式

源码工程接入时按需依赖：

```groovy
dependencies {
    implementation project(':affective_sdk_api')
    implementation project(':affective_cloud_sdk')
}
```

如果需要同时管理本地和云端算法，再加入：

```groovy
implementation project(':affective_sdk_manager')
```

发布包坐标、仓库地址和版本号以项目发布说明或交付文档为准。

## 权限

云端算法需要网络权限：

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

## 推荐入口

新业务建议直接创建 `EnterAffectiveCloudService`，并通过统一的 `IAffectiveDataAnalysisService` API 使用云端算法。

```kotlin
val cloudService: IAffectiveDataAnalysisService = EnterAffectiveCloudService()

val config = EnterAffectiveConfigProxy(
    availableBioDataCategories = listOf(BioDataCategory.EEG, BioDataCategory.HR),
    availableAffectiveDataCategories = listOf(
        PhysiologyDataCategory.ATTENTION,
        PhysiologyDataCategory.PRESSURE,
        PhysiologyDataCategory.AROUSAL,
        PhysiologyDataCategory.SLEEP,
    ),
    userId = USER_ID,
    appKey = APP_KEY,
    appSecret = APP_SECRET,
)

lifecycleScope.launch {
    when (val connected = cloudService.awaitConnectAffectiveService(config)) {
        is AffectiveServiceResult.Success -> Unit
        is AffectiveServiceResult.Failure -> return@launch
    }

    when (val started = cloudService.awaitStartAffectiveService()) {
        is AffectiveServiceResult.Success -> Unit
        is AffectiveServiceResult.Failure -> return@launch
    }

    cloudService.append(AffectiveInput.EegBytes(bytes))
    cloudService.append(AffectiveInput.HeartRate(heartRate))
}
```

实时数据可以使用 Flow 订阅：

```kotlin
val eventJob = lifecycleScope.launch {
    cloudService.realtimeEvents(sourceKey = "cloud").collect { event ->
        // 处理 AffectiveRealtimeEvent.BioData / AffectiveRealtimeEvent.AffectiveData
    }
}
```

获取报表和释放资源：

```kotlin
lifecycleScope.launch {
    val report = cloudService.awaitGetReport(isEar = false, needFinishService = false)

    eventJob.cancelAndJoin()
    cloudService.awaitFinishAffectiveService()
    cloudService.closeAffectiveServiceConnection()
}
```

## 与本地算法切换

`affective_cloud_sdk` 和 `affective_local_sdk` 都实现 `IAffectiveDataAnalysisService`。业务可以按网络状态、用户选择或产品策略显式创建不同实现：

```kotlin
fun createService(useCloud: Boolean): IAffectiveDataAnalysisService =
    if (useCloud) EnterAffectiveCloudService() else EnterAffectiveLocalService()
```

需要本地和云端同时运行时，请使用 `AffectiveManager` 显式注册服务实例：

```kotlin
val manager = AffectiveManager<String>()
manager.registerAffectiveService("cloud", EnterAffectiveCloudService())
```

`affective_sdk_api` 不再自动发现或创建云端实现，业务侧必须自己创建、注入或注册服务实例。

## 兼容入口

既有项目仍可使用 `EnterAffectiveCloudManager`：

```kotlin
val config = EnterAffectiveCloudConfig.Builder(APP_KEY, APP_SECRET, USER_ID)
    .url(websocketAddress)
    .availableBiodataServices(listOf(Service.EEG, Service.HR))
    .availableAffectiveServices(listOf(Service.ATTENTION, Service.PRESSURE, Service.SLEEP))
    .build()

val manager = EnterAffectiveCloudManager(config)
manager.init(callback)
manager.addBiodataRealtimeListener { data -> }
manager.addAffectiveDataRealtimeListener { data -> }
manager.appendEEGData(bytes)
manager.appendHeartRateData(heartRate)
manager.getBiodataReport(callback)
manager.getAffectiveDataReport(callback)
manager.release(callback)
```

兼容入口主要面向旧代码迁移；新代码优先使用 `EnterAffectiveCloudService` 和 `IAffectiveDataAnalysisService`。

## 原始数据

云端服务可通过 `StorageSettings` 控制是否保存当前会话的原始数据：

```kotlin
val storageSettings = StorageSettings.Builder()
    .allowStoreRawData(true)
    .build()

val config = EnterAffectiveCloudConfig.Builder(APP_KEY, APP_SECRET, USER_ID)
    .storageSettings(storageSettings)
    .build()
```

如需查询或删除云端原始数据，请使用 `AffectiveCloudSourceDataApi`，详细接口见[情感云详细 API 功能说明](../media/情感云详细API功能说明.md)。
