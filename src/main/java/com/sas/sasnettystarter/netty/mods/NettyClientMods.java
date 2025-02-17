package com.sas.sasnettystarter.netty.mods;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.cache.Variable;
import com.sas.sasnettystarter.netty.constant.NettyConstant;
import com.sas.sasnettystarter.netty.exception.NettyLinkException;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @ClassName: NettyStartClientMods
 * @Description: 客户端
 * @Author: Wqy
 * @Date: 2024-05-31 15:17
 * @Version: 1.0
 **/
@Data
@Slf4j
public class NettyClientMods extends NettyMods {

    private Bootstrap bootstrap;

    private EventLoopGroup group;

    // 所有客户端
    private Map<String, ChannelFuture> channelFutures;

    public NettyClientMods(Bootstrap bootstrap, EventLoopGroup group) {
        this.nettyType = NettyType.C;
        this.bootstrap = bootstrap;
        this.group = group;
        this.channelFutures = new ConcurrentHashMap<>();
        //初始化缓存信息
        this.variable = new Variable();
    }

    public NettyClientMods(Bootstrap bootstrap, EventLoopGroup group, Function<Channel, Boolean> startSuccessCallback) {
        this.nettyType = NettyType.C;
        this.bootstrap = bootstrap;
        this.group = group;
        this.channelFutures = new ConcurrentHashMap<>();
        // 初始化缓存信息
        this.variable = new Variable();
        // 成功回调
        this.startSuccessCallback = startSuccessCallback;
    }

    /**
     * 等待退出
     */
    @Override
    public void awaitSync() {
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
            this.getGroup().shutdownGracefully();
        }
    }

    /**
     * 连接-同步
     *
     * @param ipPortAddress
     */
    public Boolean connectSync(IpPortAddress ipPortAddress) {
        // 进行连接
        ChannelFuture future = this.getBootstrap().connect(ipPortAddress.getIp(), ipPortAddress.getPort());
        // 等待
        try {
            future.sync();
            if (future.isSuccess()) {
                log.info("{}-同步连接成功", ipPortAddress.ipPort());
                this.channelFutures.put(ipPortAddress.ipPort(), future);
                return true;
            } else {
                future.cause().printStackTrace();
                log.info("{}-同步连接失败", ipPortAddress.ipPort());
                this.channelFutures.put(ipPortAddress.ipPort(), future);
                return false;
            }
        } catch (Exception e) {
            throw new NettyLinkException(NettyConstant.CONN_ERR_MSG);
        }
    }

    /**
     * 连接-异步
     *
     * @param ipPortAddress
     */
    public Boolean connectAsync(IpPortAddress ipPortAddress) {
        ChannelFuture future = this.getBootstrap().connect(ipPortAddress.getIp(), ipPortAddress.getPort());
        // 结果
        AtomicReference<Boolean> rs = new AtomicReference<>(false);
        future.addListener(f -> {
            if (f.isSuccess()) {
                log.info("{}-异步连接成功", ipPortAddress.ipPort());
                this.channelFutures.put(ipPortAddress.ipPort(), future);
                rs.set(true);
            } else {
                f.cause().printStackTrace();
                log.info("{}-异步连接失败", ipPortAddress.ipPort());
                this.channelFutures.put(ipPortAddress.ipPort(), future);
                rs.set(false);
            }
        });
        return rs.get();
    }

    /**
     * 断开
     *
     * @param ipPortAddress
     */
    public Boolean disconnect(IpPortAddress ipPortAddress) {
        // 获取channelFuture
        ChannelFuture future = this.channelFutures.get(ipPortAddress.ipPort());
        if (Objects.nonNull(future)){
            if (future.channel().isActive()){
                future.channel().close();
                return true;
            }
        }
        return false;
    }

    /**
     * 下发指令
     *
     * @param key
     * @param writeData
     */
    public void distributeInstruct(String key, NettyWriteBo writeData) {
        // 获取通道
        ChannelHandlerContext ctx = this.variable.getCtx(key);
        if (Objects.isNull(ctx)) {
            throw new NettyLinkException(NettyConstant.LINK_NOT_EXIST + key);
        }
        if (!ctx.channel().isActive()) {
            throw new NettyLinkException(NettyConstant.LINK_NOT_ACTIVE + key);
        }
        ctx.channel().writeAndFlush(writeData);
    }

    /**
     * 获取所有客户端
     *
     * @return
     */
    public List<ChannelFuture> channelFutureList() {
        List<ChannelFuture> futures = new ArrayList<>();
        for (ChannelFuture future : this.channelFutures.values()) {
            futures.add(future);
        }
        return futures;
    }

    /**
     * 销毁当前服务
     * @return
     */
    @Override
    public boolean destroyServer() throws InterruptedException {
        log.info("Netty客户端开始销毁:{}",this);
        // 关闭所有channel
        this.channelFutures.values().forEach(channel->channel.channel().close());
        // 关闭线程组
        this.group.shutdownGracefully().sync();
        // 销毁variable
        this.variable.destroy();
        log.info("Netty客户端销毁完成:{}",this);
        // 优雅关闭
        return true;
    }

}
