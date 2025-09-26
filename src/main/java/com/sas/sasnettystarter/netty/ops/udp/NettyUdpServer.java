package com.sas.sasnettystarter.netty.ops.udp;

import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.ops.core.NettyServerBaseContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @ClassName: NettyUdpServer
 * @Description: netty-udp-client功能
 * @Author: Wqy
 * @Date: 2025-09-24 10:30
 * @Version: 1.0
 **/
@Slf4j
public class NettyUdpServer extends NettyServerBaseContext implements NettyUdpOperations {

    public NettyUdpServer(ProjectAbstract pe, NettyType nettyType, Bootstrap bootstrap, EventLoopGroup workerGroup) {
        super(pe, nettyType, bootstrap, workerGroup);
    }

    /**
     * 等待退出
     */
    @Override
    public void awaitCloseSync(Integer port) {
        try {
            log.info("[{}]-UDP-准备启动中:{}", this.getPe().toStr(), port);
            ChannelFuture f = this.getBootstrap().bind(port).sync();
            log.info("[{}]-UDP-启动完成:{}", this.getPe().toStr(), port);
            this.setChannelFuture(f);;
            // 进行回调
            if (Objects.nonNull(this.getStartSuccessCallback())) {
                this.getStartSuccessCallback().apply(f.channel());
            }
            //使用f.channel().closeFuture().sync()方法进行阻塞,等待服务端链路关闭之后main函数才退出。
            f.channel().closeFuture().sync();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            /**优雅退出，释放NIO线程组*/
            if (Objects.nonNull(this.getWorkerGroup())) {
                this.getWorkerGroup().shutdownGracefully();
            }
        }
    }

    @Override
    public void awaitCloseSync() {
        this.awaitCloseSync(0);
    }

    @Override
    public void distributeInstruct(NettyWriteBo writeData) {
        this.getChannelFuture().channel().writeAndFlush(writeData);
    }

    @Override
    public boolean destroyServer() {
        log.info("Netty-UDP服务开始销毁:{}", this);
        this.getChannelFuture().channel().close();
        if (Objects.nonNull(this.getWorkerGroup())) {
            this.getWorkerGroup().shutdownGracefully();
        }
        // 销毁variable
        this.getVariable().destroy(this.getPe());
        log.info("Netty-UDP服务销毁完成:{}", this);
        return true;
    }
}
