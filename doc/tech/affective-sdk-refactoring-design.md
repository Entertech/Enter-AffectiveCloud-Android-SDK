# Enter AffectiveCloud Android SDK 重构设计

> 状态：已确认
>
> 日期：2026-07-10
>
> 目标版本：2.x 渐进式重构，3.0.0 才允许删除旧 API

## 1. 背景

当前工程由多个可以被 App 或 Demo 直接接入的组件组成：

- affective_local_sdk：本地算法实现。
- affective_cloud_sdk：云端算法实现。
- affective_sdk_manager：管理多个算法实例同时运行。
- affective_cloud_protocol：将实时数据转换为传输协议，供 App 内 Web 服务对外展示实时数据。
- affective_data_provider：提取、过滤和缓存展示数据。
- affective_sdk_api：上述组件共同依赖的公共契约、配置、实时数据和报表模型。

现有工程已经完成统一版本目录、基础 convention plugin、Android 16 KB native 对齐和父仓库/算法子模块联动发布，但代码职责仍存在以下问题：

- affective_sdk_api 的公开接口较多，缺少自动化二进制兼容基线。
- AffectiveManager 使用多组平行 Map 保存同一个算法实例的服务、监听器和状态，容易出现状态不同步。
- EnterAffectiveLocalService 同时承担生命周期、输入缓存、JNI、订阅、结果转换和报表组装。
- EnterAffectiveSDKManager 同时承担授权、线程、缓存阈值和 JNI 调用。
- Protocol 的新旧职责没有显式边界，传输 payload 主要依赖 Map<Any, Any>。
- Data Provider 的数据提取、过滤、窗口限制和状态持有耦合在继承体系中。
- 各组件缺少与自身职责匹配的单元测试和契约测试。

本设计参考 Enter-Glass-Android 的能力边界、统一构建约定、依赖方向检查和 Kotlin-first 异步模型，但不会照搬其 App 业务分层，也不会引入所有组件必须经过的统一 Runtime。

## 2. 已确认的约束

### 2.1 API 兼容范围

只对 affective_sdk_api 承诺源码兼容和二进制兼容。

其他组件允许重构公开实现类型和内部结构，但应尽量保留薄兼容外壳，降低现有使用方的迁移成本。

### 2.2 Cloud 冻结范围

本轮不重构 affective_cloud_sdk 核心业务实现：

- 不修改 Cloud WebSocket、协议、会话和报表等核心实现。
- 不修改包名和发布坐标。
- 不为 API 入口补充服务发现或构造逻辑。
- 不要求它实现新算法接口。
- 不通过修改共享 convention plugin 间接改变它的构建行为。

允许在模块外建立黑盒契约测试，验证 Cloud 仍能通过现有 affective_sdk_api 被直接使用或被 Manager 管理。

### 2.3 组件接入方式

App 或 Demo 可以直接依赖五个能力组件。Manager 只负责多算法运行场景，不是 Local、Cloud、Protocol 或 Data Provider 的统一入口。

## 3. 目标架构

~~~mermaid
flowchart TD
    App["App / Demo<br/>组合根"] --> Local["affective_local_sdk<br/>本地算法"]
    App --> Cloud["affective_cloud_sdk<br/>云端算法"]
    App --> Manager["affective_sdk_manager<br/>多算法并行管理"]
    App --> Protocol["affective_cloud_protocol<br/>实时数据传输协议"]
    App --> Provider["affective_data_provider<br/>展示数据控制"]

    Local --> API["affective_sdk_api<br/>稳定公共契约"]
    Cloud --> API
    Manager --> API
    Protocol --> API
    Provider --> API

    Cloud --> Protocol
~~~

目标架构采用“稳定 API + 平级能力组件 + App 组合根”：

- Local 和 Cloud 是两种可独立使用的算法实现。
- Manager 按 Key 管理多个算法实例，可以同时运行 Local 和 Cloud。
- Protocol 将公共实时数据模型转换成传输消息，不持有 Web 服务。
- Data Provider 将公共实时数据转换成 UI 可观察的展示状态，不依赖 UI 框架。
- App 负责把算法输出组合到 Protocol、Data Provider 或其他业务。

## 4. 模块依赖规则

允许的模块依赖为：

