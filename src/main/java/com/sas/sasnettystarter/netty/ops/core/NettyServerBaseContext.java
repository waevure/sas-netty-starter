package com.sas.sasnettystarter.netty.ops.core;

import cn.hutool.core.util.ObjectUtil;
import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.cache.VariableChannelCache;
import com.sas.sasnettystarter.netty.exception.NettyServiceException;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @ClassName: NettyServerBaseContext
 * @Description: 服务端
 * @Author: Wqy
 * @Date: 2024-05-31 15:17
 * @Version: 1.0
 **/
@Getter
@Slf4j
public abstract class NettyServerBaseContext extends NettyProjectContext {

    /**
     * netty引导类
     * 用途：客户端 或 UDP 场景
     * channel层级：只有一个 Channel（自己就是客户端的连接/UDP 通道）
     */
    private Bootstrap bootstrap;

    /**
     * netty引导类
     * 用途：服务端（TCP监听）
     * channel层级：有 两级 Channel：
     * ① parent channel → NioServerSocketChannel（负责监听端口，接收新连接）
     * ② child channel → NioSocketChannel（每个客户端连接对应一个）
     */
    private ServerBootstrap serverBootstrap;

    /**
     * boss组
     */
    private EventLoopGroup bossGroup;

    /**
     * worker组
     */
    private EventLoopGroup workerGroup;

    /**
     * 外部使用的通道缓存，一般key为设备唯一编码。其实就是存一些执行力注册的设备
     */
    private Map<String, Channel> keyMap = new ConcurrentHashMap<>();

    /**
     * 服务通道构建结果
     */
    private ChannelFuture channelFuture;

    /**
     * 所有客户端连接服务端的结果
     */
    private Map<String, ChannelFuture> channelFutures = new ConcurrentHashMap<>();

    /**
     * 缓存信息值
     */
    private VariableChannelCache variableChannelCache;

    /**
     * 启动成功回调
     */
    private Function<Channel, Boolean> startSuccessCallback;

    public NettyServerBaseContext(ProjectAbstract pe) {
        this.variableChannelCache = new VariableChannelCache(pe);
    }

    public NettyServerBaseContext(ProjectAbstract pe, NettyType nettyType) {
        super(pe, nettyType);
        this.variableChannelCache = new VariableChannelCache(pe);
    }

    /**
     * 构建 ServerBoostrap
     *
     * @param pe
     * @param nettyType
     * @param serverBootstrap
     * @param bossGroup
     * @param workerGroup
     */
    public NettyServerBaseContext(ProjectAbstract pe, NettyType nettyType, ServerBootstrap serverBootstrap, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        super(pe, nettyType);
        this.serverBootstrap = serverBootstrap;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.variableChannelCache = new VariableChannelCache(pe);
    }

    /**
     * 构建 ServerBoostrap
     *
     * @param pe
     * @param nettyType
     * @param serverBootstrap
     * @param bossGroup
     * @param workerGroup
     */
    public NettyServerBaseContext(ProjectAbstract pe, NettyType nettyType, ServerBootstrap serverBootstrap, EventLoopGroup bossGroup, EventLoopGroup workerGroup, Function<Channel, Boolean> startSuccessCallback) {
        super(pe, nettyType);
        this.serverBootstrap = serverBootstrap;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.startSuccessCallback = startSuccessCallback;
        this.variableChannelCache = new VariableChannelCache(pe);
    }

    /**
     * 构建 Bootstrap
     * @param pe
     * @param nettyType
     * @param bootstrap
     * @param bossGroup
     */
    public NettyServerBaseContext(ProjectAbstract pe, NettyType nettyType, Bootstrap bootstrap, EventLoopGroup bossGroup) {
        super(pe, nettyType);
        this.bootstrap = bootstrap;
        this.bossGroup = bossGroup;
        this.variableChannelCache = new VariableChannelCache(pe);
    }

