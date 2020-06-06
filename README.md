# Mobile-project (One Auth)
CS5010, Final project

通过加速度传感器数据（500Hz），做身份识别。识别动作：单次敲击或者拍打手机背部，产生特异振动。

## Member
* Changchao Sun ([@sunc2](https://github.com/sunc2))
* Ziqiang Li ([@liziwl](https://github.com/liziwl))

## SenseLFlip (Android 客户端)
1. 采集数据保存为CSV，在手机 Download/OneAuth 文件夹下
2. 测试新增数据

## judge（Python 服务器）
1. 提供网络接口
2. 授权判定

### Reference
我们参考了 Taprint: Secure Text Input for Commodity Smart Wristbands @ MobiCom '19。我们借鉴了他们的思想，但是分类和判定模型并没有采用他们实现，另外选择了，具体可以查看Slides。