~~~text
demo -> affective_local_sdk
demo -> affective_cloud_sdk
demo -> affective_sdk_manager
demo -> affective_cloud_protocol
demo -> affective_data_provider
demo -> affective_sdk_api

affective_local_sdk -> affective_sdk_api
affective_sdk_manager -> affective_sdk_api
affective_cloud_protocol -> affective_sdk_api
affective_data_provider -> affective_sdk_api
affective_cloud_sdk -> affective_sdk_api
affective_cloud_sdk -> affective_cloud_protocol
~~~

除现有 Cloud 到 Protocol 的依赖外，不允许新增横向依赖：

- Manager 不依赖 Local 或 Cloud 的具体实现。
- Data Provider 不依赖 Manager、Protocol、Local 或 Cloud。
- Protocol 不依赖 Manager、Data Provider、Local 或 UI。
- Local 和 Cloud 互不依赖。
- affective_sdk_api 不反向依赖任何实现组件。

在 build-logic 中新增模块依赖分析任务，对不允许的边、反向依赖和循环依赖直接报错。

## 5. affective_sdk_api 兼容设计

### 5.1 兼容基线

兼容范围覆盖 affective_sdk_api 中全部 public/protected 类型和成员，包括 Kotlin 编译器生成的公开成员。

以下内容不得修改：

- 包名、类名、父类和已有接口。
- 构造器、方法和字段的 JVM 描述符。
- 参数名称、顺序、类型、可空性和默认值。
- 属性的 val/var、getter、setter 和可见性。
- JvmStatic、JvmOverloads、const val 和 Companion 结构。
- 枚举名称、已有枚举项和序列化值。
- 公开 data class 的主构造参数顺序、componentN 和 copy 结构。
- 已有接口的抽象方法集合。

不得向 IAffectiveDataAnalysisService 增加新的抽象方法。需要的新能力通过新类型提供。

### 5.2 自动 ABI 检查

当前工程使用 Kotlin 1.9，先采用 JetBrains Binary Compatibility Validator 0.18.1：

- 只为 affective_sdk_api 生成 ABI dump。
- 将 dump 提交到版本库。
- CI 执行 apiCheck。
- 修改或删除旧签名时直接失败。
- 新增 API 时必须检查 dump diff，确认只包含预期新增。

升级到 Kotlin 2.2 或更高版本后，再迁移到 Kotlin Gradle Plugin 内置的 checkKotlinAbi。

### 5.3 源码和运行时兼容测试

ABI dump 之外增加三个最小消费工程：

- legacy-kotlin-consumer：验证 Kotlin 命名参数、默认参数、扩展方法和属性调用。
- legacy-java-consumer：验证 Java 构造器、静态入口和 getter/setter。
- legacy-binary-consumer：使用旧版 API 编译，再替换成新版 AAR 运行，验证没有 NoSuchMethodError、AbstractMethodError 或类加载错误。

另外增加行为基线测试：

- 数据类默认值。
- 枚举名称和序列化值。
- Gson 字段名。
- Local/Cloud 直接构造或业务 DI 管理实例。
- 公开常量值。

### 5.4 公共依赖

凡是出现在 affective_sdk_api 公开签名中的外部类型，其依赖必须使用 api 暴露。

当前 IAffectiveDataAnalysisService 使用 IDeviceType，因此 device 依赖应由 implementation 调整为 api。log-api 已经使用 api，继续保持。

### 5.5 新 API 策略

第一批重构只建立兼容基线，并保持 `IAffectiveDataAnalysisService` 作为业务统一入口。

业务可以直接依赖 `affective_local_sdk` 或 `affective_cloud_sdk`，运行时选择由业务层显式创建 `EnterAffectiveLocalService` / `EnterAffectiveCloudService`，或交给业务自己的 DI 容器管理。`IAffectiveDataAnalysisService` 只限定公开方法，不负责服务发现或实例构造。

在不移除现有方法的前提下，以新增方式补充：

- AffectiveInput：EEG、SCEEG、HR、PEPR、BCG、Gyro 等类型化输入。
- AffectiveRealtimeEvent：统一实时数据事件。
- AffectiveServiceResult：操作成功或类型化失败。
- `IAffectiveDataAnalysisService` 扩展方法：提供 `suspend`、`Flow` 和类型化 `append(AffectiveInput)` 调用。

旧 callback API 继续作为 Java 兼容入口。命名错误或职责不清的方法可以标记 `DeprecationLevel.WARNING`，但不能破坏源码与二进制兼容。

