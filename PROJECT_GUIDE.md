# sas-netty-starter 说明文档

## 项目概览
`sas-netty-starter` 是一个基于 Netty 的通信脚手架（Spring Boot 2.x 依赖风格），提供统一的启动流程、通道管理和处理器链组装能力，支持 TCP/HTTP/UDP 以及“无网络嵌入式通道”场景。

核心目标：用一致的方式搭建不同协议的网络通信项目，并在运行期获得统一的操作接口（Operations）与通道管理能力。

## 技术栈与依赖
- Java 8
- Spring Boot 2.7.18（父工程）
- Netty（`netty-all`）
- Hutool（工具包）
- Gson（JSON）
- Lombok（代码简化）

## 目录结构速览
- `src/main/java/com/sas/sasnettystarter/netty`
  - `NettyGuideAbstract`：统一入口与注册中心
  - `NettySupport`：构建 Netty 环境、拼装责任链
  - `NettyLink`：链路配置（端口、解码器、日志、处理器、回调等）
  - `ops/*`：具体协议能力实现（TCP/HTTP/UDP/Embedded）
  - `handle/*`：处理器基类与默认状态管理器
  - `unpack/*`：拆包/解包配置
  - `cache/*`：通道缓存
  - `log/*`：日志处理与回调
- `src/test/java/*`
  - 各协议使用示例（TCP/HTTP/UDP/NoNetwork）

## 核心概念
### 1) ProjectAbstract
用于标识项目/协议实例，建议业务侧继承并填充 `projectName`、`projectCode`。

### 2) NettyType
指定启动类型：
- `C_TCP` / `S_TCP`：TCP 客户端 / 服务端
- `C_HTTP` / `S_HTTP`：HTTP 客户端 / 服务端
- `UDP`：UDP 通道
- `NO_NETWORK_CHANNEL`：嵌入式 Channel（无网络）

### 3) NettyLink
链路配置对象，负责拼装 Netty 初始化参数：
- 端口、Bootstrap options、日志、拆包器
- `beforePipelines`：在拆包前插入自定义 handler（常用于 HTTP codec）
- `logicHandlers` / `readHandlers` / `writeHandlers`
- 默认通道状态管理器开关
- 线程池绑定（`EventExecutorGroup`）

### 4) NettyGuideAbstract
全局管理入口，负责：
- 初始化并启动（`initStart`）
- 获取运行能力（例如 `tcpServerOperations`）
- 销毁服务（`destroyServer`）

建议业务侧创建自己的 Guide 类继承它，并维护 `ProjectAbstract` 的缓存（见测试示例）。

### 5) NettySupport
封装 Netty 具体构建与启动逻辑：
- 组装 Pipeline
- 创建 Bootstrap/ServerBootstrap
- 运行 `NettyRunnable` 进行阻塞监听

### 6) NettyServerBaseContext
各协议能力的基础类，提供：
- `writeAndFlush` / `closeConnect` / `channelActive`
- `VariableChannelCache` 通道缓存

### 7) 默认通道状态管理
通过 `NettyLink.openDefaultChannelStatus(...)` 启用：
- `ChannelStatusManager` 负责通道上线/下线状态维护
- 通道缓存存入 `VariableChannelCache`（key: `ip:port`）
- 可自定义上线/下线处理器

UDP 和 NO_NETWORK_CHANNEL 不启用该逻辑。

## 责任链组装顺序（概念）
1. LoggingHandler（可选）
2. `beforePipelines`（业务自定义，如 HTTP codec）
3. 拆包器 `Unpacking`（可选）
4. 默认状态管理器（可选）
5. LogicHandlers
6. ReadHandlers
7. WriteHandlers

> 注：实际代码中 Logic/Read/Write 统一被加入 pipelines，按添加顺序执行。

## 使用流程（通用）
1. 定义项目 `ProjectAbstract` 子类
2. 组装 `NettyLink`
3. Guide 中调用 `initStart(...)`
4. 通过 Guide 获取能力接口（Operations）进行发包、连接、关闭
5. 调用 `destroyServer(...)` 释放资源

## 示例
以下示例可在 `src/test/java` 中找到完整实现：

