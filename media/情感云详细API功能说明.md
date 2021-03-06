[English Readme](detail_api.md)
# 情感云详细API功能说明
- [会话](#会话)
  * [SDK初始化及情感云连接](#sdk初始化及情感云连接)
  * [是否已连接情感云](#是否已连接情感云)
  * [建立会话](#建立会话)
  * [会话是否已建立](#会话是否已建立)
  * [Restore会话](#restore会话)
- [初始化服务](#初始化服务)
  * [初始化基础服务](#初始化基础服务)
  * [初始化情感服务](#初始化情感服务)
- [发送数据](#发送数据)
  * [发送脑波数据](#发送脑波数据)
  * [发送心率数据](#发送心率数据)
- [接受订阅数据](#接受订阅数据)
  * [订阅基础数据](#订阅基础数据)
  * [订阅情感数据](#订阅情感数据)
- [取消订阅](#取消订阅)
  * [取消订阅基础数据](#取消订阅基础数据)
  * [取消订阅情感数据](#取消订阅情感数据)
- [生成报表](#生成报表)
  * [生成基础数据报表](#生成基础数据报表)
  * [生成情感数据报表](#生成情感数据报表)
- [结束情感服务](#结束情感服务)
- [关闭情感云](#关闭情感云)

如果你需要根据不同场景灵活使用情感云服务，可以使用`EnterAffectiveCloudApi`来调用相应API，该类封装了所有情感云服务对外的接口。各个API调用的相互顺序可以参考以下时序图。

![时序图](https://github.com/Entertech/Enter-AffectiveCloud-Android-SDK/blob/master/media/sequence_diagram.png)

## 会话
### SDK初始化及情感云连接

```kotlin
var appKey = "YOUR_APP_KEY"
var appSecret = "YOUR_APP_SECRET"
var websocketAddress = "AFFECTIVE_CLOUD_WEBSOCKET_ADDRESS"
var userID = "YOUR_APP_USER_ID"
var enterAffectiveCloudApi = EnterAffectiveCloudApiFactory.createApi(websocketAddress,appKey,appSecret,userID)
enterAffectiveCloudApi.openWebSocket(fun(){
    Logger.d("情感云连接成功...")
},fun(error:String){
    Logger.d("情感云连接失败：$error")
})
```

### 是否已连接情感云

```Kotlin
var isWebSocketOpen = enterAffectiveCloudApi.isWebSocketOpen()
Logger.d("情感云是否已连接："+isWebSocketOpen)
```

### 建立会话

```kotlin
enterAffectiveCloudApi.createSession(fun(sessionId:String){
    //成功回调
},fun(error:Error)){
    //失败回调
}
```

### 会话是否已建立

```kotlin
var isSessionCreate = enterAffectiveCloudApi.isSessionCreate()
if(isSessinoCreate){
    var sessionId = enterAffectiveCloudApi.getSessionId()
    Logger.d("当前会话id为："+ sessionId)
}
```

### Restore会话

```kotlin
enterAffectiveCloudApi.restoreSession(fun(){
    //restore成功回调
},fun(error:Error)){
    //restore失败回调
}
```

## 初始化服务

### 初始化基础服务

```kotlin
enterAffectiveCloudApi.initBiodataServices(serviceList,fun(){
    //init成功回调
},fun(error:String){
    //init失败回调
})
```

|   名称    |   说明   |
| :-------: | :------: |
|    EEG    | 脑波数据 |
| HeartRate | 心率数据 |

### 初始化情感服务

```kotlin
enterAffectiveCloudApi.initAffectiveDataServices(serviceList,fun(){
    //init成功回调
},fun(error:String){
    //init失败回调
})
```

可使用的服务如下，具体参见[这里](https://docs.affectivecloud.com/🎙接口协议/5.%20情感计算服务协议.html#affective-start)：

|    名称    |             说明              |
| :--------: | :---------------------------: |
| attention  |  专注度服务 （依赖脑波数据）  |
| relaxation |  放松度服务 （依赖脑波数据）  |
|  pleasure  |  愉悦度服务 （依赖脑波数据）  |
|  pressure  | 压力水平服务 （依赖心率数据） |
|  arousal   |  激活度服务 （依赖心率数据）  |
|   sleep    |      睡眠检测和判断服务       |


## 发送数据

### 发送脑波数据

```kotlin
enterAffectiveCloudApi.appendEEGData(EEGData)
```

### 发送心率数据

```kotlin
enterAffectiveCloudApi.appendHeartRateData(heartRateData)
```

## 接受订阅数据

### 订阅基础数据

```kotlin
enterAffectiveCloudApi.subscribeBiodata(dataNameList,fun(data：Type){
    Logger.d("基础数据："+data)
},fun(){
     //订阅成功回调
},fun(subError:String){
    //订阅失败回调
})
```

可订阅的基础生物数据服务如下：

|  biodataName   |          说明          |
| :------------: | :--------------------: |
| eeg_wave_left  | 脑电波：左通道脑波数据 |
| eeg_wave_right | 脑电波：右通道脑波数据 |
|   eeg_alpha    |  脑电波频段能量：α 波  |
|    eeg_beta    |  脑电波频段能量：β 波  |
|   eeg_theta    |  脑电波频段能量：θ 波  |
|   eeg_delta    |  脑电波频段能量：δ 波  |
|   eeg_gamma    |  脑电波频段能量：γ 波  |
|  eeg_quality   |     脑电波数据质量     |
|    hr_value    |          心率          |
| hr_variability |       心率变异性       |


### 订阅情感数据

```
enterAffectiveCloudApi.subscribeAffectiveData(dataNameList,fun(data:Type){
     Logger.d("情感数据："+data)
},fun(){
     //订阅成功回调
},fun(subError:String){
  	 //订阅失败回调
})
```

可订阅的情感分析数据服务如下，具体参见[这里](https://docs.affectivecloud.com/🎙接口协议/5.%20情感计算服务协议.html#affective-subscribe)：

| 服务类型(cloud_service) | 数据类型(data_type) | 类型  | 取值范围 |                 说明                 |
| :---------------------: | :-----------------: | :---: | :------: | :----------------------------------: |
|        attention        |      attention      | float | [0, 100] |   注意力值，数值越高代表注意力越高   |
|       relaxation        |     relaxation      | float | [0, 100] |   放松度值，数值越高代表放松度越高   |
|        pressure         |      pressure       | float | [0, 100] | 压力水平值，数值越高代表压力水平越高 |
|        pleasure         |      pleasure       | float | [0, 100] | 愉悦度值，数值越高代表情绪愉悦度越高 |
|         arousal         |       arousal       | float | [0, 100] | 激活度值，数值越高代表情绪激活度越高 |
|          sleep          |    sleep_degree     | float | [0, 100] |    睡眠程度，数值越小代表睡得越深    |
|                         |     sleep_state     |  int  |  {0, 1}  | 睡眠状态，0 表示未入睡，1 表示已入睡 |


## 取消订阅

### 取消订阅基础数据

配置你要取消订阅的服务。

```kotlin
enterAffectiveCloudApi.unsubscribeBiodata(dataNameList,fun(){
     //取消订阅成功回调
},fun(subError:String){
    //取消订阅失败回调
})
```

### 取消订阅情感数据

配置你要取消订阅的服务。

```kotlin
enterAffectiveCloudApi.unsubscribeAffectivedata(dataNameList,fun(){
     //取消订阅成功回调
},fun(subError:String){
    //取消订阅失败回调
})
```

## 生成报表

### 生成基础数据报表

生成的基础生物数据报表的格式参数，参见[这里](https://docs.affectivecloud.com/🎙接口协议/4.%20生物数据基础分析服务协议.html#biodata-report)。

```kotlin
enterAffectiveCloudApi.getBiodataReport(dataNameList,fun(data:BiodataReport)){
     Logger.d("基础报表："+data)
}
```

### 生成情感数据报表

生成的情感数据报表的格式参数，参见[这里](https://docs.affectivecloud.com/🎙接口协议/5.%20情感计算服务协议.html#affective-report)。

```kotlin
enterAffectiveCloudApi.getAffectivedataReport(dataNameList,fun(data:AffectiveDataReport)){
     Logger.d("情感报表："+data)
}
```

## 结束情感服务

```kotlin
enterAffectiveCloudApi.finishAffectiveDataServices(fun(){
    Logger.d("已正常结束情感云服务")
},fun(error:String){
    Logger.d("结束情感云服务遇到问题：$error")
})
```

## 关闭情感云

```kotlin
enterAffectiveCloudApi.destroySessionAndCloseWebSocket()
```