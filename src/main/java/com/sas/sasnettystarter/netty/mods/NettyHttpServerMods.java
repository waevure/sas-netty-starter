package com.sas.sasnettystarter.netty.mods;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.cache.Variable;
import com.sas.sasnettystarter.netty.mods.ab.NettyHttpClientAbility;
import com.sas.sasnettystarter.netty.mods.ab.NettyHttpServerAbility;
import com.sas.sasnettystarter.netty.utils.GsonUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Function;

/**
 * @ClassName: NettyHttpClientMods
 * @Description: netty-http-client功能
 * @Author: Wqy
 * @Date: 2024-10-29 10:30
 * @Version: 1.0
 **/
@Slf4j
public class NettyHttpServerMods extends NettyServerMods implements NettyHttpServerAbility {

    /**
     * @param nettyType   类型
     * @param bootstrap   netty引导类
     * @param bossGroup   boss组
     * @param workerGroup worker组
     */
    public NettyHttpServerMods(ProjectAbstract pe, NettyType nettyType, ServerBootstrap bootstrap, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        super(pe, nettyType);
        this.serverBootstrap = bootstrap;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        //初始化缓存信息
        this.variable = new Variable();
    }

    /**
     * 等待退出
     */
    @Override
    public void awaitSync(Integer port) {
        try {
            log.info("{}-HTTP-服务端-准备启动中:{}", this.getPe().toStr(), port);
            ChannelFuture f = this.serverBootstrap.bind(port).sync();
            log.info("{}-HTTP-服务端-启动完成:{}", this.getPe().toStr(), port);
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
            this.getBossGroup().shutdownGracefully();
            if (Objects.nonNull(this.getWorkerGroup())) {
                this.getWorkerGroup().shutdownGracefully();
            }
        }
    }

}
