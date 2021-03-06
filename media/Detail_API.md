# Detailed API function description of Affective Cloud
-[Session](#session)
  * [SDK initialization and affective cloud connection] (#sdk initialization and affective cloud connection)
  * [Whether connected to the affective cloud] (# Whether connected to the affective cloud)
  * [Establish Session] (#Establish Session)
  * [Whether the session has been established] (#whether the session has been established)
  * [Restore Session](#restoreSession)
-[Initial Service](#Initialize Service)
  * [Initialize basic services](#Initialize basic services)
  * [Initialize affective service] (#Initialize affective service)
-[Send data](#Send data)
  * [Send brain wave data] (#Send brain wave data)
  * [Send Heart Rate Data] (#Send Heart Rate Data)
-[Accept subscription data](#Accept subscription data)
  * [Subscription basic data](#Subscription basic data)
  * [Subscribe to affective data] (#Subscribe to affective data)
-[Cancel Subscription](#Cancel Subscription)
  * [Unsubscribe basic data](#Unsubscribe basic data)
  * [Unsubscribe affective data](#Unsubscribe affective data)
-[Generate report](#Generate report)
  * [Generate basic data report](#Generate basic data report)
  * [Generate affective data report](#Generate affective data report)
-[End Affective Service] (#End Affective Service)
-[Close Affective Cloud] (#Close Affective Cloud)

If you need to use affective cloud services flexibly according to different scenarios, you can use `EnterAffectiveCloudApi` to call the corresponding API, which encapsulates all the external interfaces of affective cloud services. The mutual sequence of each API call can refer to the following sequence diagram.
![时序图](https://github.com/Entertech/Enter-AffectiveCloud-Android-SDK/blob/master/media/sequence_diagram.png)
## Conversation
### SDK initialization and emotional cloud connection

```kotlin
var appKey = "YOUR_APP_KEY"
var appSecret = "YOUR_APP_SECRET"
var websocketAddress = "AFFECTIVE_CLOUD_WEBSOCKET_ADDRESS"
var userID = "YOUR_APP_USER_ID"
var enterAffectiveCloudApi = EnterAffectiveCloudApiFactory.createApi(websocketAddress,appKey,appSecret,userID)
enterAffectiveCloudApi.openWebSocket(fun(){
     Logger.d("Affective cloud connection is successful...")
},fun(error:String){
     Logger.d("Affective Cloud Connection Failed: $error")
})
```

### Is the emotional cloud connected?

```Kotlin
var isWebSocketOpen = enterAffectiveCloudApi.isWebSocketOpen()
Logger.d("Is the emotional cloud connected:"+isWebSocketOpen)
```
### Establish a session

```kotlin
   enterAffectiveCloudApi.createSession(fun(sessionId:String){
        //Success callback
   },fun(error:Error)){
        //Failure callback
   }
```

### Is the session established?

   ```kotlin
   var isSessionCreate = enterAffectiveCloudApi.isSessionCreate()
   if(isSessinoCreate){
        var sessionId = enterAffectiveCloudApi.getSessionId()
        Logger.d("The current session id is:" + sessionId)
   }
   ```

### Restore session

   ```kotlin
   enterAffectiveCloudApi.restoreSession(fun(){
        //restore success callback
   },fun(error:Error)){
        //restore failed callback
   }
   ```
## Initialize the service

### Initialize basic services

```kotlin
enterAffectiveCloudApi.initBiodataServices(serviceList,fun(){
    //Init success callback
},fun(error:String){
    //init failed callback
})
```

| Name | Description |
| :-------: | :------: |
| EEG | Brain Wave Data |
| HeartRate | Heart Rate Data |

### Initialize emotional service

```kotlin
enterAffectiveCloudApi.initAffectiveDataServices(serviceList,fun(){
    //Init success callback
},fun(error:String){
    //init failed callback
})
```

The available services are as follows, see [here] (https://docs.affectivecloud.com/🎙interface protocol/5.%20 affective computing service protocol.html#affective-start):

| Name | Description |
| :--------: | :---------------------------: |
| attention | Attention service (rely on brain wave data) |
| relaxation | Relaxation service (relies on brain wave data) |
| pleasure | Pleasure service (relies on brain wave data) |
| pressure | Pressure level service (depending on heart rate data) |
| arousal | Activation service (depending on heart rate data) |
| sleep | Sleep detection and judgment service |

## send data

### Send brainwave data

```kotlin
enterAffectiveCloudApi.appendEEGData(EEGData)
```

### Send heart rate data

```kotlin
enterAffectiveCloudApi.appendHeartRateData(heartRateData)
```

## Accept subscription data

### Subscribe to basic data

```kotlin
enterAffectiveCloudApi.subscribeBiodata(dataNameList,fun(data:Type){
    Logger.d("Basic data:"+data)
},fun(){
     //Subscription success callback
},fun(subError:String){
    //Subscription failure callback
})
```

The basic biological data services that can be subscribed are as follows:

| biodataName | Description |
| :------------: | :--------------------: |
| eeg_wave_left | Brainwave: Left channel brainwave data |
| eeg_wave_right | Brainwave: Right channel brainwave data |
| eeg_alpha | Brainwave band energy: alpha wave |
| eeg_beta | Brainwave band energy: β wave |
| eeg_theta | Brainwave band energy: Theta wave |
| eeg_delta | Brainwave band energy: δ wave |
| eeg_gamma | Brain wave frequency band energy: γ wave |
| eeg_quality | Brainwave data quality |
| hr_value | Heart rate |
| hr_variability | Heart Rate Variability |
### Subscribe to emotional data

```
enterAffectiveCloudApi.subscribeAffectiveData(dataNameList,fun(data:Type){
     Logger.d("Affective data:"+data)
},fun(){
     //Subscription success callback
},fun(subError:String){
  //Subscription failure callback
})
```

The sentiment analysis data services that can be subscribed are as follows, see [here] (https://docs.affectivecloud.com/🎙interface agreement/5.%20 affective computing service agreement.html#affective-subscribe):

| Service type (cloud_service) | Data type (data_type) | Type | Value range | Description |
| :---------------------: | :-----------------: | :---: | :------: | :----------------------------------: |
| attention | attention | float | [0, 100] | Attention value, the higher the value, the higher the attention |
| relaxation | relaxation | float | [0, 100] | Relaxation value, the higher the value, the higher the relaxation |
| pressure | pressure | float | [0, 100] | The pressure level value, the higher the value, the higher the pressure level |
| pleasure | pleasure | float | [0, 100] | Pleasure value, the higher the value, the higher the emotional pleasure |
| arousal | arousal | float | [0, 100] | Activation value, the higher the value, the higher the emotional activation |
| sleep | sleep_degree | float | [0, 100] | Sleep degree, the smaller the value, the deeper the sleep |
| | sleep_state | int | {0, 1} | Sleep state, 0 means not falling asleep, 1 means falling asleep |


## unsubscribe

### Unsubscribe basic data

Configure the service you want to unsubscribe.

```kotlin
enterAffectiveCloudApi.unsubscribeBiodata(dataNameList,fun(){
     //Cancel subscription success callback
},fun(subError:String){
    //Unsubscribe failed callback
})
```

### Unsubscribe from emotional data

Configure the service you want to unsubscribe.

```kotlin
enterAffectiveCloudApi.unsubscribeAffectivedata(dataNameList,fun(){
     //Cancel subscription success callback
},fun(subError:String){
    //Cancel subscription failure callback
})
```
## Generate reports

### Generate basic data report

For the format parameters of the generated basic biological data report, please refer to [here](https://docs.affectivecloud.com/🎙Interface Protocol/4.%20Biodata Basic Analysis Service Agreement.html#biodata-report).

```kotlin
enterAffectiveCloudApi.getBiodataReport(dataNameList,fun(data:BiodataReport)){
     Logger.d("Basic report:"+data)
}
```

### Generate emotional data report

For the format parameters of the generated emotional data report, see [here](https://docs.affectivecloud.com/🎙interface protocol/5.%20 affective computing service protocol.html#affective-report).

```kotlin
enterAffectiveCloudApi.getAffectivedataReport(dataNameList,fun(data:AffectiveDataReport)){
     Logger.d("Affective report:"+data)
}
```

## End emotional service

```kotlin
enterAffectiveCloudApi.finishAffectiveDataServices(fun(){
    Logger.d("The emotional cloud service has ended normally")
},fun(error:String){
    Logger.d("End of the emotional cloud service encountered a problem: $error")
})
```

## Close emotional cloud

```kotlin
enterAffectiveCloudApi.destroySessionAndCloseWebSocket()
```