### 5.6 过时与删除策略

只有替代 API 功能完整、测试通过并提供迁移示例后，旧 API 才能标记过时。

规则如下：

- 2.x 仅使用 DeprecationLevel.WARNING。
- 每个过时 API 提供明确 message 和 ReplaceWith。
- 不使用 ERROR 或 HIDDEN。
- 不改变旧 API 的行为，旧入口仅委托给新实现。
- 旧 API 保留整个 2.x 生命周期。
- 最早在 3.0.0 删除。
- IAffectiveDataAnalysisService 在 Cloud 完成新 API 适配前不整体标记过时。
- 拼写错误方法可以先增加正确命名的新方法，旧方法继续委托。

## 6. affective_sdk_manager 重构设计

### 6.1 当前问题

AffectiveManager 当前使用多组 Map 分别保存：

- 算法 Service。
- 连接监听器。
- 启动监听器。
- 结束监听器。
- 报表监听器。
- Bio/Affective 实时监听器。
- 开始和结束时间。

这些状态属于同一个 Key，却由不同集合维护，容易产生部分注册、部分清理和状态漂移。当前阶段先保留直接 Map 管理，避免引入只有监听器缓存作用的过度抽象。

现有 suspendCoroutine 桥接还存在重复回调、取消后仍回调和无法注销监听器的风险。

### 6.2 当前模型

每个 Key 对应一个显式注册的 `IAffectiveDataAnalysisService`：

~~~text
AffectiveManager
├── ConcurrentHashMap<Key, IAffectiveDataAnalysisService>
├── ConcurrentHashMap<Key, IConnectionServiceListener>
├── ConcurrentHashMap<Key, IStartAffectiveServiceLister>
├── ConcurrentHashMap<Key, IGetReportListener>
├── ConcurrentHashMap<Key, IFinishAffectiveServiceListener>
└── ConcurrentHashMap<Key, realtime callback>
~~~

不再保留独立 Registry/Slot 抽象。只有当单个 Key 需要承载明确状态机、资源 owner 或并发控制时，再引入聚合对象。

### 6.3 新职责拆分

- AffectiveManager：注册、查找和移除算法实例。
- 输入分发：向一个、多个或全部算法分发输入。
- 事件转发：将实时数据和错误转换成带 Key 的事件。
- 报表转发：按 Key 获取报表。

### 6.4 多算法行为

- 支持显式 register(key, service)。
- 不在 API 层提供默认服务发现。
- 支持 append(key, input) 和 appendAll(input)。
- 一个算法连接或计算失败不终止其他算法。
- 所有结果和错误携带 Key。
- 同一 Key 的生命周期操作串行，不同 Key 可以并行。
- 重复 connect/start/finish 必须幂等。
- finish 或 close 完成后才移除对应 service。

### 6.5 异步规则

- 新内部代码使用结构化协程。
- callback 到 suspend 使用 suspendCancellableCoroutine。
- 监听器只能完成 continuation 一次。
- 协程取消时注销监听器。
- CancellationException 必须继续抛出。
- 后续如引入 per-key 状态机，再使用 Mutex 保证同一 Key 的生命周期转换顺序。

## 7. affective_local_sdk 重构设计

### 7.1 当前问题

EnterAffectiveLocalService 同时负责：

- 会话连接和状态。
- 授权参数。
- 输入数据路由。
- 数据缓存。
- JNI 调用。
- 实时结果组装。
- 订阅监听器。
- 报表转换。

EnterAffectiveSDKManager 同时负责：

- HandlerThread。
- 授权校验。
- JNI 实例。
- 多种数据缓存。
- 触发阈值。
- JNI 回调。

这使得单元测试必须依赖 Android 线程或 JNI，生命周期释放也缺少明确 owner。

### 7.2 目标拆分

- EnterAffectiveLocalService：旧 IAffectiveDataAnalysisService 兼容外壳。
- LocalAlgorithmSession：本地算法会话和生命周期状态。
- NativeEngine：本地算法引擎接口，测试可替换为 Fake。
- JniNativeEngine：唯一允许直接访问 JNI 的实现。
- NativeThreadOwner：持有 HandlerThread、Handler 和释放逻辑。
- LocalAuthenticationPolicy：auth/noAuth flavor 行为。
- LocalInputRouter：根据 AffectiveInput 或 IDataType 分发数据。
- EegInputBuffer、HrInputBuffer、PeprInputBuffer：独立缓存和阈值。
- NativeRealtimeResultMapper：JNI 实时结果转公共模型。
- LocalReportAssembler：JNI 报表转 UploadReportEntity。
- LocalSubscriptionRegistry：监听器注册、去重和释放。

