package com.sas.sasnettystarter.netty;

import lombok.Data;

import java.util.Objects;

/**
 * @ClassName: NettyRunnable
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 10:18
 * @Version: 1.0
 **/
@Data
public class NettyRunnable implements Runnable{

    private final NettySupport support;

    public NettyRunnable(NettySupport support) {
        this.support = support;
    }

    @Override
    public void run() {
        //等待关闭
        if (Objects.nonNull(support.getNettyLink().getServerPort()) && support.getNettyLink().getServerPort() > 0) {
            // 等待关闭
            support.awaitSync(support.getNettyLink().getServerPort());
        } else {
            // 等待关闭
            support.awaitSync();
        }
    }
}
