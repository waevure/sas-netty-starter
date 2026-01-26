# sas-netty-starter
> 这是一个
> 这种
> 外网
> 

## 项目概览
`sas-netty-starter` 是一个基于 Netty 的通信脚手架（Spring Boot 2.x 依赖风格），提供统一的启动流程、通道管理和处理器链组装能力，支持 TCP/HTTP/UDP 以及“无网络嵌入式通道”场景。

核心目标：用一致的方式搭建不同协议的网络通信项目，并在运行期获得统一的操作接口（Operations）与通道管理能力。

## 核心概念
- `NettyGuideAbstract`：统一入口与注册中心（初始化/获取能力/销毁）
- `NettySupport`：构建 Netty 环境与责任链
- `NettyLink`：链路配置（端口、解码器、日志、处理器、回调等）
- `NettyType`：TCP/HTTP/UDP/NO_NETWORK_CHANNEL

## 快速使用流程
1. 定义 `ProjectAbstract` 子类标识项目
2. 组装 `NettyLink`
3. Guide 中调用 `initStart(...)`
4. 通过 Guide 获取 Operations 能力进行连接/发包/关闭

## 完整文档
详见 `PROJECT_GUIDE.md`。

## Demo 入口
- `src/test/java/demo/DemoTcpServer.java`
- `src/test/java/demo/DemoTcpClient.java`
- `src/test/java/demo/DemoHttpServer.java`
- `src/test/java/demo/DemoHttpClient.java`
- `src/test/java/demo/DemoUdp.java`
- `src/test/java/demo/DemoNoNetwork.java`
