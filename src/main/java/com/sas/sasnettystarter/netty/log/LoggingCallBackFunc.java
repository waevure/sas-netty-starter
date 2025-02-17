package com.sas.sasnettystarter.netty.log;

import io.netty.channel.ChannelId;

/**
 * @InterfaceName: LoggingCallBackFunc
 * @Description: 日志回调函数
 * @Author: Wqy
 * @Date: 2024-06-06 11:22
 * @Version: 1.0
 **/
@FunctionalInterface
public interface LoggingCallBackFunc {

    /**
     * 字符串日志回调
     * 一般用于日志存库之类的
     *
     * @return
     */
    void strLogCall(ChannelId channelId, String type, String strLog, String serverIpPortStr);

}
