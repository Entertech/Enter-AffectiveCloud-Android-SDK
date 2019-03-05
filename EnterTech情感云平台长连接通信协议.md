#EnterTech情感云平台长连接通信协议

## 简介

由生物电采集模块采集到设备信息包括脑波、心率等数据需要上传到情感云端进行数据分析，分析后由云端返回处理结果到客户端。这一过程采用Socket长连接进行双向通信。数据以json格式传输，且为了便于分辨数据结束的位置，在每个json体后加上”\r\n“结束符。

> 注意：本文档暂时以脑波数据为例，演示代码采用kotlin编写

## API说明

### 连接平台

**说明**

Socket连接到平台地址。平台地址：

> 域名: api.affectivecloud.com 端口：8080

**代码示例**

```kotlin
var mBrainDataSocket = Socket()
mBrainDataSocket?.connect(InetSocketAddress("api.affectivecloud.com", 8080))
```

### 数据接受

**说明**

由于服务器会定时发送数据到客户端，这里单独开启一个线程用来接受服务器返回的数据

**代码示例**

```kotlin
var inputStream = mBrainDataSocket?.getInputStream()
var bufferedReader = BufferedReader(InputStreamReader(inputStream))
while (true) {
	var result = bufferedReader.readLine()
	if (result != null) {
        Logger.d("receive data from server: $result")
	}
}
```

### 数据发送

**说明**

定义一个全局的发送方法

**代码示例**

```kotlin
fun sendMessage(json: String) {
	var pw: PrintWriter? = null
	try {
		pw = PrintWriter(mBrainDataSocket?.getOutputStream())
		pw.write(json + "\r\n")
		pw.flush()
		Logger.d("send data to server: $data\r\n")
	} catch (e: IOException) {
		Logger.d("send error:$e")
		e.printStackTrace()
	}
}
```

将模块采集到的数据发送服务器分三个步骤：

1. 发送start命令到服务器开启临时会话
2. 发送process命令，并携带数据到服务器（这里以脑波数据为例）
3. 发送finish命令结束会话

#### start(开启会话)

**发送示例**

```json
{
	"command": "start",
	"device_id": "A0"
}
```

**参数说明**

| 名称      | 类型   | 可选 | 说明         |
| --------- | ------ | ---- | ------------ |
| command   | String | NO   | 必须为 start |
| device_id | String | NO   | 区分不同设备 |

**响应示例**

```json
{
	"command": "start",
    "session_id": "7a8d4266-e411-461a-af12-9540a3b2f05b"
}
```

**返回参数说明**

| 名称       | 类型   | 可选 | 说明         |
| ---------- | ------ | ---- | ------------ |
| command    | String | NO   | 必须为 start |
| session_id | String | NO   | 会话 id      |

#### process(上传数据，这里以脑波数据为例)

**发送示例**

```json
{
	"command": "process",
	"data": "[1,3,5,3,...5,4,19,36]"//无符号int数组
}
```

**参数说明**

| 名称    | 类型          | 可选 | 说明           |
| ------- | ------------- | ---- | -------------- |
| command | String        | NO   | 必须为 process |
| data    | 无符号Int数组 | NO   | 原始脑电数据   |

> **注意：从硬件采集到的脑波数据为一个包20个字节，每次发送服务器需要600字节，因此需要客户端缓存30个包后再发到服务器；另外由于客户端平台的差异性，采集到的脑波数据可能是有符号数据，需转为无符号Int数组后再传到服务器。**

**响应示例**

```json
//实时脑电波json数据
{
    "data_type": "eeg_wave", 
    "left_eeg_wave": [34, 23, 3...3,52,62],
    "right_eeg_wave": [35, 23, 6...35,81,9],
}
//实时注意力与放松度
{
    "data_type": "attention_relaxation", 
    "attention": 64, "relaxation": 34
}
```

**参数说明**

**实时脑电波形**

| 名称           | 类型        | 可选 | 说明               |
| -------------- | ----------- | ---- | ------------------ |
| data_type      | string      | NO   | 必须为 eeg_wave    |
| left_eeg_wave  | float array | NO   | 左通道脑电波形片段 |
| right_eeg_wave | float array | NO   | 右通道脑电波形片段 |

**实时注意力与放松度**

| 名称       | 类型   | 可选 | 说明                        |
| ---------- | ------ | ---- | --------------------------- |
| data_type  | String | NO   | 必须为 attention_relaxation |
| attention  | Float  | NO   | 实时注意力                  |
| relaxation | Float  | NO   | 实时放松度                  |

#### finish（结束会话）

**发送示例**

```json
{
    "command": "finish"
}
```

**参数说明**

| 名称    | 类型   | 可选 | 说明          |
| ------- | ------ | ---- | ------------- |
| command | String | NO   | 必须为 finish |

**响应示例**

```json
{
    "command": "finish", 
    "attention_avg": 85.34, 
    "relaxation_avg": 43
}
```

**参数说明**

| 名称           | 类型   | 可选 | 说明          |
| -------------- | ------ | ---- | ------------- |
| command        | String | NO   | 必须为 finish |
| attention_avg  | Float  | NO   | 注意力平均值  |
| relaxation_avg | Float  | NO   | 放松度平均值  |

### 错误响应

当请求出现异常时，服务器会返回错误信息

**示例**

```json
{
	"command": "start|process|finish"
	"error_code": 1
	"message": "message indicates possible error causes."
}
```

#### **参数说明**

| 名称       | 类型   | 可选 | 说明                                             |
| ---------- | ------ | ---- | ------------------------------------------------ |
| error_code | String | NO   | 错误码                                           |
| command    | String | YES  | 该错误对应哪个请求，若无则表示非请求指向性的错误 |
| message    | String | NO   | 错误消息                                         |

#### 错误码

| error_code | 说明           | 可能原因                        |
| ---------- | -------------- | ------------------------------- |
| 0          | 请求体过大     | 比如分隔符忘了加                |
| 1          | JSON 解析失败  | JSON 格式错误                   |
| 2          | 参数错误       | 缺少参数；参数内容或类型不对    |
| 3          | 命令错误       | 比如 start 前就开始发送 process |
| 4          | 服务器内部错误 | 服务器出问题，联系服务器开发者  |
| 5          | 服务器致命错误 | 服务器将会主动关闭连接          |
| 9999       | 未知错误       | 尚未明确定义的错误              |


