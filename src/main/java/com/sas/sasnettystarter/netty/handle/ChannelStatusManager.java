package com.sas.sasnettystarter.netty.handle;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.TiFunction;
import com.sas.sasnettystarter.netty.cache.VariableChannelCache;
import com.sas.sasnettystarter.netty.handle.bo.NettyOfflineBo;
import com.sas.sasnettystarter.netty.handle.bo.NettyOnlineBo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 默认通道状态管理，放在解码之后
 *
 * @author WQY
 * @version 1.0
 * @date 2024/1/22 16:00
 */
@Slf4j
@Getter
public class ChannelStatusManager extends LogicHandler {

    // 项目接口表记
    private ProjectAbstract pe;
    /**
     * 读取回调
     */
    private TiFunction<ChannelHandlerContext, Object, ProjectAbstract, Object> function;

    /**
     * 缓存引用
     */
    private VariableChannelCache variableChannelCache;

    public ChannelStatusManager(TiFunction<ChannelHandlerContext, Object, ProjectAbstract, Object> function, VariableChannelCache variableChannelCache, ProjectAbstract pe) {
        log.info("map引用:{}", variableChannelCache);
        this.function = function;
        this.variableChannelCache = variableChannelCache;
        this.pe = pe;
    }

    /**
     * 信道登记，刚有连接连入的时候,
     * 只有刚连入的时候触发一次
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("{}-信道登记:{}", this.getPe().toStr(), ctx.toString());
    }

    /**
     * 信道未注册，
     * 通道断开连接后执行，级别低于channelInactive
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.error("{}-信道未注册:{}", this.getPe().toStr(), NetAddress.nettyRemoteAddress(ctx.channel()).ipPort());
        super.channelUnregistered(ctx);
    }

    /**
     * 沟道激活
     * 只有刚接入的时候触发一次
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //解析channel
        String key = NetAddress.nettyRemoteAddress(ctx.channel()).ipPort();
        //加入缓存数据
        this.getVariableChannelCache().putCtx(key, ctx);
        // 写入用户事件
        ctx.fireUserEventTriggered(new NettyOnlineBo(this.getPe()));
        log.info("{}-沟道激活:{}", this.getPe().toStr(), NetAddress.nettyRemoteAddress(ctx.channel()).ipPort());
    }

    /**
     * 沟道不活跃，
     * 通道断开连接时执行
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //释放资源
        this.getVariableChannelCache().removeCtx(ctx);
        // 写入用户事件
        ctx.fireUserEventTriggered(new NettyOfflineBo(this.getPe()));
        log.warn("{}-连接断开:{}", this.getPe().toStr(), NetAddress.nettyRemoteAddress(ctx.channel()).ipPort());
    }

    /**
     * 沟道读
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (Objects.nonNull(this.getFunction())) {
            try {
                this.getFunction().apply(ctx, msg, getPe());
            } finally {
                ReferenceCountUtil.release(msg); // buf 生命周期到这结束
            }
        } else {
            ctx.fireChannelRead(msg); // 不处理就交给下游
        }
    }

    /**
     * 通道读取完成
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.debug("{}-通道读取完成:{}", this.getPe().toStr(), NetAddress.nettyRemoteAddress(ctx.channel()).ipPort());
    }

    /**
     * 已触发用户事件
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("{}-已触发用户事件:{}", this.getPe().toStr(), NetAddress.nettyRemoteAddress(ctx.channel()).ipPort());
    }

    /**
     * 频道可写入性已更改
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.warn("{}-频道可写入性已更改:{}", this.getPe().toStr(), NetAddress.nettyRemoteAddress(ctx.channel()).ipPort());
    }

    /**
     * 异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //释放资源
        this.getVariableChannelCache().removeCtx(ctx);
        // 写入用户事件
        ctx.fireUserEventTriggered(new NettyOfflineBo(this.getPe()));
        log.error("{}-异常-断开连接:{}", this.getPe().toStr(), NetAddress.nettyRemoteAddress(ctx.channel()).ipPort());
        ctx.close();
    }

}