    /**
     * 构建 Bootstrap
     * @param pe
     * @param nettyType
     * @param bootstrap
     * @param bossGroup
     */
    public NettyServerBaseContext(ProjectAbstract pe, NettyType nettyType, Bootstrap bootstrap, EventLoopGroup bossGroup, Function<Channel, Boolean> startSuccessCallback) {
        super(pe, nettyType);
        this.bootstrap = bootstrap;
        this.bossGroup = bossGroup;
        this.startSuccessCallback = startSuccessCallback;
        this.variableChannelCache = new VariableChannelCache(pe);
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public abstract void awaitCloseSync(Integer port);

    /**
     * 同步等待关闭
     */
    public void awaitCloseSync() {
        this.awaitCloseSync(0);
    }

    public boolean destroyServer() {
        return true;
    }

    public void printNettyServerBootstrapGroupStatus() {
        // 项目信息
        String peStr = this.getPe().toStr();
        log.info("===== {}-ServerBootstrap服务销毁状态检查 =====", peStr);
        // 通道状态
        if (channelFuture != null && channelFuture.channel() != null) {
            log.info("{}-通道是否打开: {}", peStr, channelFuture.channel().isOpen() ? "是" : "否");
            log.info("{}-通道是否激活: {}", peStr, channelFuture.channel().isActive() ? "是" : "否");
        } else {
            log.info("{}-通道: 未初始化");
        }
        this.printNettyBootstrapGroupStatusCommon();
        // WorkerGroup 状态
        if (workerGroup != null) {
            log.info("{}-WorkerGroup 是否正在关闭: {}", peStr, workerGroup.isShuttingDown() ? "是" : "否");
            log.info("{}-WorkerGroup 是否已终止: {}", peStr, workerGroup.isTerminated() ? "是" : "否");
        } else {
            log.info("{}-WorkerGroup: 未初始化", peStr);
        }
    }

    public void printNettyBootstrapGroupStatus() {
        // 项目信息
        String peStr = this.getPe().toStr();
        log.info("===== {}-Bootstrap销毁状态检查 =====", peStr);
        this.printNettyBootstrapGroupStatusCommon();
    }

    /**
     * 打印Boostrap状态
     */
    private void printNettyBootstrapGroupStatusCommon() {
        // 项目信息
        String peStr = this.getPe().toStr();
        // BossGroup 状态
        if (bossGroup != null) {
            log.info("{}-BossGroup 是否正在关闭: {}", peStr, bossGroup.isShuttingDown() ? "是" : "否");
            log.info("{}-BossGroup 是否已终止: {}", peStr, bossGroup.isTerminated() ? "是" : "否");
        } else {
            log.info("{}-BossGroup: 未初始化", peStr);
        }
    }

    /**
     * 传入WriteBo对象
     * 放入通道链路
     *
     * @param writeBo
     * @return
     */
    public <T extends NettyWriteBo> void writeAndFlush(T writeBo) {
        Channel channel = this.variableChannelCache.getCtx(writeBo.ipPortStr());
        if (Objects.nonNull(channel)) {
            channel.writeAndFlush(writeBo);
        } else {
            log.error("{}-发送数据失败,链路不存在-{}", writeBo.ipPortStr(), writeBo.getMsg());
            throw new NettyServiceException("发送数据失败,链路不存在");
        }
    }

    /**
     * 关闭连接
     *
     * @param netAddress
     */
    public void closeConnect(NetAddress netAddress) {
        Channel channel = this.variableChannelCache.getCtx(netAddress.ipPort());
        channel.close();
    }

    /**
     * 通道状态
     *
     * @param netAddress
     * @return
     */
    public Boolean channelActive(NetAddress netAddress) {
        Channel channel = this.variableChannelCache.getCtx(netAddress.ipPort());
        if (ObjectUtil.isNotNull(channel)) {
            return channel.isActive();
        }
        return false;
    }
}
