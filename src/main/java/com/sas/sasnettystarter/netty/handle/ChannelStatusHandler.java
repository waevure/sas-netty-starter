package com.sas.sasnettystarter.netty.handle;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.TiFunction;
import com.sas.sasnettystarter.netty.cache.Variable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
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
public class ChannelStatusHandler extends LogicHandler {

    // 项目接口表记
    private ProjectAbstract projectAbstract;
    /**
     * 读取回调
     */
    private TiFunction<ChannelHandlerContext, Object, ProjectAbstract, Object> function;

    /**
     * 缓存引用
     */
    private Variable variable;

    public ChannelStatusHandler(TiFunction<ChannelHandlerContext, Object, ProjectAbstract, Object> function, Variable variable, ProjectAbstract projectAbstract) {
        log.info("map引用:{}", variable);
        this.function = function;
        this.variable = variable;
        this.projectAbstract = projectAbstract;
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
        log.info("{}-信道登记:{}", projectAbstract.getProjectCode(), ctx.toString());
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
        log.error("{}-信道未注册:{}", projectAbstract.getProjectCode(), IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort());
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
        String key = IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort();
        //加入缓存数据
        this.variable.putCtx(key, ctx);
        log.info("{}-沟道激活:{}", projectAbstract.getProjectCode(), IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort());
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
        this.variable.removeCtx(ctx);
        log.warn("{}-连接断开:{}", projectAbstract.getProjectCode(), IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort());
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
        boolean release = true;
        try {
            if (Objects.nonNull(this.function)) {
                // 所有设备状态
                this.function.apply(ctx, msg, this.projectAbstract);
            } else {
                release = false;
                super.channelRead(ctx, msg);
            }
        } finally {
            if (release) {
                // 手动释放引用计数对象，防止内存泄漏
                ReferenceCountUtil.release(msg);
            }
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
        log.debug("{}-通道读取完成:{}", projectAbstract.getProjectCode(), IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort());
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
        log.info("{}-已触发用户事件:{}", projectAbstract.getProjectCode(), IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort());
    }

    /**
     * 频道可写入性已更改
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.warn("{}-频道可写入性已更改:{}", projectAbstract.getProjectCode(), IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort());
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
        this.variable.removeCtx(ctx);
        log.error("{}-异常-断开连接:{}", projectAbstract.getProjectCode(), IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort());
        ctx.close();
    }

}
