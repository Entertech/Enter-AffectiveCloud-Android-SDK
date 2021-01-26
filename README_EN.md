# Enter-AffectiveCloud-Andriod-SDK [![Download](https://api.bintray.com/packages/hzentertech/maven/affectivecloud/images/download.svg?version=2.0.0)](https://bintray.com/hzentertech/maven/affectivecloud/2.0.0/link)
- [Introduction](#introduction)
- [Demo](#demo)
- [Getting Started](#getting-started)
    + [Gradle](#gradle)
    + [Permissions](#permissions)
- [How to use](#how-to-use)
  * [1.Initialization](#1initialization)
  * [2. Add real-time data listener](#2-add-real-time-data-listener)
  * [3. Upload data](#3-upload-data)
  * [4. Get the report](#4-get-the-report)
  * [5. Release](#5-release)
- [Detailed API function description](#detailed-api-function-description)
- [Change Notes](#change-notes)
# Introduction

Enter Affective Cloud is a cloud algorithm platform that can perform advanced emotional data analysis based on the user’s brainwave data and heart rate data, and can also provide: relaxation, attention, pleasure, stress, excitement (internal test ), including a variety of emotional values. For details, please see [Official Website](https://www.entertech.cn).

Before starting the development, please check the [Development Documents](https://docs.affectivecloud.com) of Enter Affective Cloud to understand the structure of Affective Cloud platform and specific descriptions of the services it can provide, and determine your application The service needed. You also need to contact the administrator to register the test application, and then develop.

In order to facilitate your rapid development of the Android platform, we provide the affective cloud rapid development SDK, through which you can quickly integrate the emotional cloud capabilities into your app.

# Demo

**Download demo apk**

[demo.apk](http://fir.entertech.cn/gb3a)


**Description**

For more demo operation instructions, please see [Enter Bioelectric Acquisition Module Demo](https://github.com/Entertech/Enter-AffectiveCloud-Android-SDK/tree/master/demo)

In addition, we also provide a heart flow demonstration application, which integrates Bluetooth SDK, Bluetooth device management SDK, affective cloud SDK and other functions. For details, please see [Heart Flow Demo Application](https://github.com/Entertech/Enter-Affective-Cloud-Demo-Android)

# Getting Started

### Gradle
Add the following dependencies under the module's build.gradle file
```groovy
    implementation'cn.entertech:affectivecloud:2.0.0'
```
Add the following dependency address under the build.gradle file in the project root directory
```groovy
allprojects {
    repositories {
        maven {
            url "https://dl.bintray.com/hzentertech/maven"
        }
    }
}
```
### Permissions

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

# How to use


The SDK provides a management class `EnterAffectiveCloudManager` for quick access to the affective cloud. Using this class only takes a few steps to complete the data interaction between the client and the affective cloud platform.

##  1.Initialization

```kotlin
//biodata services
var availableBioServices = listOf(Service.EEG, Service.HR)
//affective services(Need to apply to the official)
var availableAffectiveServices = listOf(Service.ATTENTION, Service.PRESSURE, Service.AROUSAL, Service.SLEEP)
//biodata subscribe params
var biodataSubscribeParams = BiodataSubscribeParams.Builder()
            .requestEEG//subscribe all eeg data
            .requestHr//subscribe add heart rate data
            .build()
//affective data subscribe params
var affectiveSubscribeParams = AffectiveSubscribeParams.Builder()
            .requestSleep()//subscribe sleep data
            .requestAttention()//subscribe attention data
            .requestRelaxation()//subscribe relaxation data
            .requestPressure()//subscribe pressure data
            .requestPleasure()//subscribe pleasure data
            .build()
//configs
var enterAffectiveCloudConfig = EnterAffectiveCloudConfig.Builder(APP_KEY, APP_SECRET, USER_ID)
            .url(websocketAddress)// config websocket address
            .timeout(10000)//config timeout of websocket connection unit：ms
            .availableBiodataServices(availableBioServices)//available biodata services
            .availableAffectiveServices(availableAffectiveServices)//available affective services
            .biodataSubscribeParams(biodataSubscribeParams!!)//biodata subscribe params
            .affectiveSubscribeParams(affectiveSubscribeParams!!)//affective subscribe params
            .build()
//create manager
var enterAffectiveCloudManager = EnterAffectiveCloudManager(enterAffectiveCloudConfig)
//init sdk
enterAffectiveCloudManager?.init(object : Callback {
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("init failed：${error.toString()}")
            }
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("init success")
            }
        })
```
**Parameter Description**

|Parameter|Type|Description|
|:--:|:--:|:--:|
| websocketAddress | String | Affective cloud server link, see [link](https://docs.affectivecloud.com/🎙interface protocol/1.%20 summary.html#official) |

|Parameter|Type|Description|
|:--:|:--:|:--:|
| APP_KEY | String | Generated by our background: App Key |
| APP_SECRET | String | Generated by our background: App Secret|
| USER_ID | String | The id of the current user of your app, such as mobile phone number, id number, nickname, etc., must be unique. See [userID](https://docs.affectivecloud.com/🎙Interface Protocol/3.%20Session Protocol.html#userID) for details |

## 2. Add real-time data listener

Note that if you want to return data in real time, you need to configure and subscribe to the corresponding data field in the above steps, otherwise the monitor will return no data.

```kotlin
enterAffectiveCloudManager!!.addBiodataRealtimeListener {
            messageReceiveFragment.appendMessageToScreen("Basic service real-time data: ${it.toString()}")
        }
enterAffectiveCloudManager!!.addAffectiveDataRealtimeListener {
            messageReceiveFragment.appendMessageToScreen("Affective service real-time data: ${it.toString()}")
        }
```

## 3. Upload data

**Upload brainwave data**

```kotlin
enterAffectiveCloudManager?.appendEEGData(bytes)
```

**Upload heart rate data**

```kotlin
enterAffectiveCloudManager?.appendHeartRateData(heartRate)
```

> Note: The above data are all directly returned by the hardware. If the hardware has data to return, call the corresponding method and input the data without other processing.

## 4. Get the report

The report field returned is determined by the previous configuration. For detailed descriptions of specific fields, see [Biodata Basic Report Parameters](https://docs.affectivecloud.com/🎙Interface Protocol/4.%20Biodata Basic Analysis Service Agreement.html#biodata-report) and [Emotional Calculation Report Parameters](https://docs.affectivecloud.com/🎙interface protocol/5.%20 affective computing service protocol.html#affective-report).

```kotlin
enterAffectiveCloudManager?.getBiodataReport(object: Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen("Basic report: ${t.toString()}")
            }
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("Basic report error: ${error.toString()}")
            }
        })
enterAffectiveCloudManager?.getAffectiveDataReport(object: Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen("Affective report: ${t.toString()}")
            }
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("Affective report error: ${error.toString()}")
            }
        })
```

## 5. Release

Note that you need to call the following `release` method to release resources every time you use the affective cloud service, otherwise you will face the risk of continuous deductions

```kotlin
enterAffectiveCloudManager?.release(object: Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("Affective cloud has been successfully disconnected!")
            }
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("Affective cloud disconnection failed: ${error}")
            }
        })
```


# Detailed API function description

If you need to use emotional cloud services flexibly according to different scenarios, you can use `EnterAffectiveCloudApi` to call the corresponding API, which encapsulates all the external interfaces of emotional cloud services. For more detailed Affective Cloud API, please refer to [Affective Cloud Detailed API Function Description](https://github.com/Entertech/Enter-AffectiveCloud-Android-SDK/blob/master/media/%E6%83%85%E6% 84%9F%E4%BA%91%E8%AF%A6%E7%BB%86API%E5%8A%9F%E8%83%BD%E8%AF%B4%E6%98%8E.md)

# Change Notes
[Change Notes](https://github.com/Entertech/Enter-AffectiveCloud-Android-SDK/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)
















