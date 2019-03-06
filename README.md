# Demo说明文档

本demo演示了脑波数据通过Socket上传服务器并响应的过程。具体的Socket通信协议可查看[EnterTech情感云平台长连接通信协议](https://github.com/EnterTech/Enter-Biomodule-Demo-Android/blob/master/doc/EnterTech%E6%83%85%E6%84%9F%E4%BA%91%E5%B9%B3%E5%8F%B0%E9%95%BF%E8%BF%9E%E6%8E%A5%E9%80%9A%E4%BF%A1%E5%8D%8F%E8%AE%AE.md)

**Demo简要说明**

打开Demo时，需点击`连接设备`进行设备蓝牙连接，之后可点击`准备上传脑波数据`发送start命令到服务器开启一个会话，开启会话后可点击`上传脑波数据`进行脑波数据上传，点击`结束上传脑波数据`可结束当前会话；发送和接受到的数据会实时显示在屏幕上，可左右滑动进行切换。

<img src="https://github.com/EnterTech/Enter-Biomodule-Demo-Android/blob/master/doc/receive.png" width="50%" height="500px">

<img src="https://github.com/EnterTech/Enter-Biomodule-Demo-Android/blob/master/doc/send.png" width="50%" height="500px">