### 7.3 生命周期

本地算法使用以下内部状态：

~~~text
Idle -> Connected -> Starting -> Running -> Finishing -> Connected -> Closed
                         \-> Failed
~~~

要求：

- 连接、启动、结束和关闭幂等。
- JNI 初始化失败后可安全重试。
- release 只能执行一次，但重复调用不报错。
- HandlerThread、JNI 资源和监听器由同一个 Session owner 释放。
- 关闭后不再发送实时事件。

### 7.4 Native 与发布约束

- 保留现有 project/authentication product flavor。
- 保留动态 artifactId 规则。
- 父仓库继续记录算法子模块精确 SHA。
- SO 必须由记录的子模块 SHA 构建。
- 保留 Android 16 KB LOAD segment 和 AAR 重复 SO 检查。
- Native 改动必须同时验证 auth/noAuth 和全部发布 flavor。

## 8. affective_cloud_sdk 冻结设计

本轮不进行内部重构。

Manager 继续通过 IAffectiveDataAnalysisService 或显式注入管理 Cloud。新 Manager 可以在自身内部把旧 Service 适配为新 Slot，不需要 Cloud 修改。

允许新增的外部验证：

- Cloud 可以由业务直接创建。
- Cloud 可以被 Demo 直接创建。
- Cloud 可以被 Manager 作为一个 Key 管理。
- Cloud 的旧实时数据可以被 Protocol 编码。
- Cloud AAR 与当前 affective_sdk_api 兼容。

Cloud 后续解冻时再执行：

1. 建立 Cloud 黑盒行为基线。
2. 拆分 WebSocket、协议、会话和报表职责。
3. 原生适配 `IAffectiveDataAnalysisService` 的现代扩展能力。
4. 与旧 callback 方法双轨运行。
5. 完成迁移后再考虑过时旧 callback 方法；`IAffectiveDataAnalysisService` 本身继续作为业务入口。

## 9. affective_cloud_protocol 重构设计

### 9.1 职责边界

Protocol 只负责：

- 实时传输数据模型。
- 公共实时数据到传输 payload 的映射。
- 编码和解码。
- 协议版本兼容。

Protocol 不负责：

- 启动或停止 Web 服务。
- 管理 WebSocket 客户端连接。
- 算法生命周期。
- 展示缓存。

### 9.2 兼容与新模型

现有 Request、RequestBody、AffectiveCloudResponseBody 保留，避免影响冻结的 Cloud。

新增：

- RealtimeTransportEnvelope。
- BioDataPayload。
- AffectiveDataPayload。
- RealtimeProtocolEncoder。
- LegacyCloudProtocolEncoder。
- ProtocolVersion。

新代码使用类型化 payload，Map<Any, Any> 只留在旧兼容路径。

现有 clound 拼写、包名和常量不进行批量重命名；新类型放入正确命名的新包，通过 Mapper 与旧模型连接。

### 9.3 协议测试

- 固定 RealtimeBioData 输入对应固定 legacy JSON。
- 固定 RealtimeAffectiveData 输入对应固定 legacy JSON。
- 字段缺失、null 和新增未知字段。
- 协议版本兼容。
- 字段顺序不作为语义要求，但字段名和值必须稳定。

## 10. affective_data_provider 重构设计

### 10.1 定位

Data Provider 是展示数据控制组件，负责将算法实时结果变成 UI 可观察状态。它不属于算法运行层，也不依赖具体 UI 技术。

### 10.2 目标流水线

~~~text
RealtimeBioData / RealtimeAffectiveData
        |
        v
DataSelector<Input, Point>
        |
        v
DataFilter<Point>
        |
        v
WindowPolicy<Point>
        |
        v
DisplayDataProvider<Point>
        |
        v
StateFlow<List<Point>>
~~~

职责拆分：

