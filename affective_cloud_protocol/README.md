# affective_cloud_protocol

## 功能

`affective_cloud_protocol` 负责把实时生物数据和情感数据编码成可传输的协议消息，供 WebSocket、WebView、调试工具或外部 UI 展示使用。

主要类型：

- `RealtimeProtocolEncoder`：实时协议编码接口。
- `GsonRealtimeProtocolEncoder`：输出带 `version`、`type`、`timestampMillis`、`payload` 的结构化 JSON。
- `LegacyCloudProtocolEncoder`：兼容旧 Web 展示协议的 JSON 结构。
- `BioDataPayload` / `AffectiveDataPayload`：类型化 payload。
- `RealtimeTransportEnvelope`：传输包结构。

## 版本

- groupId: `cn.entertech.android`
- artifactId: `affective-cloud-protocol`
- version: 来自根目录 [`version.gradle`](../version.gradle) 的 `affectiveCloudProtocolVersion`

## 使用方式

源码工程依赖：

```groovy
dependencies {
    implementation project(':affective_sdk_api')
    implementation project(':affective_cloud_protocol')
}
```

示例：

```kotlin
val encoder: RealtimeProtocolEncoder = GsonRealtimeProtocolEncoder()

val bioJson = encoder.encodeBioData(realtimeBioData)
val affectiveJson = encoder.encodeAffectiveData(realtimeAffectiveData)
```

需要兼容旧展示端时：

```kotlin
val legacyEncoder = LegacyCloudProtocolEncoder()
```

## 注意事项

- 协议模块只负责编码，不负责网络连接、重试或会话管理。
- 新展示端优先使用 `GsonRealtimeProtocolEncoder`。
- 旧展示端如依赖历史字段结构，可继续使用 `LegacyCloudProtocolEncoder`。
