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
        super(pe, nettyType);
        this.bootstrap = bootstrap;
        this.workerGroup = workerGroup;
    }

    /**
     * 等待退出
     */
    @Override
    public void awaitSync(Integer port) {
        try {
            log.info("[{}]-UDP-准备启动中:{}", this.getPe().toStr(), port);
            ChannelFuture f = this.bootstrap.bind(port).sync();
            log.info("[{}]-UDP-启动完成:{}", this.getPe().toStr(), port);
            this.channelFuture = f;
            // 进行回调
            if (Objects.nonNull(this.startSuccessCallback)) {
                this.startSuccessCallback.apply(f.channel());
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
    public void awaitSync() {
        this.awaitSync(0);
    }

    @Override
    public void distributeInstruct(NettyWriteBo writeData) {
        this.channelFuture.channel().writeAndFlush(writeData);
    }

    @Override
    public boolean destroyServer() {
        log.info("Netty-UDP服务开始销毁:{}", this);
        this.channelFuture.channel().close();
        if (Objects.nonNull(this.workerGroup)) {
            this.workerGroup.shutdownGracefully();
        }
        // 销毁variable
        this.variable.destroy(this.getPe());
        log.info("Netty-UDP服务销毁完成:{}", this);
        return true;
    }
}
