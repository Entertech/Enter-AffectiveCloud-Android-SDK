# affective_data_provider

## 功能

`affective_data_provider` 提供面向 UI 展示的数据处理工具，用于从实时算法数据中选择、过滤、缓存和裁剪展示数据。

主要能力：

- `IDataProvider`：展示数据提供者基础接口。
- `BaseETCacheDataProvider` / `BaseETCacheListDoubleDataProvider`：缓存型数据提供者基类。
- `AttentionListCacheDataProvider`、`RelaxationListCacheDataProvider`、`HrListCacheDataProvider`：常用指标缓存。
- `DisplayDataProvider`：组合 selector、filter 和 window policy 的展示数据管线。
- `DataSelector` / `DataFilter` / `WindowPolicy` / `SlidingWindowPolicy`：可组合的数据处理策略。

## 版本

- groupId: `cn.entertech.android`
- artifactId: `affective-data-provider`
- version: 来自根目录 [`version.gradle`](../version.gradle) 的 `affectiveDataProviderVersion`

## 使用方式

源码工程依赖：

```groovy
dependencies {
    implementation project(':affective_sdk_api')
    implementation project(':affective_data_provider')
}
```

使用预置缓存：

```kotlin
val attentionProvider = AttentionListCacheDataProvider()
val hrProvider = HrListCacheDataProvider()
```

使用展示管线：

```kotlin
val provider = DisplayDataProvider(
    selector = DataSelector { data -> data },
    filter = DataFilter { value -> value != null },
    windowPolicy = SlidingWindowPolicy(maxSize = 120),
)
```

## 注意事项

- 本模块不负责算法计算，只处理已经产生的实时数据。
- UI 层应根据图表刷新频率选择合适的 window size，避免无限缓存。
- 需要自定义指标时，优先组合 `DataSelector`、`DataFilter` 和 `WindowPolicy`，避免复制缓存逻辑。
