package com.sas.sasnettystarter.netty.ops.core;

import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: NettyServerBaseContext
 * @Description: 服务端
 * @Author: Wqy
 * @Date: 2024-05-31 15:17
 * @Version: 1.0
 **/
@Data
@Slf4j
public class NettyServerBaseContext extends NettyProjectContext {

    /**
     * netty引导类
     * 用途：客户端 或 UDP 场景
     * channel层级：只有一个 Channel（自己就是客户端的连接/UDP 通道）
     */
    public Bootstrap bootstrap;

    /**
     * netty引导类
     * 用途：服务端（TCP监听）
     * channel层级：有 两级 Channel：
     * ① parent channel → NioServerSocketChannel（负责监听端口，接收新连接）
     * ② child channel → NioSocketChannel（每个客户端连接对应一个）
     */
    public ServerBootstrap serverBootstrap;

    /**
     * boss组
     */
    public EventLoopGroup bossGroup;

    /**
     * worker组
     */
    public EventLoopGroup workerGroup;

    /**
     * 外部使用的通道缓存，一般key为设备唯一编码。其实就是存一些执行力注册的设备
     */
    public Map<String, ChannelHandlerContext> keyMap = new HashMap<>();

    /**
     * 服务通道构建结果
     */
    public ChannelFuture channelFuture;

    /**
     * 所有客户端连接服务端的结果
     */
    public Map<String, ChannelFuture> channelFutures = new HashMap<>();

    public NettyServerBaseContext() {
    }

    public NettyServerBaseContext(ProjectAbstract pe, NettyType nettyType) {
        super(pe, nettyType);
    }

    @Override
    public boolean destroyServer() {
        return true;
    }

    @Override
    public void printNettyServerBootstrapGroupStatus() {
        log.info("===== {}-ServerBootstrap服务销毁状态检查 =====", this.getPe().toStr());
        // 通道状态
        if (channelFuture != null && channelFuture.channel() != null) {
            log.info("{}-通道是否打开: {}", this.pe.toStr(), channelFuture.channel().isOpen() ? "是" : "否");
            log.info("{}-通道是否激活: {}", this.pe.toStr(), channelFuture.channel().isActive() ? "是" : "否");
        } else {
            log.info("{}-通道: 未初始化");
        }
        this.printNettyBootstrapGroupStatusCommon();
        // WorkerGroup 状态
        if (workerGroup != null) {
            log.info("{}-WorkerGroup 是否正在关闭: {}", this.pe.toStr(), workerGroup.isShuttingDown() ? "是" : "否");
            log.info("{}-WorkerGroup 是否已终止: {}", this.pe.toStr(), workerGroup.isTerminated() ? "是" : "否");
        } else {
            log.info("{}-WorkerGroup: 未初始化", this.pe.toStr());
        }
    }

    @Override
    public void printNettyBootstrapGroupStatus() {
        log.info("===== {}-Bootstrap销毁状态检查 =====", this.getPe().toStr());
        this.printNettyBootstrapGroupStatusCommon();
    }

    /**
     * 打印Boostrap状态
     */
    private void printNettyBootstrapGroupStatusCommon() {
        // BossGroup 状态
        if (bossGroup != null) {
            log.info("{}-BossGroup 是否正在关闭: {}", this.pe.toStr(), bossGroup.isShuttingDown() ? "是" : "否");
            log.info("{}-BossGroup 是否已终止: {}", this.pe.toStr(), bossGroup.isTerminated() ? "是" : "否");
        } else {
            log.info("{}-BossGroup: 未初始化", this.pe.toStr());
        }
    }
}
