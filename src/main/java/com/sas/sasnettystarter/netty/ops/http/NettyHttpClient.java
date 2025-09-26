package com.sas.sasnettystarter.netty.ops.http;

import cn.hutool.http.HttpUtil;
import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.constant.NettyConstant;
import com.sas.sasnettystarter.netty.exception.NettyLinkException;
import com.sas.sasnettystarter.netty.ops.core.NettyServerBaseContext;
import com.sas.sasnettystarter.netty.utils.GsonUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @ClassName: NettyHttpClient
 * @Description: netty-http-client功能
 * @Author: Wqy
 * @Date: 2024-10-29 10:30
 * @Version: 1.0
 **/
@Slf4j
public class NettyHttpClient extends NettyServerBaseContext implements NettyHttpClientOperations {

    public NettyHttpClient(ProjectAbstract pe, NettyType nettyType, Bootstrap bootstrap, EventLoopGroup group) {
        super(pe, nettyType, bootstrap, group);
    }

    public NettyHttpClient(ProjectAbstract pe, NettyType nettyType, Bootstrap bootstrap, EventLoopGroup group, Function<Channel, Boolean> startSuccessCallback) {
        super(pe, nettyType, bootstrap, group, startSuccessCallback);
    }

    @Override
    public void awaitCloseSync(Integer port) {

    }

    @Override
    public boolean destroyServer() {
        try {
            // 打印日志：开始销毁
            log.info("{}-HTTP-客户端-开始销毁", this.getPe().toStr());
            // 关闭所有服务连接
            for (String key : this.getChannelFutures().keySet()) {
                // 获取future
                ChannelFuture future = this.getChannelFutures().get(key);
                if (future.channel().isOpen()) {
                    future.channel().close().syncUninterruptibly();
                    log.info("{}-HTTP-客户端-关闭连接:{}", this.getPe().toStr(), key);
                }
            }
            // 关闭bossGroup
            this.getBossGroup().shutdownGracefully().syncUninterruptibly();
            // 关闭所有客户端连接
            this.getVariableChannelCache().destroy(this.getPe());
            // 打印关闭信息
            this.printNettyBootstrapGroupStatus();
            // 打印日志：销毁完成
            log.info("{}-HTTP-客户端-销毁完成", this.getPe().toStr());
            return true;
        } catch (Exception ex) {
            log.error("{} - TCP 客户端销毁失败: {}", this.getPe().toStr(), ex.getMessage(), ex);
            return false;
        }
    }


    @Override
    public Boolean connectSync(IpPortAddress ipPortAddress) {
        // 进行连接
        ChannelFuture future = this.getBootstrap().connect(ipPortAddress.getIp(), ipPortAddress.getPort());
        // 等待
        try {
            future.sync();
            if (future.isSuccess()) {
                log.info("[{}]-{}-HTTP-客户端-同步连接成功", this.getPe().toStr(), ipPortAddress.ipPort());
                // ⭐ 监听关闭事件，移除缓存
                future.channel().closeFuture().addListener((ChannelFutureListener) closeFuture -> {
                    log.info("[{}]-{}-HTTP-客户端-连接已关闭，移除缓存", this.getPe().toStr(), ipPortAddress.ipPort());
                    this.getChannelFutures().remove(ipPortAddress.ipPort());
                });
                this.getChannelFutures().put(ipPortAddress.ipPort(), future);
                return true;
            } else {
                future.cause().printStackTrace();
                log.info("[{}]-{}-HTTP-客户端-同步连接失败", this.getPe().toStr(), ipPortAddress.ipPort());
                this.getChannelFutures().put(ipPortAddress.ipPort(), future);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NettyLinkException(NettyConstant.CONN_ERR_MSG);
        }
    }

    /**
     * 下发指令
     *
     * @param path 请求路径
     * @param data 消息体
     */
    @Override
    public <T> void sendPostBody(String path, T data, IpPortAddress ipPortAddress,
                                 Function<HttpHeaders, Boolean> headerFunc,
                                 Function<ChannelFuture, Boolean> callback) {
        // 获取请求地址
        ChannelHandlerContext ctx = this.getVariableChannelCache().getCtx(ipPortAddress.ipPort());
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

        // 发送请求
        ChannelFuture future = ctx.writeAndFlush(request);
        // 回调
        future.addListener((ChannelFutureListener) f -> {
            if (Objects.nonNull(callback)) {
                callback.apply(f);
            }
            request.release(); // 发送完成后释放
        });
    }

    /**
     * 下发指令
     *
     * @param path 请求路径
     * @param data 消息体（GET 请求通常没有消息体，数据通过 URL 参数传递）
     */
    @Override
    public void sendGetRequest(String path, Map<String, ?> data, IpPortAddress ipPortAddress, Function<HttpHeaders, Boolean> headerFunc) {
        // 获取请求地址
        ChannelFuture cf = this.getChannelFutures().get(ipPortAddress.ipPort());
        if (Objects.isNull(cf) || !cf.channel().isActive()) {
            log.error("连接不存在:{}", ipPortAddress.ipPort());
            return;
        }


        // 创建 GET 请求
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, path + "?" + HttpUtil.toParams(data),
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
        cf.channel().writeAndFlush(request);

        // 释放
        request.release();
    }


}
