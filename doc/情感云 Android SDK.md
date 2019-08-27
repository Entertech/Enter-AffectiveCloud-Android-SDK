# 回车情感云平台SDK(Android)

## 简介

基于原有[情感云通信协议](<https://docs.affectivecloud.com/%F0%9F%8E%99%E6%8E%A5%E5%8F%A3%E5%8D%8F%E8%AE%AE/1.%20%E7%BB%BC%E8%BF%B0.html>)，大大简化了客户端与情感云平台数据交互。

## 集成

### Gradle 自动集成
在module的build.gradle文件下添加以下依赖
```groovy
implementation 'cn.entertech:affectivecloud:1.0.0-alpha'
implementation 'com.google.code.gson:gson:2.8.5'
implementation "org.java-websocket:Java-WebSocket:1.4.0"
```
在项目根目录的build.gradle文件下添加以下依赖地址
```groovy
allprojects {
    repositories {
        maven {
            url "https://dl.bintray.com/hzentertech/maven"
        }
    }
}
```
### 权限申请

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

## 快速接入

SDK提供了快速接入情感云的管理类`EnterAffectiveCloudManager`，使用该类只需要几步就可以完成客户端与情感云平台的数据交互。

###  1.初始化

```kotlin
//基础服务
var availableBioServices = listOf(Service.EEG, Service.HR)
//情感云服务(需向官方申请)
var availableAffectiveServices = listOf(Service.ATTENTION, Service.PRESSURE, Service.AROUSAL, Service.SLEEP)
//基础服务订阅参数
var biodataSubscribeParams = BiodataSubscribeParams.Builder()
            .requestAllEEGData()//订阅所有eeg数据
            .requestAllHrData()//订阅所有心率相关数据
            .build()
//情感服务订阅参数
var affectiveSubscribeParams = AffectiveSubscribeParams.Builder()
            .requestAllSleepData()//订阅所有sleep服务数据
            .requestAttention()//订阅attention数据
            .requestRelaxation()//订阅relaxation数据
            .requestPressure()//订阅pressure数据
            .requestPleasure()//订阅pleasure数据
            .build()
//配置项
var enterAffectiveCloudConfig = EnterAffectiveCloudConfig.Builder(APP_KEY, APP_SECRET, USER_ID)
            .url(websocketAddress)//配置websocket地址
            .timeout(10000)//配置websocket连接超时时间 单位：ms
            .availableBiodataServices(availableBioServices)//可用的基础服务
            .availableAffectiveServices(availableAffectiveServices)//可用的情感服务
            .biodataSubscribeParams(biodataSubscribeParams!!)//基础服务订阅参数
            .affectiveSubscribeParams(affectiveSubscribeParams!!)//情感服务订阅参数
            .build()
//创建管理类
var enterAffectiveCloudManager = EnterAffectiveCloudManager(enterAffectiveCloudConfig)
//初始化SDK
enterAffectiveCloudManager?.init(object : Callback {
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("初始化失败：${error.toString()}")
            }
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("初始化成功")
            }
        })
```

### 2.添加实时数据监听

注意如果要想实时返回数据，需要在上面步骤中配置订阅相应数据字段，否则监听将无数据返回。

```kotlin
enterAffectiveCloudManager!!.addBiodataRealtimeListener {
            messageReceiveFragment.appendMessageToScreen("基础服务实时数据：${it.toString()}")
        }
enterAffectiveCloudManager!!.addAffectiveRealtimeListener {
            messageReceiveFragment.appendMessageToScreen("情感服务实时数据：${it.toString()}")
        }
```

### 3.上传数据

**上传脑波数据**

```kotlin
enterAffectiveCloudManager?.appendBrainData(bytes)
```

**上传心率数据**

```kotlin
enterAffectiveCloudManager?.appendHeartRateData(heartRate)
```

> 注意：上面数据都直接由硬件返回，硬件有数据返回就调用相应的方法，传入数据即可，无需做其他处理

### 4.获取报表

相应返回的report字段，由之前配置决定。具体字段的详细描述见[生物数据基础分析服务协议](<https://docs.affectivecloud.com/%F0%9F%8E%99%E6%8E%A5%E5%8F%A3%E5%8D%8F%E8%AE%AE/4.%20%E7%94%9F%E7%89%A9%E6%95%B0%E6%8D%AE%E5%9F%BA%E7%A1%80%E5%88%86%E6%9E%90%E6%9C%8D%E5%8A%A1%E5%8D%8F%E8%AE%AE.html>)和[情感计算服务协议](<https://docs.affectivecloud.com/%F0%9F%8E%99%E6%8E%A5%E5%8F%A3%E5%8D%8F%E8%AE%AE/5.%20%E6%83%85%E6%84%9F%E8%AE%A1%E7%AE%97%E6%9C%8D%E5%8A%A1%E5%8D%8F%E8%AE%AE.html>)。

```kotlin
enterAffectiveCloudManager?.reportBiodata(object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen("基础报表：${t.toString()}")
            }
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("基础报表出错：${error.toString()}")
            }
        })
enterAffectiveCloudManager?.reportAffective(object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen("情感报表：${t.toString()}")
            }
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感报表出错：${error.toString()}")
            }
        })
```

### 5.资源释放

注意，每次使用完情感云服务都需调用如下`release`方法来释放资源，否则会面临持续扣费的风险

```kotlin
enterAffectiveCloudManager?.release(object : Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("情感云已成功断开！")
            }
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感云断开失败：${error}")
            }
        })
```


## 详细API功能说明

如果你需要根据不同场景灵活使用情感云服务，可以使用`EnterAffectiveCloudApi`来调用相应API，该类封装了所有情感云服务对外的接口。各个API调用的相互顺序可以参考以下时序图。

### 时序图

![时序图](https://github.com/Entertech/Enter-Biomodule-Demo-Android/blob/develop/doc/sequence_diagram.png)


### SDK初始化及情感云连接

```kotlin
var appKey = "YOUR_APP_KEY"
var appSecret = "YOUR_APP_SECRET"
var websocketAddress = "AFFECTIVE_CLOUD_WEBSOCKET_ADDRESS"
var enterAffectiveCloudApi = EnterAffectiveCloudApiFactory.createApi(websocketAddress,appKey,appSecret)
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

### 初始化

#### 初始化基础服务

```kotlin
enterAffectiveCloudApi.initBiodataServices(serviceList,fun(){
    //init成功回调
},fun(error:String){
    //init失败回调
})
```

#### 初始化情感服务

```kotlin
enterAffectiveCloudApi.initAffectiveServices(serviceList,fun(){
    //init成功回调
},fun(error:String){
    //init失败回调
})
```

### 发送数据

#### 发送脑波数据

```kotlin
enterAffectiveCloudApi.appendBrainData(brainData)
```

#### 发送心率数据

```kotlin
enterAffectiveCloudApi.appendHeartRateData(heartRateData)
```

### 接受订阅数据

#### 订阅基础数据

```kotlin
enterAffectiveCloudApi.subscribeBiodata(dataNameList,fun(data：Type){
    Logger.d("基础数据："+data)
},fun(){
     //订阅成功回调
},fun(subError:String){
    //订阅失败回调
})
```

#### 订阅情感数据

```
enterAffectiveCloudApi.subscribeAffectiveData(dataNameList,fun(data:Type){
     Logger.d("情感数据："+data)
},fun(){
     //订阅成功回调
},fun(subError:String){
  	 //订阅失败回调
})
```

### 取消订阅

#### 取消订阅基础数据

```kotlin
enterAffectiveCloudApi.unsubscribeBiodata(dataNameList,fun(){
     //取消订阅成功回调
},fun(subError:String){
    //取消订阅失败回调
})
```

#### 取消订阅情感数据

```
enterAffectiveCloudApi.unsubscribeAffectivedata(dataNameList,fun(){
     //取消订阅成功回调
},fun(subError:String){
    //取消订阅失败回调
})
```

### 生成报表

#### 生成基础数据报表

```kotlin
enterAffectiveCloudApi.reportBiodata(dataNameList,fun(data:BioReport)){
     Logger.d("基础报表："+data)
}
```

#### 生成情感数据报表

```
enterAffectiveCloudApi.reportAffectiveData(dataNameList,fun(data:AffectiveReport)){
     Logger.d("情感报表："+data)
}
```

### 结束情感服务

```kotlin
enterAffectiveCloudApi.finishAffectiveServices(fun(){
    Logger.d("已正常结束情感云服务")
},fun(error:String){
    Logger.d("结束情感云服务遇到问题：$error")
})
```

### 关闭情感云

```kotlin
enterAffectiveCloudApi.destroySessionAndCloseWebSocket()
```

















