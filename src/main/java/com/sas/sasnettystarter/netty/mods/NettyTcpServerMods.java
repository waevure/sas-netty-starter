package com.sas.sasnettystarter.netty.mods;

import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.cache.Variable;
import com.sas.sasnettystarter.netty.exception.NettyLinkException;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.mods.operation.NettyTcpServerOperations;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * @ClassName: NettyTcpClientMods
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 15:21
 * @Version: 1.0
 **/
@Slf4j
public class NettyTcpServerMods extends NettyServerMods implements NettyTcpServerOperations {
    /**
     * @param nettyType   类型
     * @param bootstrap   netty引导类
     * @param bossGroup   boss组
     * @param workerGroup worker组
     */
    public NettyTcpServerMods(ProjectAbstract pe, NettyType nettyType, ServerBootstrap bootstrap, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
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
            log.info("{}-TCP-服务端-准备启动中:{}", this.getPe().toStr(), port);
            ChannelFuture f = this.serverBootstrap.bind(port).sync();
            log.info("{}-TCP-服务端-启动完成:{}", this.getPe().toStr(), port);
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
            this.getBossGroup().shutdownGracefully().syncUninterruptibly();
            this.getWorkerGroup().shutdownGracefully().syncUninterruptibly();
            // 打印状态
            this.printNettyServerBootstrapGroupStatus();
        }
    }

    /**
     * 添加ctx
     *
     * @param key
     * @param ctx
     */
    @Override
    public void putCtx(String key, ChannelHandlerContext ctx) {
        this.keyMap.put(key, ctx);
    }

    /**
     * 获取
     *
     * @param key
     * @return
     */
    @Override
    public ChannelHandlerContext getCtx(String key) {
        return this.keyMap.get(key);
    }

    /**
     * 下发指令
     *
     * @param key
     * @param writeData
     */
    @Override
    public void distributeInstruct(String key, NettyWriteBo writeData) {
        // 获取通道
        ChannelHandlerContext ctx = this.getCtx(key);
        if (Objects.isNull(ctx)) {
            throw new NettyLinkException(String.format("%s-TCP-服务端-链路未注册:%s", this.pe.toStr(), key));
        }
        if (!ctx.channel().isActive()) {
            throw new NettyLinkException(String.format("%s-TCP-服务端-链路未激活:%s", this.pe.toStr(), key));
        }
        ctx.channel().writeAndFlush(writeData);
    }

    /**
     * 全部注册通道
     *
     * @return
     */
    @Override
    public Map<String, ChannelHandlerContext> registerClientChannel() {
        return this.keyMap;
    }

    @Override
    public boolean destroyServer() {
        try {
            // 打印日志：开始销毁
            log.info("{}-TCP-服务端-开始销毁", this.getPe().toStr());
            // 关闭客户端缓存
            for (String key : this.keyMap.keySet()) {
                ChannelHandlerContext ctx = this.keyMap.get(key);
                if (ctx.channel().isOpen()) {
                    ctx.close().syncUninterruptibly();
                    log.info("{}-{}-关闭", this.getPe().toStr(), key);
                } else {
                    log.info("{}-{}-已关闭", this.getPe().toStr(), key);
                }
            }
            // 关闭所有客户端连接
            this.getVariable().destroy(this.getPe());
            // 关闭 Netty 的 Channel（也就是服务端的监听端口，accept 新连接的入口）
            if (this.channelFuture != null) {
                Channel channel = this.channelFuture.channel();
                if (channel != null && channel.isOpen()) {
                    channel.close().syncUninterruptibly();
                }
            }
            // 打印日志：销毁完成
            log.info("{}-TCP-服务端-销毁完成", this.pe.toStr());
            return true;
        } catch (Exception ex) {
            log.error("{} - TCP 服务端销毁失败: {}", this.pe.toStr(), ex.getMessage(), ex);
            return false;
        }
    }
}
