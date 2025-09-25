package com.sas.sasnettystarter.netty.mods.operation;

import com.sas.sasnettystarter.netty.IpPortAddress;
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
     * @param ipPortAddress
     * @return
     */
    Boolean connectSync(IpPortAddress ipPortAddress);

    /**
     * 发送post请求
     *
     * @param path
     * @param data
     * @param ipPortAddress
     * @param headerFunc
     * @param callback
     * @param <T>
     */
    <T> void sendPostBody(String path, T data, IpPortAddress ipPortAddress,
                          Function<HttpHeaders, Boolean> headerFunc,
                          Function<ChannelFuture, Boolean> callback);


    /**
     * 发送get请求
     *
     * @param path
     * @param data
     * @param ipPortAddress
     * @param headerFunc
     */
    void sendGetRequest(String path, Map<String, ?> data, IpPortAddress ipPortAddress, Function<HttpHeaders, Boolean> headerFunc);

}
