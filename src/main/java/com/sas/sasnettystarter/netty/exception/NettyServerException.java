package com.sas.sasnettystarter.netty.exception;

/**
 * netty服务异常
 */
public class NettyServerException extends RuntimeException {
    public NettyServerException(String message) {
        super(message);
    }
}