### Demo 入口（最小可跑）
- `src/test/java/demo/DemoTcpServer.java`
- `src/test/java/demo/DemoTcpClient.java`
- `src/test/java/demo/DemoHttpServer.java`
- `src/test/java/demo/DemoHttpClient.java`
- `src/test/java/demo/DemoUdp.java`
- `src/test/java/demo/DemoNoNetwork.java`

### TCP 服务端
参考：`src/test/java/tcp/server/NettyTcpServerStart.java`
```java
NettyLink link = new NettyLink()
    .addServerPort(5577)
    .addBootstrapOption(ChannelOption.TCP_NODELAY, true)
    .logMerge(new LogMerge(LogLevel.INFO))
    .addUnpack(new Unpacking(1024, ";"))
    .openDefaultChannelStatus()
    .addReadHandler(StringCusReader.class)
    .addWriteHandler(StringCusWriter.class);

NettyTcpServerGuide.initStart(pe, NettyType.S_TCP, executor, link);
NettyTcpServerOperations ops = NettyTcpServerGuide.tcpServerOperations(pe);
```

### TCP 客户端
参考：`src/test/java/tcp/client/NettyTcpClientStart.java`
```java
NettyTcpClientGuide.initStart(pe, NettyType.C_TCP, executor, link);
NettyTcpClientOperations ops = NettyTcpClientGuide.tcpClientOperations(pe);
ops.connectSync(new NetAddress("127.0.0.1", 6677));
```

### HTTP 服务端
参考：`src/test/java/http/server/NettyHttpServerStart.java`
```java
NettyLink link = new NettyLink()
    .addServerPort(8877)
    .addBeforePipeline(ch -> {
        ch.pipeline().addLast(new HttpServerCodec())
            .addLast(new HttpObjectAggregator(1024 * 1024));
        return true;
    })
    .openDefaultChannelStatus()
    .addReadHandler(StringCusHttpServerReader.class)
    .addWriteHandler(StringCusHttpServerWriter.class);

NettyHttpServerGuide.initStart(pe, NettyType.S_HTTP, executor, link);
```

### HTTP 客户端
参考：`src/test/java/http/client/NettyHttpClientStart.java`
```java
NettyHttpClientGuide.initStart(pe, NettyType.C_HTTP, executor, link);
NettyHttpClientOperations ops = NettyHttpClientGuide.httpClientOperations(pe);
ops.connectSync(new NetAddress("127.0.0.1", 8877));
ops.sendGetRequest("/test/ww", params, new NetAddress("127.0.0.1", 8877), null);
```

### UDP
参考：`src/test/java/udp/server/NettyUdpServerStart.java`
```java
NettyUdpServerGuide.initStart(pe, NettyType.UDP, executor, link);
NettyUdpOperations ops = NettyUdpServerGuide.udpServerOperations(pe);
```

### 无网络嵌入式通道
参考：`src/test/java/nonetworkchannel/NettyNoNetworkStart.java`
```java
NettyNoNetworkGuide.initStart(pe, NettyType.NO_NETWORK_CHANNEL, executor, link);
NettyNoNetworkGuide.noNetworkChannelOperations(pe)
    .distributeOutInstruct(new NettyWriteBo(pe, new NetAddress("127.0.0.1", 6677), "payload"));
```

## 扩展与定制
- 自定义处理器：
  - `LogicHandler`：连接管理、业务状态逻辑
  - `ReadHandler<T>`：读逻辑（`channelRead0`）
  - `PacketEncoder<T>`：写逻辑（编码输出）
- 自定义拆包：
  - 使用 `Unpacking` 支持定长、分隔符、长度域、或自定义 `ByteToMessageDecoder`
- 自定义线程池：
  - `NettyLink.addReadHandler(group, handlerClass)`

## 生命周期与资源释放
- 服务启动由 `NettyGuideAbstract.initStart(...)` 触发
- 服务关闭建议统一调用 `NettyGuideAbstract.destroyServer(...)`
- `VariableChannelCache` 会在销毁时关闭并清理通道

## 构建方式
```bash
mvn -DskipTests package
```

## 常见注意点
- TCP/HTTP 默认支持 `openDefaultChannelStatus` 管理通道状态，UDP/无网络模式不生效。
- 若不启用默认状态管理器，请自行维护通道缓存与资源释放。
- HTTP 使用时需在 `beforePipelines` 中加入 HTTP 编解码器。
