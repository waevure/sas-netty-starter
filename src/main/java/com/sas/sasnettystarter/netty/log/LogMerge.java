package com.sas.sasnettystarter.netty.log;

import io.netty.handler.logging.LogLevel;
import lombok.Data;

/**
 * @ClassName: LogMerge
 * @Description: 只是个简单的合并参数
 * @Author: Wqy
 * @Date: 2024-06-06 11:26
 * @Version: 1.0
 **/
@Data
public class LogMerge {

    /**
     * 日志级别
     * 如果为null则不打印日志
     */
    private LogLevel logLevel;

    /**
     * 回调函数
     */
    private LoggingCallBackFunc loggingCallBackFunc;

    public LogMerge(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public LogMerge(LogLevel logLevel, LoggingCallBackFunc loggingCallBackFunc) {
        this.logLevel = logLevel;
        this.loggingCallBackFunc = loggingCallBackFunc;
    }
}
