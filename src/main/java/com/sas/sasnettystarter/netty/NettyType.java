package com.sas.sasnettystarter.netty;


/**
 * @EnumName: NettyType
 * @Description: netty类型
 * @Author: Wqy
 * @Date: 2024-05-31 15:03
 * @Version: 1.0
 **/
public enum NettyType {

    // 客户端,TCP长连接
    C,
    // 客户端，HTTP
    C_HTTP,
    // 服务端，TCP长连接
    S,
    // 无网络的责任链
    NO_NETWORK_CHANNEL;
}
