# affective_local_sdk

## 功能

`affective_local_sdk` 是本地离线算法组件，提供 `IAffectiveDataAnalysisService` 的本地实现 `EnterAffectiveLocalService`。它可在无网络或低延迟场景下直接处理硬件输入数据，并输出实时生物数据、情感数据和报表。

本模块的核心算法源码位于 Git submodule：`affective_local_sdk/src/main`。

## 版本

- groupId: `cn.entertech.android`
- base artifactId: `affective-offline-sdk`
- version: 来自根目录 [`version.gradle`](../version.gradle) 的 `affectiveLocalSdkVersion`

本地算法按产品和鉴权拆分发布变体：

| project flavor | authentication flavor | artifactId 示例 |
| --- | --- | --- |
| `breath` | `auth` / `noAuth` | `breath-affective-offline-sdk-authentication` / `breath-affective-offline-sdk` |
| `obsession` | `auth` / `noAuth` | `obsession-affective-offline-sdk-authentication` / `obsession-affective-offline-sdk` |
| `flowtime` | `auth` / `noAuth` | `flowtime-affective-offline-sdk-authentication` / `flowtime-affective-offline-sdk` |
| `sdk` | `auth` / `noAuth` | `sdk-affective-offline-sdk-authentication` / `sdk-affective-offline-sdk` |

具体发布包坐标以交付文档为准。

## 使用方式

源码工程依赖：

```groovy
dependencies {
    implementation project(':affective_sdk_api')
    implementation project(':affective_local_sdk')
}
```

创建本地服务：

```kotlin
val localService: IAffectiveDataAnalysisService = EnterAffectiveLocalService()
```

启动和追加数据：

```kotlin
lifecycleScope.launch {
    localService.awaitConnectAffectiveService(EnterAffectiveConfigProxy())
    localService.awaitStartAffectiveService()

    localService.append(AffectiveInput.EegBytes(bytes))
    localService.append(AffectiveInput.HeartRate(heartRate))
}
```

结束服务：

```kotlin
lifecycleScope.launch {
    localService.awaitFinishAffectiveService()
    localService.closeAffectiveServiceConnection()
}
```

## Submodule 注意事项

新设备 clone 后先初始化子仓库：

```bash
git submodule update --init --recursive affective_local_sdk/src/main
```

修改 `affective_local_sdk/src/main` 内文件时，需要先在子仓库提交，再回到父仓库提交 submodule 指针。

## 相关文档

- [native 算法 README](src/main/README.md)
- [CPP SDK README](src/main/cpp/README.md)
