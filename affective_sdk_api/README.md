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

## 完整流程

```kotlin
val service: IAffectiveDataAnalysisService = EnterAffectiveLocalService()

lifecycleScope.launch {
    val connected = service.awaitConnectAffectiveService(EnterAffectiveConfigProxy())
    if (connected is AffectiveServiceResult.Failure) return@launch

    val started = service.awaitStartAffectiveService()
    if (started is AffectiveServiceResult.Failure) return@launch

    val eventsJob = launch {
        service.realtimeEvents(sourceKey = service.getAffectiveWay().name).collect { event ->
            when (event) {
                is AffectiveRealtimeEvent.BioData -> {
                    val bioData = event.data
                    // bioData?.realtimeEEGData / realtimeHrData / realtimeSCEEGData / realtimePEPRData
                }
                is AffectiveRealtimeEvent.AffectiveData -> {
                    val affectiveData = event.data
                    // affectiveData?.attention / relaxation / pleasure / sleep / flow
                }
            }
        }
    }

    service.append(AffectiveInput.EegBytes(bytes))
    service.append(AffectiveInput.HeartRate(heartRate))

    val report = service.awaitGetReport(isEar = false, needFinishService = false)
    if (report is AffectiveServiceResult.Success) {
        val uploadReport = report.value
        // uploadReport?.biodata / affective
    }

    service.awaitFinishAffectiveService()
    eventsJob.cancel()
    service.closeAffectiveServiceConnection()
}
```

可变长度双通道 EEG 包先转换成兼容的 20 字节包，再复用现有追加入口：

```kotlin
val variableEegAdapter = VariableEegPacketAdapter()
service.appendVariableEEGPacket(packet, variableEegAdapter)
```

实时订阅：

```kotlin
val job = lifecycleScope.launch {
    service.realtimeEvents(sourceKey = "local").collect { event ->
        // AffectiveRealtimeEvent.BioData / AffectiveRealtimeEvent.AffectiveData
    }
}
```

## API 流程

### 1. 创建实现

`affective_sdk_api` 只定义契约，不负责创建实现。业务侧需要显式依赖并创建本地或云端 service：

```kotlin
val localService: IAffectiveDataAnalysisService = EnterAffectiveLocalService()
val cloudService: IAffectiveDataAnalysisService = EnterAffectiveCloudService()
```

### 2. 连接

推荐 Kotlin 协程入口：

```kotlin
val result = service.awaitConnectAffectiveService(EnterAffectiveConfigProxy())
```

回调入口：

```kotlin
service.connectAffectiveServiceConnection(listener, EnterAffectiveConfigProxy())
```

连接状态相关 API：

```kotlin
service.hasConnectAffectiveService()
service.getSessionId()
service.closeAffectiveServiceConnection()
```

### 3. 启动

无鉴权启动：

```kotlin
service.awaitStartAffectiveService()
```

需要鉴权文件时：

```kotlin
service.awaitStartAffectiveService(authenticationInputStream, context)
```

回调入口：

```kotlin
service.startAffectiveService(listener)
service.startAffectiveService(authenticationInputStream, context, listener)
```

其他生命周期 API：

```kotlin
service.hasStartAffectiveService()
service.restoreAffectiveService(listener)
service.checkEnvironment(context)
```

### 4. 订阅实时数据

推荐 Flow：

```kotlin
val job = lifecycleScope.launch {
    service.realtimeEvents(sourceKey = "local").collect { event ->
        when (event) {
            is AffectiveRealtimeEvent.BioData -> Unit
            is AffectiveRealtimeEvent.AffectiveData -> Unit
        }
    }
}
```

回调入口：

```kotlin
service.subscribeData(
    bdListener = { bioData -> },
    listener = { affectiveData -> },
)

service.unSubscribeData(
    bdListener = bioListener,
    listener = affectiveListener,
)
```

### 5. 追加硬件数据

推荐使用类型化输入：

```kotlin
service.append(AffectiveInput.EegBytes(eegBytes, isEar = false))
service.append(AffectiveInput.EegSample(eegValue))
service.append(AffectiveInput.EegSamples(eegValues))

service.append(AffectiveInput.SceegBytes(sceegBytes, isEar = false))
service.append(AffectiveInput.SceegSample(sceegValue))
service.append(AffectiveInput.SceegSamples(sceegValues))

service.append(AffectiveInput.HeartRate(heartRate))
service.append(AffectiveInput.Pepr(peprBytes))
service.append(AffectiveInput.Bcg(bcgBytes))
service.append(AffectiveInput.Gyro(gyroBytes))
service.append(AffectiveInput.DcEeg(dcEegBytes))
service.append(AffectiveInput.McEeg(mcEegBytes))
```

底层 typed append 方法仍可直接调用：

```kotlin
service.appendEEGData(eegBytes)
service.appendEEGData(eegValue)
service.appendEEGData(eegValues)
service.appendSCEEGData(sceegBytes)
service.appendHeartRateData(heartRate)
service.appendPEPRData(peprBytes)
service.appendBCGData(bcgBytes)
service.appendGyroData(gyroBytes)
service.appendDCEEGData(dcEegBytes)
service.appendMCEEGData(mcEegBytes)
```

可变长度双通道 EEG 包：

```kotlin
val adapter = VariableEegPacketAdapter()
service.appendVariableEEGPacket(packet, adapter, isEar = false)
```

同一条设备数据流需要复用同一个 `VariableEegPacketAdapter`，它会缓存不足一个 20 字节兼容包的采样。

设备佩戴位置和质量判断：

```kotlin
service.appendDevicePosition(device, position)
val good = service.isGoodQuality(quality)
```

### 6. 获取报表和结束

协程入口：

```kotlin
val report = service.awaitGetReport(isEar = false, needFinishService = false)
service.awaitFinishAffectiveService()
service.closeAffectiveServiceConnection()
```

回调入口：

```kotlin
service.getReport(listener, isEar = false, needFinishService = false)
service.finishAffectiveService(listener)
```

`needFinishService = true` 表示获取报表时由实现自动结束服务；否则需要业务侧显式调用结束接口。

## 注意事项

- `IAffectiveDataAnalysisService.getService(...)` 已不再作为服务发现入口使用；业务侧必须显式创建实现。
- `append(AffectiveInput)` 是推荐输入入口；旧的按 `IDataType` 分发方法仅用于兼容。
- 该模块是公共兼容边界，变更 public/protected API 时必须更新 `affective_sdk_api/api/affective_sdk_api.api` 并运行 `apiCheck`。
