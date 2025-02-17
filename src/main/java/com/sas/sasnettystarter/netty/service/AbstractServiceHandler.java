package com.sas.sasnettystarter.netty.service;

import com.sas.sasnettystarter.netty.exception.NettyServiceException;

import java.util.List;

/**
 * 业务处理抽象
 * @author WQY
 * @version 1.0
 * @date 2024/1/23 09:51
 */
public abstract class AbstractServiceHandler<T extends ServiceData> implements ServiceHandler<T>{

    @Override
    public void handle(T data) {
        throw new NettyServiceException("需要自己重写该方法");
    }

    @Override
    public void handle(List<T> data) {
        throw new NettyServiceException("需要自己重写该方法");
    }
}
