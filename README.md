# 回车生物电采集模块演示 demo

## 硬件使用
关于硬件的操作，请参考[回车生物电采集模块操作说明](https://github.com/EnterTech/Enter-Biomodule-Demo-Android/wiki/回车生物电采集模块操作说明)。

硬件使用的注意事项，请参考[回车生物电采集模块使用注意事项](https://github.com/EnterTech/Enter-Biomodule-Demo-Android/wiki/回车生物电采集模块使用注意事项)。

## Demo 说明

本 demo 演示了设备连接和脑波数据的采集，将采集到的数据发送到云端，经过云端算法分析后返回分析结果的整个过程。主要包含两部分：
* 集成模块的蓝牙 SDK，完成设备连接和数据采集，具体参见此蓝牙 SDK 工程--[Enter-Biomodule-BLE-Android-SDK](https://github.com/EnterTech/Enter-Biomodule-BLE-Android-SDK)；
* 将采集数据通过 Socket 上传云端并接收分析结果。具体的 Socket 通信协议可查看[回车情感云平台长连接通信协议](https://github.com/Entertech/Enter-Biomodule-Demo-Android/wiki/%E5%9B%9E%E8%BD%A6%E6%83%85%E6%84%9F%E4%BA%91%E5%B9%B3%E5%8F%B0%E6%8E%A5%E5%8F%A3%E5%8D%8F%E8%AE%AE%EF%BC%88v0.2%EF%BC%89)。

## 操作
* 打开Demo时，需点击`连接设备`进行设备蓝牙连接，此时设备需处于[广播状态](https://github.com/EnterTech/Enter-Biomodule-Demo-Android/wiki/回车生物电采集模块操作说明#连接指示灯)；
* 连接设备后，点击 `准备上传脑波数据` 发送 `start` 命令到服务器开启一个会话；
* 开启会话后可点击`上传脑波数据`进行脑波数据上传；
* 点击`结束上传脑波数据`可结束当前会话。
* 点击`模拟会话中断并恢复`可实现模拟socket中断并重新连接，重连后可进行接下来的操作（中断重连超时为5分钟）。

## 界面交互和展示

接收和发送的数据会实时显示在屏幕上，可左右滑动进行切换。

<img src="https://github.com/EnterTech/Enter-Biomodule-Demo-Android/blob/master/doc/receive.png" width="40%">

<img src="https://github.com/EnterTech/Enter-Biomodule-Demo-Android/blob/master/doc/send.png" width="40%">
