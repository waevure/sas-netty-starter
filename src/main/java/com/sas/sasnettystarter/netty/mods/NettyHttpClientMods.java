package com.sas.sasnettystarter.netty.mods;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.utils.GsonUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
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
public class NettyHttpClientMods extends NettyClientMods {

    public NettyHttpClientMods(Bootstrap bootstrap, EventLoopGroup group) {
        super(bootstrap, group);
    }

    public NettyHttpClientMods(Bootstrap bootstrap, EventLoopGroup group, Function<Channel, Boolean> startSuccessCallback) {
        super(bootstrap, group, startSuccessCallback);
    }



    /**
     * 下发指令
     *
     * @param path 请求路径
     * @param data 消息体
     */
    public <T> void sendPostBody(String path, T data, IpPortAddress ipPortAddress, Function<HttpHeaders, Boolean> headerFunc) {
        // 获取请求地址
        ChannelHandlerContext ctx = this.variable.getCtx(ipPortAddress.ipPort());
        if (Objects.isNull(ctx) || !ctx.channel().isActive()) {
            log.error("连接不存在:{}", ipPortAddress.ipPort());
            return;
        }
        // 创建 POST 请求
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                path,
                Unpooled.copiedBuffer(GsonUtils.toJSONString(data), CharsetUtil.UTF_8)
        );
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        request.headers().set(HttpHeaderNames.HOST, ipPortAddress.getIp());
        // 设置消息头
        headerFunc.apply(request.headers());

        ctx.writeAndFlush(request);
    }

    /**
     * 下发指令
     *
     * @param path 请求路径
     * @param data 消息体（GET 请求通常没有消息体，数据通过 URL 参数传递）
     */
    public <T> void sendGetRequest(String path, T data, IpPortAddress ipPortAddress, Function<HttpHeaders, Boolean> headerFunc) {
        // 获取请求地址
        ChannelHandlerContext ctx = this.variable.getCtx(ipPortAddress.ipPort());
        if (Objects.isNull(ctx) || !ctx.channel().isActive()) {
            log.error("连接不存在:{}", ipPortAddress.ipPort());
            return;
        }

        // 创建 GET 请求
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, path,
                Unpooled.EMPTY_BUFFER);

        // 设置请求头
        request.headers().set(HttpHeaderNames.HOST, ipPortAddress.getIp());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

        // 设置额外的消息头
        if (Objects.nonNull(headerFunc)) {
            headerFunc.apply(request.headers());
        }

        // 发送请求
        ctx.writeAndFlush(request);

        // 释放
        request.release();
    }


}
