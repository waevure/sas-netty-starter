package com.sas.sasnettystarter.netty.mods;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.constant.NettyConstant;
import com.sas.sasnettystarter.netty.exception.NettyLinkException;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.mods.operation.NettyTcpClientOperations;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Function;

/**
 * @ClassName: NettyTcpClientMods
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 15:21
 * @Version: 1.0
 **/
@Slf4j
public class NettyTcpClientMods extends NettyServerMods implements NettyTcpClientOperations {

    public NettyTcpClientMods(ProjectAbstract pe, NettyType nettyType, Bootstrap bootstrap, EventLoopGroup group, Function<Channel, Boolean> startSuccessCallback) {
        super(pe, nettyType);
        this.bootstrap = bootstrap;
        this.bossGroup = group;
        this.startSuccessCallback = startSuccessCallback;
    }

    @Override
    public void awaitSync(Integer port) {
        try {
            // 进行回调
            if (Objects.nonNull(this.startSuccessCallback)) {
                this.startSuccessCallback.apply(null);
            }
            // 等待所有客户端链路关闭
            this.getBootstrap().config().group().terminationFuture().sync();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // 优雅退出，释放NIO线程组
            this.getBossGroup().shutdownGracefully().syncUninterruptibly();
            this.printNettyBootstrapGroupStatus();
        }
    }

    @Override
    public void awaitSync() {
        this.awaitSync(0);
    }

    @Override
    public Boolean connectSync(IpPortAddress ipPortAddress) {
        // 进行连接
        ChannelFuture future = this.getBootstrap().connect(ipPortAddress.getIp(), ipPortAddress.getPort());
        // 等待
        try {
            future.sync();
            if (future.isSuccess()) {
                log.info("[{}]-{}-TCP-客户端-同步连接成功", this.getPe().toStr(), ipPortAddress.ipPort());
                this.channelFutures.put(ipPortAddress.ipPort(), future);
                return true;
            } else {
                future.cause().printStackTrace();
                log.info("[{}]-{}-TCP-客户端-同步连接失败", this.getPe().toStr(), ipPortAddress.ipPort());
                this.channelFutures.put(ipPortAddress.ipPort(), future);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NettyLinkException(NettyConstant.CONN_ERR_MSG);
        }
    }

    @Override
    public void distributeInstruct(String key, NettyWriteBo writeData) {
        ChannelFuture future = this.channelFutures.get(key);
        if (Objects.isNull(future)) {
            throw new NettyLinkException(String.format("[%s:%s]-TCP-客户端-链路未注册:%s", this.pe.getProjectCode(), pe.getProjectName(), key));
        }
        if (!future.channel().isActive()) {
            throw new NettyLinkException(String.format("[%s:%s]-TCP-客户端-链路未激活:%s", this.pe.getProjectCode(), pe.getProjectName(), key));
        }
        future.channel().writeAndFlush(writeData);
    }

    @Override
    public boolean destroyServer() {
        try {
            // 打印日志：开始销毁
            log.info("{}-TCP-客户端-开始销毁", this.getPe().toStr());
            // 关闭所有服务连接
            for (String key : this.channelFutures.keySet()) {
                // 获取future
                ChannelFuture future = this.channelFutures.get(key);
                if (future.channel().isOpen()) {
                    future.channel().close().syncUninterruptibly();
                    log.info("{}-TCP-客户端-关闭连接:{}", this.getPe().toStr(), key);
                }
            }
            // 关闭bossGroup
            this.getBossGroup().shutdownGracefully().syncUninterruptibly();
            // 关闭所有客户端连接
            this.getVariable().destroy(this.getPe());
            // 打印日志：销毁完成
            log.info("{}-TCP-客户端-销毁完成", this.pe.toStr());
            return true;
        } catch (Exception ex) {
            log.error("{} - TCP 客户端销毁失败: {}", this.pe.toStr(), ex.getMessage(), ex);
            return false;
        }
    }
}