- DataSelector：选择 Attention、HR、Relaxation 等目标字段。
- DataFilter：过滤 0、非法质量和业务无效值。
- WindowPolicy：限制最大缓存、滑动窗口和采样策略。
- DisplayDataProvider：持有唯一可变状态，对外只暴露只读 StateFlow。

### 10.3 具体 Provider

AttentionListCacheDataProvider、HrListCacheDataProvider、RelaxationListCacheDataProvider 保留为薄配置类。

所有输入路径必须使用同一窗口限制逻辑。当前 affectiveData 路径未统一调用 limitData 的行为需要通过失败测试先固定，再修复。

新增：

- clear/reset。
- 当前窗口大小。
- 可配置最大缓存。
- 不可变列表快照。
- 并发更新安全。

移除不需要的 AppCompat、Material 等 UI 依赖。

## 11. Demo 设计

Demo 是组件组合根，直接依赖：

- affective_local_sdk。
- affective_cloud_sdk。
- affective_sdk_manager。
- affective_cloud_protocol。
- affective_data_provider。
- affective_sdk_api。

提供五条独立示例：

### 11.1 Local Direct

直接创建本地算法、输入设备数据、订阅实时结果和获取报表。

### 11.2 Cloud Direct

直接创建云端算法、建立 WebSocket 会话、输入设备数据和获取报表。

### 11.3 Multi Algorithm

Manager 使用两个不同 Key 同时管理 Local 和 Cloud，同一份原始数据分发给两个算法，并分别展示状态、实时结果和错误。

### 11.4 Web Realtime Transport

算法实时结果经 Protocol 编码后，由 Demo 持有的 Web 服务向浏览器或远端页面发送。

### 11.5 Display Provider

算法实时结果进入 Data Provider，UI 通过 StateFlow 展示 Attention、Relaxation 和 HR 曲线。

Demo 不访问组件内部实现、不直接调用 JNI、不自行复制协议转换和窗口缓存逻辑。

## 12. Build Logic 设计

保留现有 enter.android.library，不直接修改其行为，以免间接影响冻结的 Cloud。

新增 opt-in convention plugin：

- enter.android.sdk-component：普通可发布 SDK 组件。
- enter.android.native-component：Local native 组件。
- enter.android.api-compatibility：仅 affective_sdk_api 使用。

统一配置：

- compileSdk、minSdk 和 Java/Kotlin target。
- consumer ProGuard 文件。
- 测试依赖。
- PublishInfo 基础校验。
- Sources/Javadoc 制品策略。
- lint 和单元测试任务。

注意：

- affective_sdk_api 在 2.x 保持当前 Kotlin/JVM 编译边界。
- Manager 当前 Java 17 与其他组件 Java 8 的差异不能在同一次 API 重构中强制统一。
- Version Catalog 继续作为依赖版本来源。
- Cloud 使用的依赖兼容版本暂不随全局清理升级。
- 2.x 保持统一 libraryVersion，不引入组件独立版本。

## 13. 显式实例与混淆

Local 和 Cloud 都提供 `IAffectiveDataAnalysisService` 实现类，由业务直接创建或纳入业务自己的 DI 容器。

重构必须增加：

- 未混淆 Demo 的直接实例化测试。
- 开启 R8 的 release Demo smoke test。
- Local consumer ProGuard 规则。
- Cloud 冻结期间不修改其 consumer rules，必要保护规则暂时放在 Demo/消费者侧验证。
- AAR/编译检查，确认 Local/Cloud 实现类可被消费者直接引用。

## 14. 实施阶段

### P0：兼容与行为基线

- 生成 affective_sdk_api ABI dump。
- 增加 Kotlin、Java 和旧二进制消费测试。
- 固定公共模型、JSON、组件发现和默认值行为。
- 不改业务实现。

### P1：构建和依赖门禁

- 增加 opt-in convention plugin。
- 增加 analyzeModuleDependencies。
- 增加 API、AAR、POM 和最小消费者检查。
- Cloud 继续使用原插件配置。

### P2：Manager 重构

- 先使用 Fake Local/Fake Cloud 建立多算法测试。
- 使用显式注册的 service map 管理多算法实例。
- 迁移输入分发、订阅、报表和释放。
- 旧方法委托新实现。

### P3：Local 重构

- 抽出 NativeEngine 和线程 owner。
- 拆分输入缓存、授权、结果 Mapper、报表和订阅。
- 使用 Fake NativeEngine 完成 JVM 测试。
- 使用真实测试向量验证 JNI。

