package com.sas.sasnettystarter.netty.mods;

import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.cache.Variable;
import com.sas.sasnettystarter.netty.exception.NettyLinkException;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName: NettyStartClientMods
 * @Description: 服务端
 * @Author: Wqy
 * @Date: 2024-05-31 15:17
 * @Version: 1.0
 **/
@Data
@Slf4j
public class NettyServerMods extends NettyMods {

    private ServerBootstrap bootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    // 外部使用的通道缓存，一般key为设备唯一编码。其实就是存一些执行力注册的设备
    private Map<String, ChannelHandlerContext> keyMap = new HashMap<>();

    // 服务通道future
    private ChannelFuture channelFuture;

    public NettyServerMods(ServerBootstrap bootstrap, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.nettyType = NettyType.S;
        this.bootstrap = bootstrap;
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
            log.info("准备启动中:{}", port);
            ChannelFuture f = this.bootstrap.bind(port).sync();
            log.info("启动完成:{}", port);
            this.channelFuture = f;
            // 进行回调
            if (Objects.nonNull(this.startSuccessCallback)){
                this.startSuccessCallback.apply(f.channel());
            }
            //使用f.channel().closeFuture().sync()方法进行阻塞,等待服务端链路关闭之后main函数才退出。
            f.channel().closeFuture().sync();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            /**优雅退出，释放NIO线程组*/
            this.getBossGroup().shutdownGracefully();
            this.getWorkerGroup().shutdownGracefully();
        }
    }

    /**
     * 通道是否活跃
     *
     * @return
     */
    public boolean serverChannelActive() {
        return this.channelFuture.channel().isActive();
    }

    /**
     * 服务本地地址
     *
     * @return
     */
    public String serverLocalAddress() {
        // 判断存到缓存是否有值，有值的话从通道缓存取
        List<ChannelHandlerContext> ctxList = this.variable().channelActiveList();
        if (ctxList.size() > 0) {
            return ctxList.get(0).channel().localAddress().toString();
        }
        return this.channelFuture.channel().localAddress().toString();
    }

    /**
     * 添加ctx
     *
     * @param key
     * @param ctx
     */
    public void putCtx(String key, ChannelHandlerContext ctx) {
        this.keyMap.put(key, ctx);
    }

    /**
     * 获取
     * @param key
     * @return
     */
    public ChannelHandlerContext getCtx(String key) {
        return this.keyMap.get(key);
    }

    /**
     * 下发指令
     * @param key
     * @param writeData
     */
    public void distributeInstruct(String key, NettyWriteBo writeData) {
        // 获取通道
        ChannelHandlerContext ctx = this.keyMap.get(key);
        if (Objects.isNull(ctx)) {
            throw new NettyLinkException("链路未注册:" + key);
        }
        if (!ctx.channel().isActive()) {
            throw new NettyLinkException("链路未激活:" + key);
        }
        ctx.channel().writeAndFlush(writeData);
    }

    /**
     * 全部注册通道
     *
     * @return
     */
    public Map<String, ChannelHandlerContext> registerClientChannel() {
        return this.keyMap;
    }

    @Override
    public boolean destroyServer() {
        log.info("Netty服务端开始销毁:{}",this);
        this.channelFuture.channel().close();
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
        // 销毁variable
        this.variable.destroy();
        log.info("Netty服务端销毁完成:{}",this);
        return true;
    }
}
