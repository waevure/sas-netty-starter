package com.sas.sasnettystarter.netty.exception;

/**
 * netty链路异常
 * @author WQY
 * @version 1.0
 * @date 2024/1/23 09:54
 */
public class NettyLinkException extends RuntimeException{
    public NettyLinkException(String s) {
        super(s);
    }
}