### P4：Data Provider 和 Protocol

- Data Provider 重构为 selector/filter/window/state 流水线。
- Protocol 新增类型化模型和 Encoder。
- 旧 Cloud JSON 和旧模型保持不变。

### P5：Demo 组合验证

- 增加五种接入示例。
- 验证 Local/Cloud 直接使用。
- 验证 Manager 多算法。
- 验证 Web 实时传输和 Data Provider 展示。
- 验证 R8 后 Local/Cloud 直接实例化。

### P6：公共 API 整理和过时周期

- 整理 `IAffectiveDataAnalysisService` 现有方法，不移除公开接口。
- 新增 `AffectiveInput`、`AffectiveRealtimeEvent`、`AffectiveServiceResult` 以及 `IAffectiveDataAnalysisService` 现代扩展方法。
- Local/Cloud 提供 `IAffectiveDataAnalysisService` 实现，但不在 API 层注册或发现。
- 旧入口增加 WARNING 级别过时注解。
- Cloud 内部逻辑继续保持冻结。

### P7：Cloud 后续重构

- Cloud 解冻后再单独形成设计和实施计划。
- 完成新 API 适配后，才能整体过时 IAffectiveDataAnalysisService。
- 3.0.0 才执行旧 API 删除。

### Implementation Status（实施状态）

| 阶段 | 状态 | 已落地内容 | 验证入口 |
| --- | --- | --- | --- |
| P0 | 已完成 | `affective_sdk_api/api/affective_sdk_api.api`、`compatibility-tests/legacy-consumer` 建立 ABI、Kotlin 和 Java 源码兼容基线 | `./gradlew apiCheck :compatibility-tests:legacy-consumer:assembleDebug` |
| P1 | 已完成 | `ModuleDependencyAnalysisPlugin`、允许依赖边和根工程 API validation 已接入 | `./gradlew analyzeModuleDependencies apiCheck` |
| P2 | 已完成 | Manager 使用显式 service map 管理每个 Key，支持显式注册、全算法分发、失败隔离和完成后清理 | `./gradlew :affective_sdk_manager:test` |
| P3 | 已完成 | Local 子模块增加生命周期、订阅、批缓存和 `NativeEngine` seam，并保留全部发布 flavor | `./gradlew :affective_local_sdk:test :affective_local_sdk:assembleRelease`、`scripts/verify_android_16k.sh` |
| P4 | 已完成 | Data Provider 增加 selector/filter/window 组合管线；Protocol 增加版本化类型模型和 legacy golden encoder | `./gradlew :affective_data_provider:test :affective_cloud_protocol:test` |
| P5 | 已完成 | Demo 直接依赖五个能力组件，`AffectiveDemoPipeline` 同时路由实时传输和展示数据 | `./gradlew :demo:test :demo:assembleDebug` |
| P6 | 已完成 | 保留 `IAffectiveDataAnalysisService` 作为统一入口，新增 `AffectiveInput`、`AffectiveRealtimeEvent`、`AffectiveServiceResult` 和直接扩展方法；移除未采用的中间抽象 | `./gradlew :affective_sdk_api:testDebugUnitTest apiCheck :demo:assembleDebug` |
| P7 | 未开始 | `affective_cloud_sdk` 内部继续冻结；后续重构单独设计 | `git diff --exit-code HEAD -- affective_cloud_sdk/src/main/java/cn/entertech/affectivecloudsdk/EnterAffectiveCloudApiImpl.kt` |

发布门禁由 `scripts/verify_sdk_architecture.sh`、`scripts/verify_service_loader_artifacts.sh` 和现有 `scripts/verify_android_16k.sh` 共同执行，并已接入发布 PR 与正式发布工作流。

## 15. 测试矩阵

| 范围 | 必须覆盖 |
| --- | --- |
| affective_sdk_api | ABI、Kotlin 源码、Java 源码、旧二进制运行、默认值、枚举、JSON |
| Manager | Local+Cloud 并行、单实例失败隔离、幂等、取消、重复回调、报表、释放 |
| Local | auth/noAuth、全部 project flavor、缓存阈值、JNI 映射、报表、线程释放 |
| Protocol | legacy JSON golden、新协议版本、null、未知字段 |
| Data Provider | selector、filter、窗口上限、两类实时输入、并发更新、clear |
| Demo | 五组件直接依赖、多算法、Web 展示、StateFlow 展示、R8 |
| 架构 | 允许依赖边、无循环、无新增横向依赖 |
| Native | 子模块 SHA、SO 清单、16 KB、AAR 重复 SO |
| 发布 | AAR、POM、Gradle metadata、版本、依赖 scope、最小消费者 |

