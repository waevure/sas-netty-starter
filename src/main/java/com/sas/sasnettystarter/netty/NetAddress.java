package com.sas.sasnettystarter.netty;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author WQY
 * @version 1.0
 * @date 2023/12/4 15:25
 */
@Slf4j
@Data
@AllArgsConstructor
public class NetAddress {

    private String ip;

    private Integer port;

    public String ipPort() {
        return String.format("%s:%d", this.getIp(), this.getPort());
    }

    public Integer getPort() {
        if (port == null) {
            return 80;
        }
        return port;
    }

    public void setPort(Integer port) {
        if (port == null) {
            this.port = 80;
            return;
        }
        this.port = port;
    }

    /**
     * 解析netty地址
     *
     * @param channel
     * @return
     */
    public static NetAddress nettyRemoteAddress(Channel channel) {
        // 获取远程地址信息
        SocketAddress socketAddress = channel.remoteAddress();
        if (Objects.nonNull(socketAddress)) {
            // 是否为无网络的channel
            if (socketAddress.toString().equals("embedded")) {
                return new NetAddress(socketAddress.toString(), null);
            }
            InetSocketAddress remoteAddr = (InetSocketAddress) socketAddress;
            //返回
            return new NetAddress(remoteAddr.getHostName(), remoteAddr.getPort());
        }
        return new NetAddress("connect failed", -1);
    }

    /**
     * 解析地址
     *
     * @return
     */
    public static String analysisAddress(String msg) {
        if (StrUtil.isBlank(msg)) {
            log.error("解析地址失败:{}", msg);
        }
        //长度
        if (msg.length() > 6) {
            //去除所有空格
            msg = msg.replace(" ", "");
            String address = msg.substring(4, 6);
            return address;
        }
        return null;
    }
}
