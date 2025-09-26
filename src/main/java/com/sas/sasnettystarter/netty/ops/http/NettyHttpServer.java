package com.sas.sasnettystarter.netty.ops.http;

import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.ops.core.NettyServerBaseContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @ClassName: NettyHttpClient
 * @Description: netty-http-client功能
 * @Author: Wqy
 * @Date: 2024-10-29 10:30
 * @Version: 1.0
 **/
@Slf4j
public class NettyHttpServer extends NettyServerBaseContext implements NettyHttpServerOperations {

    /**
     * @param nettyType   类型
     * @param bootstrap   netty引导类
     * @param bossGroup   boss组
     * @param workerGroup worker组
     */
    public NettyHttpServer(ProjectAbstract pe, NettyType nettyType, ServerBootstrap bootstrap, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        super(pe, nettyType, bootstrap, bossGroup, workerGroup);
    }

    /**
     * 等待退出
     */
    @Override
    public void awaitCloseSync(Integer port) {
        try {
            log.info("{}-HTTP-服务端-准备启动中:{}", this.getPe().toStr(), port);
            ChannelFuture f = this.getServerBootstrap().bind(port).sync();
            log.info("{}-HTTP-服务端-启动完成:{}", this.getPe().toStr(), port);
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
            this.getBossGroup().shutdownGracefully();
            if (Objects.nonNull(this.getWorkerGroup())) {
                this.getWorkerGroup().shutdownGracefully();
            }
        }
    }

}
