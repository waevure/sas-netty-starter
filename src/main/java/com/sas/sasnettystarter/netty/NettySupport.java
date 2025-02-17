package com.sas.sasnettystarter.netty;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.sas.sasnettystarter.netty.handle.ChannelStatusHandler;
import com.sas.sasnettystarter.netty.handle.DefaultServerHandler;
import com.sas.sasnettystarter.netty.log.LogMerge;
import com.sas.sasnettystarter.netty.log.LoggingHandler;
import com.sas.sasnettystarter.netty.mods.*;
import com.sas.sasnettystarter.netty.unpack.Unpacking;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 构建netty环境
 *
 * @author WQY
 * @version 1.0
 * @date 2023/12/3 09:41
 */
@Slf4j
public class NettySupport {
    /**
     * netty模块
     */
    private NettyMods mods;

    /**
     * 初始化NettyStartMods
     *
     * @param mods
     */
    public NettySupport initStartMods(NettyMods mods) {
        this.mods = mods;
        return this;
    }

    /**
     * 等待关闭
     */
    public void awaitSync() {
        this.mods.awaitSync();
    }

    /**
     * 等待关闭
     */
    public void awaitSync(Integer port) {
        this.mods.awaitSync(port);
    }

    public Builder builder() {
        return new Builder();
    }


    /**
     * netty能力模块
     *
     * @return
     */
    public NettyMods nettyMods() {
        return mods;
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
         * 日志
         * 如果为null则不打印日志
         */
        private LogMerge logMerge = null;

        /**
         * 启动成功回调
         */
        public Function<Channel, Boolean> startSuccessCallback;

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
         * 合并参数-client
         *
         * @param pe 协议类型
         * @param b  引导
         */
        private void mergeParamClient(ProjectAbstract pe, Bootstrap b, NettyType nettyType) {
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
        private void mergeParamServer(ServerBootstrap b, ProjectAbstract pe) {
            //添加netty-option
            for (ChannelOption option : this.options.keySet()) {
                b.childOption(option, this.options.get(option));
            }
            b.childHandler(new SocketChannelChannelInitializer(pe));

        }

        /**
         * 阻塞方法-启动netty客户端
         *
         * @return
         */
        public NettyMods startClient(ProjectAbstract pe) {
            /**配置客户端 NIO 线程组/池*/
            EventLoopGroup group = new NioEventLoopGroup();
            /**Bootstrap 与 ServerBootstrap 都继承(extends)于 AbstractBootstrap
             * 创建客户端辅助启动类,并对其配置,与服务器稍微不同，这里的 Channel 设置为 NioSocketChannel
             * 然后为其添加 Handler，这里直接使用匿名内部类，实现 initChannel 方法
             * 作用是当创建 NioSocketChannel 成功后，在进行初始化时,将它的ChannelHandler设置到ChannelPipeline中，用于处理网络I/O事件*/
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class);
            // 合并参数
            mergeParamClient(pe, b, NettyType.C);
            log.info("client启动完成，构建NettyStartMods:{}", pe.toString());
            return new NettyClientMods(b, group, this.startSuccessCallback);
        }

        /**
         * 阻塞方法-启动http-netty客户端
         *
         * @return
         */
        public NettyMods startHttpClient(ProjectAbstract pe) {
            /**配置客户端 NIO 线程组/池*/
            EventLoopGroup group = new NioEventLoopGroup();
            /**Bootstrap 与 ServerBootstrap 都继承(extends)于 AbstractBootstrap
             * 创建客户端辅助启动类,并对其配置,与服务器稍微不同，这里的 Channel 设置为 NioSocketChannel
             * 然后为其添加 Handler，这里直接使用匿名内部类，实现 initChannel 方法
             * 作用是当创建 NioSocketChannel 成功后，在进行初始化时,将它的ChannelHandler设置到ChannelPipeline中，用于处理网络I/O事件*/
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class);
            // 合并参数
            mergeParamClient(pe, b, NettyType.C);
            log.info("http-client启动完成，构建NettyHttpClientMods:{}", pe.toString());
            NettyHttpClientMods clientMods = new NettyHttpClientMods(b, group, this.startSuccessCallback);
            return clientMods;
        }

        /**
         * 启动netty服务端
         *
         * @return
         */
        public NettyMods startServer(ProjectAbstract pe) {
            // 构建boos-worker
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            // 创建引导类
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);

            this.mergeParamServer(b, pe);

            return new NettyServerMods(b, bossGroup, workerGroup);
        }

        /**
         * 构建无网络责任链
         *
         * @return
         */
        public NettyMods buildNoNetworkChannel(ProjectAbstract pe) throws Exception {
            // 创建一个嵌入式 Channel
            EmbeddedChannel channel = new EmbeddedChannel();
            // 设置mods
            NettySupport.this.initStartMods(new NettyNoNetworkChannelMods(channel, NettyType.NO_NETWORK_CHANNEL));
            // 初始化channel责任链
            this.initChannelSupport(pe, channel);
            // 返回mods
            return NettySupport.this.nettyMods();
        }

        /**
         * 初始化责任链
         *
         * @param pe
         * @param channel
         * @throws Exception
         */
        private void initChannelSupport(ProjectAbstract pe, Channel channel) throws Exception {
            //日志添加到首位
            if (ObjectUtil.isNotNull(logMerge)) {
                LoggingHandler loggingHandler = new LoggingHandler(logMerge.getLogLevel());
                // 添加回调处理器
                if (Objects.nonNull(logMerge.getLoggingCallBackFunc())) {
                    loggingHandler.addStrLogCall(logMerge.getLoggingCallBackFunc());
                }
                channel.pipeline().addLast(loggingHandler);
            }
            // beforePipelines在除日志之前执行
            if (Objects.nonNull(beforePipelines)) {
                // 执行beforePipelines
                beforePipelines.stream().forEach(b -> b.apply(channel));
            }
            //拆包次之
            if (ObjectUtil.isNotNull(unpacking)) {
                channel.pipeline().addLast(unpacking.buildHandler());
            }
            // 默认状态管理器是否启用
            if (Objects.nonNull(defaultChannelStatus) && defaultChannelStatus) {
                channel.pipeline().addLast(new ChannelStatusHandler(defaultChannelReadFunc, mods.variable(), pe));
            }
            //如果为null则创建默认的
            if (CollectionUtil.isEmpty(pipelines)) {
                channel.pipeline().addLast(new DefaultServerHandler());
                return;
            }
            //按顺序添加处理器
            for (Class<? extends ChannelHandler> handler : pipelines) {
                try {
                    //创建管道处理实例
                    channel.pipeline().addLast(handler.newInstance());
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
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
    }

}
