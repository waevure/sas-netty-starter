package com.sas.sasnettystarter.netty;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.sas.sasnettystarter.netty.exception.NettyChannelException;
import com.sas.sasnettystarter.netty.exception.NettyServerException;
import com.sas.sasnettystarter.netty.handle.*;
import com.sas.sasnettystarter.netty.log.LogMerge;
import com.sas.sasnettystarter.netty.log.LoggingHandler;
import com.sas.sasnettystarter.netty.ops.core.NettyServerBaseContext;
import com.sas.sasnettystarter.netty.ops.embedded.NettyNoNetworkChannel;
import com.sas.sasnettystarter.netty.ops.http.NettyHttpClient;
import com.sas.sasnettystarter.netty.ops.http.NettyHttpServer;
import com.sas.sasnettystarter.netty.ops.tcp.NettyTcpClient;
import com.sas.sasnettystarter.netty.ops.tcp.NettyTcpServer;
import com.sas.sasnettystarter.netty.ops.udp.NettyUdpServer;
import com.sas.sasnettystarter.netty.unpack.Unpacking;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 构建netty环境
 *
 * @author WQY
 * @version 1.0
 * @date 2023/12/3 09:41
 */
@Slf4j
@Getter
public class NettySupport extends PeBo {

    /**
     * netty类型
     */
    private NettyType nettyType;
    /**
     * 构建该服务的线程池
     */
    private ThreadPoolExecutor executor;
    /**
     * netty构建信息
     */
    private NettyLink nettyLink;

    /**
     * Netty模块
     * 对Netty应用能力的抽象
     * NettyProjectContext：定义项目环境等Netty基础信息
     * NettyServerBaseContext：定义Netty的环境信息系。
     *
     */
    private NettyServerBaseContext nettyServerContext;

    public NettySupport(ProjectAbstract pe, NettyType nettyType, NettyLink nettyLink) {
        super(pe);
        this.nettyType = nettyType;
        this.nettyLink = nettyLink;
    }

    public NettySupport(ProjectAbstract pe, NettyType nettyType, ThreadPoolExecutor executor, NettyLink nettyLink) {
        super(pe);
        this.nettyType = nettyType;
        this.executor = executor;
        this.nettyLink = nettyLink;
    }

    /**
     * 初始化NettyStartMods
     *
     * @param mods
     */
    public NettySupport initStartMods(NettyServerBaseContext mods) {
        this.nettyServerContext = mods;
        return this;
    }

    /**
     * 等待关闭
     */
    public void awaitSync() {
        this.nettyServerContext.awaitCloseSync();
    }

    /**
     * 等待关闭
     */
    public void awaitSync(Integer port) {
        this.nettyServerContext.awaitCloseSync(port);
    }

    /**
     * 构建器
     *
     * @return
     */
    public Builder builder() {
        return new Builder();
    }

    /**
     * 构建mods
     *
     * @return
     */
    public NettySupport buildNettyServerContext() throws Exception {
        //获取构建器
        NettySupport.Builder builder = this.builder();
        // 为空则添加默认的
        if (this.getNettyLink().getBootstrapOptions().isEmpty()) {
            //添加option
            builder.addOption(ChannelOption.TCP_NODELAY, true);
            builder.addOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 5);
        } else {
            for (ChannelOption option : this.getNettyLink().getBootstrapOptions().keySet()) {
                builder.addOption(option, this.getNettyLink().getBootstrapOptions().get(option));
            }
        }
        //添加日志
        builder.addLogMerge(this.getNettyLink().getLogMerge());
        //添加处理器
        builder.addStickyPackageUnpacking(this.getNettyLink().getDecoder());

        //对默认处理器进行配置
        if (this.getNettyLink().getOpenDefaultChannelStatusManager()) {
            if (this.getNettyType() != NettyType.UDP && this.getNettyType() != NettyType.NO_NETWORK_CHANNEL) {
                builder.openDefaultChannelStatus(this.getNettyLink().getDefaultFunctionRead());
                builder.addOnlineUserLogic(this.getNettyLink().getOnlineUserLogic());
                builder.addOfflineUserLogic(this.getNettyLink().getOfflineUserLogic());
            }
        }

