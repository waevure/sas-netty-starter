package com.sas.sasnettystarter.netty.ops.http;

import com.sas.sasnettystarter.netty.NetAddress;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.Map;
import java.util.function.Function;

/**
 * @InterfaceName: NettyHttpClientOperations
 * @Description: netty-http客户端能力
 * @Author: Wqy
 * @Date: 2025-09-24 15:15
 * @Version: 1.0
 **/
public interface NettyHttpClientOperations {

    /**
     * 链接
     *
     * @param netAddress
     * @return
     */
    Boolean connectSync(NetAddress netAddress);

    /**
     * 发送post请求
     *
     * @param path
     * @param data
     * @param netAddress
     * @param headerFunc
     * @param callback
     * @param <T>
     */
    <T> void sendPostBody(String path, T data, NetAddress netAddress,
                          Function<HttpHeaders, Boolean> headerFunc,
                          Function<ChannelFuture, Boolean> callback);


    /**
     * 发送get请求
     *
     * @param path
     * @param data
     * @param netAddress
     * @param headerFunc
     */
    void sendGetRequest(String path, Map<String, ?> data, NetAddress netAddress, Function<HttpHeaders, Boolean> headerFunc);

}
