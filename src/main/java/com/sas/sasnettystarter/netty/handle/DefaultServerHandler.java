package com.sas.sasnettystarter.netty.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认逻辑处理器
 * ChannelInboundHandlerAdapter为netty的逻辑处理器
 *
 * @author wqy
 * @version 1.0
 * @date 2020/9/12 9:13
 */
@Slf4j
public class DefaultServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 信道登记，刚有连接连入的时候,
     * 只有刚连入的时候触发一次
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("欢迎连接:{}", ctx.toString());
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
        log.info("信道未注册:{}", ctx.toString());
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
        log.info("沟道激活:{}", ctx.toString());
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
        log.info("通道断开连接:{}", ctx.toString());
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
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            String data = new String(bytes); // 这里只是演示
            System.out.println("收到原始数据: " + data);
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
        log.info("通道读取完成:{}", ctx.toString());
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
        log.info("已触发用户事件:{}", ctx.toString());
    }

    /**
     * 频道可写入性已更改
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("频道可写入性已更改:{}", ctx.toString());
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
        log.info("异常:{}", ctx.toString());
    }
}
