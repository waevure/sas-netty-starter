package com.sas.sasnettystarter.netty.ops.tcp;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.constant.NettyConstant;
import com.sas.sasnettystarter.netty.exception.NettyLinkException;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.ops.core.NettyServerBaseContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Function;

/**
 * @ClassName: NettyTcpClient
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 15:21
 * @Version: 1.0
 **/
@Slf4j
public class NettyTcpClient extends NettyServerBaseContext implements NettyTcpClientOperations {

    public NettyTcpClient(ProjectAbstract pe, NettyType nettyType, Bootstrap bootstrap, EventLoopGroup group, Function<Channel, Boolean> startSuccessCallback) {
        super(pe, nettyType, bootstrap, group, startSuccessCallback);
    }

    @Override
    public void awaitCloseSync(Integer port) {
        try {
            // 进行回调
            if (Objects.nonNull(this.getStartSuccessCallback())) {
                this.getStartSuccessCallback().apply(null);
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
    public void awaitCloseSync() {
        this.awaitCloseSync(0);
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
                // ⭐ 监听关闭事件，移除缓存
                future.channel().closeFuture().addListener((ChannelFutureListener) closeFuture -> {
                    log.info("[{}]-{}-TCP-客户端-连接已关闭，移除缓存", this.getPe().toStr(), ipPortAddress.ipPort());
                    this.getChannelFutures().remove(ipPortAddress.ipPort());
                });
                this.getChannelFutures().put(ipPortAddress.ipPort(), future);
                return true;
            } else {
                future.cause().printStackTrace();
                log.info("[{}]-{}-TCP-客户端-同步连接失败", this.getPe().toStr(), ipPortAddress.ipPort());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NettyLinkException(NettyConstant.CONN_ERR_MSG);
        }
    }

    @Override
    public void distributeInstruct(String key, NettyWriteBo writeData) {
        ChannelFuture future = this.getChannelFutures().get(key);
        if (Objects.isNull(future)) {
            throw new NettyLinkException(String.format("[%s:%s]-TCP-客户端-链路未注册:%s", this.getPe().getProjectCode(), getPe().getProjectName(), key));
        }
        if (!future.channel().isActive()) {
            throw new NettyLinkException(String.format("[%s:%s]-TCP-客户端-链路未激活:%s", this.getPe().getProjectCode(), getPe().getProjectName(), key));
        }
        future.channel().writeAndFlush(writeData);
    }

    @Override
    public boolean destroyServer() {
        try {
            // 打印日志：开始销毁
            log.info("{}-TCP-客户端-开始销毁", this.getPe().toStr());
            // 关闭所有服务连接
            for (String key : this.getChannelFutures().keySet()) {
                // 获取future
                ChannelFuture future = this.getChannelFutures().get(key);
                if (future.channel().isOpen()) {
                    future.channel().close().syncUninterruptibly();
                    log.info("{}-TCP-客户端-关闭连接:{}", this.getPe().toStr(), key);
                }
            }
            // 关闭bossGroup
            this.getBossGroup().shutdownGracefully().syncUninterruptibly();
            // 关闭所有客户端连接
            this.getVariableChannelCache().destroy(this.getPe());
            // 打印日志：销毁完成
            log.info("{}-TCP-客户端-销毁完成", this.getPe().toStr());
            return true;
        } catch (Exception ex) {
            log.error("{} - TCP 客户端销毁失败: {}", this.getPe().toStr(), ex.getMessage(), ex);
            return false;
        }
    }
}