## 16. 发布策略

继续使用现有发布链路：

- publish-pr-gate.yml：发布 PR 分支、版本和子模块检查。
- release-publish.yml：构建五个发布组件、调用 Local 算法子仓库构建 SO、本地 PublishPlugin 发布、收集 AAR 和打标签。
- release-aftercare.yml：主分支合并后的分支清理。

当前发布清单已经包含：

- affective_sdk_api。
- affective_cloud_protocol。
- affective_data_provider。
- affective_sdk_manager。
- affective_local_sdk。

当前发布清单不包含 affective_cloud_sdk，符合本轮 Cloud 冻结要求。

在 assemble/publish 之前新增：

1. API ABI 检查。
2. Kotlin/Java/旧二进制消费检查。
3. 模块依赖分析。
4. 各组件单元测试。
5. R8 直接实例化 smoke test。
6. AAR/POM/metadata 检查。
7. Native SO 和 16 KB 检查。

版本建议：

- 内部兼容重构：2.0.x。
- 新 API 和过时注解：2.1.0。
- 删除旧 API：3.0.0。

已发布版本不得覆盖。失败后使用新的 patch 或预发布版本。

## 17. 风险与控制

### 17.1 行为兼容风险

重构可能不改变签名但改变 callback 时序、默认值或错误信息。

控制方式：重构前建立行为契约测试，旧外壳委托新实现时逐项对比。

### 17.2 Manager 并发风险

多个算法同时运行时可能发生重复启动、回调串 Key 或提前清理。

控制方式：每个 Key 使用独立 service 和监听器缓存，以带 Key 的事件进行断言；如果后续引入状态机，再补充 per-key Mutex。

### 17.3 JNI 生命周期风险

HandlerThread、JNI 单例和缓存当前所有权不清晰。

控制方式：NativeThreadOwner 和 LocalAlgorithmSession 成为唯一资源 owner，测试重复 release。

### 17.4 Cloud 间接变化风险

修改共享 build plugin 或 Protocol 旧类型可能间接影响冻结的 Cloud。

控制方式：新 convention plugin 采用 opt-in；Protocol 旧类型只做兼容保留；验收要求 Cloud 目录无差异。

### 17.5 显式实例与 R8 风险

实现类可能在 release 构建中被裁剪。

控制方式：consumer rules、消费者编译检查和混淆后的 Demo smoke test。

### 17.6 子模块工作区风险

当前 affective_local_sdk/src/main 子模块存在本地未提交状态。

控制方式：进入 Local 实施前先识别并保留现有改动，不覆盖、不重置子模块。

## 18. 完成标准

全部满足以下条件才认为重构完成：

- 旧版 Kotlin 和 Java 示例无需修改即可编译。
- 使用旧 affective_sdk_api 编译的消费者替换新版 AAR 后可以运行。
- affective_sdk_api ABI 没有删除或修改。
- Demo 可以直接依赖并使用五个能力组件。
- Local 和 Cloud 均可独立运行。
- Manager 可以同时运行 Local 和 Cloud，失败相互隔离。
- Protocol 旧 JSON golden tests 全部通过。
- Data Provider 缓存始终不超过配置上限。
- Local 的全部发布 flavor、JNI 测试和 16 KB 检查通过。
- 混淆后的 Demo 可以直接创建算法实现。
- affective_cloud_sdk 核心业务实现没有被重构。
- 发布产物可以追溯到父仓库提交和 Local 子模块精确 SHA。
- 本地 Maven 消费测试通过后才允许远端发布。

## 19. 参考

- Enter-Glass-Android：https://github.com/Entertech/Enter-Glass-Android
- Kotlin backward compatibility guidelines：https://kotlinlang.org/docs/api-guidelines-backward-compatibility.html
- Kotlin binary compatibility validator：https://github.com/Kotlin/binary-compatibility-validator
- Kotlin Gradle ABI validation：https://kotlinlang.org/docs/gradle-binary-compatibility-validation.html