        // 添加Pipeline前的处理器
        if (Objects.nonNull(this.getNettyLink().getBeforePipelines()) && !this.getNettyLink().getBeforePipelines().isEmpty()) {
            for (Function<Channel, Boolean> function : this.getNettyLink().getBeforePipelines()) {
                builder.addBeforePipeline(function);
            }
        }
        // 添加指令分发器，状态处理器
        for (Class<? extends LogicHandler> logicHandler : this.getNettyLink().getLogicHandlers()) {
            builder.addPipeline(logicHandler);
        }
        // 添加读指令
        for (Class<? extends ReadHandler> readHandler : this.getNettyLink().getReadHandlers()) {
            builder.addPipeline(readHandler);
        }
        // 添加写指令
        for (Class<? extends PacketEncoder> writeHandler : this.getNettyLink().getWriteHandlers()) {
            builder.addPipeline(writeHandler);
        }
        // 添加启动成功回调
        if (Objects.nonNull(this.getNettyLink().getStartSuccessCallback())) {
            // 添加成功回调
            builder.addStartSuccessCallback(this.getNettyLink().getStartSuccessCallback());
        }
        // 添加线程池组关系
        builder.addHandlerExecutorGroups(this.getNettyLink().getHandlerExecutorGroups());

        // 启动netty
        NettyServerBaseContext mods;
        if (NettyType.C_TCP == this.getNettyType()) {
            mods = builder.startTcpClient(this.getPe());
        } else if (NettyType.S_TCP == this.getNettyType()) {
            mods = builder.startTcpServer(this.getPe());
        } else if (NettyType.C_HTTP == this.getNettyType()) {
            mods = builder.startHttpClient(this.getPe());
        } else if (NettyType.S_HTTP == this.getNettyType()) {
            mods = builder.startHttpServer(this.getPe());
        } else if (NettyType.UDP == this.getNettyType()) {
            mods = builder.startUdpServer(this.getPe());
        } else if (NettyType.NO_NETWORK_CHANNEL == this.getNettyType()) {
            mods = builder.buildNoNetworkChannel(this.getPe());
        } else {
            throw new NettyServerException("未匹配到服务类型");
        }
        // 添加mods
        this.nettyServerContext = mods;
        return this;
    }

    /**
     * 销毁
     */
    public void destroy() throws Exception {
        // 销毁mod
        this.nettyServerContext.destroyServer();
        // 销毁线程池
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            executor.shutdownNow(); // 强制中断
        }
        // 检查线程池
        this.printExecutorStatus();
    }

    /**
     * 打印线程池信息
     */
    public void printExecutorStatus() {
        // 外部线程池状态
        if (executor != null) {
            log.info("线程池是否已关闭: {}", executor.isShutdown() ? "是" : "否");
            log.info("线程池是否已终止: {}", executor.isTerminated() ? "是" : "否");
        } else {
            log.info("线程池: 未初始化");
        }
    }

    class Builder {

        /**
         * options
         */
        private LinkedHashMap<ChannelOption<?>, Object> options = new LinkedHashMap<>();

        /**
         * 拆包器之前业务处理管道
         */
        private List<Class<? extends ChannelHandler>> pipelines = new ArrayList<>();

        /**
         * Pipelines前的处理器，在日志处理器之后构建，也可以在这里面放所有的逻辑处理器
         */
        private List<Function<Channel, Boolean>> beforePipelines = new ArrayList<>();

        /**
         * 粘包拆包信息
         */
        private Unpacking unpacking = null;

        /**
         * 默认状态管理器是否启用
         */
        private boolean defaultChannelStatus = false;
        /**
         * 默认处理器读数据回调
         */
        private TiFunction<ChannelHandlerContext, Object, ProjectAbstract, Object> defaultChannelReadFunc;
        /**
         * 上线逻辑处理器
         * openDefaultChannelStatusManager为true生效
         */
        private Class<? extends LogicHandler> onlineUserLogic;
        /**
         * 离线逻辑处理器
         * openDefaultChannelStatusManager为true生效
         */
        private Class<? extends LogicHandler> offlineUserLogic;


        /**
         * 日志
         * 如果为null则不打印日志
         */
        private LogMerge logMerge = null;

        /**
         * 启动成功回调
         */
        public Function<Channel, Boolean> startSuccessCallback;

        /**
         * 处理器线程池
         * 必须使用EventExecutorGroup
         */
        public Map<String, EventExecutorGroup> handlerExecutorGroups = new HashMap<>();

        /**
         * 添加option
         *
         * @param option
         * @param value
         * @param <T>
         * @return
         */
        public <T> Builder addOption(ChannelOption<T> option, T value) {
            this.options.put(option, value);
            return this;
        }

        /**
         * 添加拆包粘包器
         *
         * @param unpacking
         * @return
         */
        public Builder addStickyPackageUnpacking(Unpacking unpacking) {
            this.unpacking = unpacking;
            return this;
        }

        /**
         * 添加日志处理器
         *
         * @param logMerge
         * @return
         */
        public Builder addLogMerge(LogMerge logMerge) {
            this.logMerge = logMerge;
            return this;
        }

        /**
         * 添加后置处理器
         *
         * @param handler
         * @return
         */
        public Builder addPipeline(Class<? extends ChannelHandler> handler) {
            this.pipelines.add(handler);
            return this;
        }

        /**
         * 添加Pipeline前的处理器
         *
         * @param function
         * @return
         */
        public Builder addBeforePipeline(Function<Channel, Boolean> function) {
            this.beforePipelines.add(function);
            return this;
        }

        public Builder openDefaultChannelStatus(TiFunction<ChannelHandlerContext, Object, ProjectAbstract, Object> biFunction) {
            this.defaultChannelStatus = true;
            this.defaultChannelReadFunc = biFunction;
            return this;
        }

        /**
         * 添加上线处理器
         *
         * @param onlineLogic
         * @return
         */
        public Builder addOnlineUserLogic(Class<? extends LogicHandler> onlineLogic) {
            if (Objects.isNull(onlineLogic)) {
                this.onlineUserLogic = NettyClientOnlineHandler.class;
                return this;
            }
            this.onlineUserLogic = onlineLogic;
            return this;
        }

        /**
         * 添加离线处理器
         *
         * @param offlineLogic
         * @return
         */
        public Builder addOfflineUserLogic(Class<? extends LogicHandler> offlineLogic) {
            if (Objects.isNull(offlineLogic)) {
                this.offlineUserLogic = NettyClientOfflineHandler.class;
                return this;
            }
            this.offlineUserLogic = offlineLogic;
            return this;
        }


        /**
         * 添加成功回调
         *
         * @param function
         * @return
         */
        public Builder addStartSuccessCallback(Function<Channel, Boolean> function) {
            this.startSuccessCallback = function;
            return this;
        }

        /**
         * 添加处理器-线程池组关系
         *
         * @param handlerExecutorGroups
         * @return
         */
        public Builder addHandlerExecutorGroups(Map<String, EventExecutorGroup> handlerExecutorGroups) {
            this.handlerExecutorGroups = handlerExecutorGroups;
            return this;
        }

        /**
         * 合并参数-client
         *
         * @param pe 协议类型
         * @param b  引导
         */
        private void mergeParamBootstrap(ProjectAbstract pe, Bootstrap b, NettyType nettyType) {
            //添加netty-option
            for (ChannelOption option : this.options.keySet()) {
                b.option(option, this.options.get(option));
            }
            b.handler(new SocketChannelChannelInitializer(pe));
        }

        /**
         * 合并参数-server
         *
         * @param b 引导
         */
        private void mergeParamServerBootstrap(ServerBootstrap b, ProjectAbstract pe) {
            //添加netty-option
            for (ChannelOption option : this.options.keySet()) {
                b.childOption(option, this.options.get(option));
            }
            b.childHandler(new SocketChannelChannelInitializer(pe));

        }

        /**
         * 合并参数-server
         *
         * @param b 引导
         */
        private void mergeParamUdpServer(Bootstrap b, ProjectAbstract pe) {
            //添加netty-option
            for (ChannelOption option : this.options.keySet()) {
                b.option(option, this.options.get(option));
            }
            b.handler(new DatagramChannelInitializer(pe));
        }


        /**
         * 阻塞方法-启动netty客户端
         *
         * @return
         */
        public NettyServerBaseContext startTcpClient(ProjectAbstract pe) {
            /**配置客户端 NIO 线程组/池*/
            EventLoopGroup group = new NioEventLoopGroup();
            /**Bootstrap 与 ServerBootstrap 都继承(extends)于 AbstractBootstrap
             * 创建客户端辅助启动类,并对其配置,与服务器稍微不同，这里的 Channel 设置为 NioSocketChannel
             * 然后为其添加 Handler，这里直接使用匿名内部类，实现 initChannel 方法
             * 作用是当创建 NioSocketChannel 成功后，在进行初始化时,将它的ChannelHandler设置到ChannelPipeline中，用于处理网络I/O事件*/
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class);
            // 合并参数
            mergeParamBootstrap(pe, b, NettyType.C_TCP);
            log.info("client启动完成，构建NettyStartMods:{}", pe.toString());
            return new NettyTcpClient(pe, NettyType.C_TCP, b, group, this.startSuccessCallback);
        }

        /**
         * 启动http-netty客户端
         *
         * @return
         */
        public NettyServerBaseContext startHttpClient(ProjectAbstract pe) {
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class);
            // 合并参数
            mergeParamBootstrap(pe, b, NettyType.C_HTTP);
            return new NettyHttpClient(pe, NettyType.C_HTTP, b, group, this.startSuccessCallback);
        }

        /**
         * 启动http-netty客户端
         *
         * @return
         */
        public NettyServerBaseContext startHttpServer(ProjectAbstract pe) {
            // boss 负责接收连接，worker 负责处理 IO
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            // 创建引导类
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
            // 合并参数
            this.mergeParamServerBootstrap(b, pe);
            return new NettyHttpServer(pe, NettyType.S_HTTP, b, bossGroup, workerGroup);
        }

        /**
         * 启动netty服务端
         *
         * @return
         */
        public NettyServerBaseContext startTcpServer(ProjectAbstract pe) {
            // 构建boss-worker
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            // 创建引导类
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
            // 合并参数
            this.mergeParamServerBootstrap(b, pe);
            return new NettyTcpServer(pe, NettyType.S_TCP, b, bossGroup, workerGroup);
        }

        /**
         * 启动udp-netty服务
         *
         * @return
         */
        public NettyServerBaseContext startUdpServer(ProjectAbstract pe) {
            // 构建boos-worker
            EventLoopGroup group = new NioEventLoopGroup();
            // 创建引导类
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class);
            // 服务参数处理
            this.mergeParamUdpServer(b, pe);
            // 构建udp模块
            return new NettyUdpServer(pe, NettyType.UDP, b, group);
        }

        /**
         * 构建无网络责任链
         *
         * @return
         */
        public NettyServerBaseContext buildNoNetworkChannel(ProjectAbstract pe) throws Exception {
            // 创建一个嵌入式 Channel
            EmbeddedChannel channel = new EmbeddedChannel();
            // 初始化channel责任链
            this.initChannelSupport(pe, channel);
            // 返回mods
            return new NettyNoNetworkChannel(channel, pe, NettyType.NO_NETWORK_CHANNEL);
        }

        /**
         * 初始化责任链
         *
         * @param pe
         * @param channel
         * @throws Exception
         */
        private void initChannelSupport(ProjectAbstract pe, Channel channel) throws Exception {
            try {
                //日志添加到首位
                if (ObjectUtil.isNotNull(logMerge)) {
                    LoggingHandler loggingHandler = new LoggingHandler(pe, logMerge.getLogLevel());
                    // 添加回调处理器
                    if (Objects.nonNull(logMerge.getLoggingCallBackFunc())) {
                        loggingHandler.addStrLogCall(logMerge.getLoggingCallBackFunc());
                    }
                    channel.pipeline().addLast(loggingHandler);
                }
                // beforePipelines在除日志之前执行
                if (Objects.nonNull(beforePipelines)) {
                    // 执行beforePipelines
                    for (Function<Channel, Boolean> f : beforePipelines) {
                        f.apply(channel);
                    }
                }
                //拆包次之
                if (ObjectUtil.isNotNull(unpacking)) {
                    channel.pipeline().addLast(unpacking.buildHandler());
                }
                // 默认状态管理器是否启用
                if (Objects.nonNull(defaultChannelStatus) && defaultChannelStatus) {
                    channel.pipeline().addLast(new ChannelStatusManager(defaultChannelReadFunc, nettyServerContext.getVariableChannelCache(), pe));
                    // 添加上线处理器
                    channel.pipeline().addLast(this.onlineUserLogic.getDeclaredConstructor().newInstance());
                    // 添加离线处理器
                    channel.pipeline().addLast(this.offlineUserLogic.getDeclaredConstructor().newInstance());
                }

                //如果为null则创建默认的
                if (CollectionUtil.isEmpty(pipelines)) {
                    channel.pipeline().addLast(new DefaultServerHandler());
                    return;
                }
                //按顺序添加处理器
                for (Class<? extends ChannelHandler> handler : pipelines) {

                    // 获取该处理器对应的线程池组
                    EventExecutorGroup group = this.handlerExecutorGroups.get(handler.getName());
                    if (Objects.nonNull(group)) {
                        //创建管道处理实例
                        channel.pipeline().addLast(group, handler.getDeclaredConstructor().newInstance());
                    } else {
                        //创建管道处理实例
                        channel.pipeline().addLast(handler.getDeclaredConstructor().newInstance());
                    }
                }
            } catch (Exception e) {
                log.error("netty channel构建异常", e);
                throw new NettyChannelException(e.getMessage());
            }
        }

        /**
         * 通道初始化
         */
        private class SocketChannelChannelInitializer extends ChannelInitializer<SocketChannel> {
            private final ProjectAbstract pe;

            public SocketChannelChannelInitializer(ProjectAbstract pe) {
                this.pe = pe;
            }

            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                // 初始化责任链
                Builder.this.initChannelSupport(this.pe, channel);
            }
        }

        /**
         * udp通道初始化
         */
        private class DatagramChannelInitializer extends ChannelInitializer<NioDatagramChannel> {
            private final ProjectAbstract pe;

            public DatagramChannelInitializer(ProjectAbstract pe) {
                this.pe = pe;
            }

            @Override
            protected void initChannel(NioDatagramChannel channel) throws Exception {
                // 初始化责任链
                Builder.this.initChannelSupport(this.pe, channel);
            }
        }
    }

}
