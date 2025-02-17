package com.sas.sasnettystarter.netty.exception;

/**
 * netty业务异常
 * @author WQY
 * @version 1.0
 * @date 2024/1/23 09:54
 */
public class NettyServiceException extends RuntimeException{
    public NettyServiceException(String s) {
        super(s);
    }
}
