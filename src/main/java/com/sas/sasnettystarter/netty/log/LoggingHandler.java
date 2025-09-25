/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.sas.sasnettystarter.netty.log;

import com.sas.sasnettystarter.netty.ProjectAbstract;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.SocketAddress;
import java.util.Objects;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * A {@link ChannelHandler} that logs all events using a logging framework.
 * By default, all events are logged at <tt>DEBUG</tt> level and full hex dumps are recorded for ByteBufs.
 */
@Sharable
@SuppressWarnings({"StringConcatenationInsideStringBufferAppend", "StringBufferReplaceableByString"})
public class LoggingHandler extends ChannelDuplexHandler {

    private static final LogLevel DEFAULT_LEVEL = LogLevel.DEBUG;

    protected final InternalLogger logger;
    protected final InternalLogLevel internalLevel;

    private final LogLevel level;
    private final ByteBufFormat byteBufFormat;

    // 项目信息
    private ProjectAbstract pe;
    // 日志回调函数
    private LoggingCallBackFunc loggingCallBackFunc;

    /**
     * Creates a new instance whose logger name is the fully qualified class
     * name of the instance with hex dump enabled.
     */
    public LoggingHandler(ProjectAbstract pe) {
        this(pe, DEFAULT_LEVEL);
    }

    /**
     * Creates a new instance whose logger name is the fully qualified class
     * name of the instance.
     *
     * @param format Format of ByteBuf dumping
     */
    public LoggingHandler(ProjectAbstract pe, ByteBufFormat format) {
        this(pe, DEFAULT_LEVEL, format);
    }

    /**
     * Creates a new instance whose logger name is the fully qualified class
     * name of the instance.
     *
     * @param level the log level
     */
    public LoggingHandler(ProjectAbstract pe, LogLevel level) {
        this(pe, level, ByteBufFormat.HEX_DUMP);
    }

    /**
     * Creates a new instance whose logger name is the fully qualified class
     * name of the instance.
     *
     * @param level         the log level
     * @param byteBufFormat the ByteBuf format
     */
    public LoggingHandler(ProjectAbstract pe, LogLevel level, ByteBufFormat byteBufFormat) {
        this.pe = pe;
        this.level = ObjectUtil.checkNotNull(level, "level");
        this.byteBufFormat = ObjectUtil.checkNotNull(byteBufFormat, "byteBufFormat");
        logger = InternalLoggerFactory.getInstance(getClass());
        internalLevel = level.toInternalLevel();
    }

    /**
     * Creates a new instance with the specified logger name and with hex dump
     * enabled.
     *
     * @param clazz the class type to generate the logger for
     */
    public LoggingHandler(Class<?> clazz) {
        this(clazz, DEFAULT_LEVEL);
    }

    /**
     * Creates a new instance with the specified logger name.
     *
     * @param clazz the class type to generate the logger for
     * @param level the log level
     */
    public LoggingHandler(Class<?> clazz, LogLevel level) {
        this(clazz, level, ByteBufFormat.HEX_DUMP);
    }

    /**
     * Creates a new instance with the specified logger name.
     *
     * @param clazz         the class type to generate the logger for
     * @param level         the log level
     * @param byteBufFormat the ByteBuf format
     */
    public LoggingHandler(Class<?> clazz, LogLevel level, ByteBufFormat byteBufFormat) {
        ObjectUtil.checkNotNull(clazz, "clazz");
        this.level = ObjectUtil.checkNotNull(level, "level");
        this.byteBufFormat = ObjectUtil.checkNotNull(byteBufFormat, "byteBufFormat");
        logger = InternalLoggerFactory.getInstance(clazz);
        internalLevel = level.toInternalLevel();
    }

    /**
     * Creates a new instance with the specified logger name using the default log level.
     *
     * @param name the name of the class to use for the logger
     */
    public LoggingHandler(String name) {
        this(name, DEFAULT_LEVEL);
    }

    /**
     * Creates a new instance with the specified logger name.
     *
     * @param name  the name of the class to use for the logger
     * @param level the log level
     */
    public LoggingHandler(String name, LogLevel level) {
        this(name, level, ByteBufFormat.HEX_DUMP);
    }

    /**
     * Creates a new instance with the specified logger name.
     *
     * @param name          the name of the class to use for the logger
     * @param level         the log level
     * @param byteBufFormat the ByteBuf format
     */
    public LoggingHandler(String name, LogLevel level, ByteBufFormat byteBufFormat) {
        ObjectUtil.checkNotNull(name, "name");

        this.level = ObjectUtil.checkNotNull(level, "level");
        this.byteBufFormat = ObjectUtil.checkNotNull(byteBufFormat, "byteBufFormat");
        logger = InternalLoggerFactory.getInstance(name);
        internalLevel = level.toInternalLevel();
    }

    /**
     * Returns the {@link LogLevel} that this handler uses to log
     */
    public LogLevel level() {
        return level;
    }

    /**
     * 添加日志回调函数
     *
     * @param loggingCallBackFunc
     */
    public void addStrLogCall(LoggingCallBackFunc loggingCallBackFunc) {
        this.loggingCallBackFunc = loggingCallBackFunc;
    }

    /**
     * Returns the {@link ByteBufFormat} that this handler uses to log
     */
    public ByteBufFormat byteBufFormat() {
        return byteBufFormat;
    }

    /**
     * 日志主机地址
     *
     * @param ctx
     * @return
     */
    private String logAddr(ChannelHandlerContext ctx) {
        String strAddr = "";
        SocketAddress address = ctx.channel().localAddress();
        if (Objects.isNull(address)) {
            address = ctx.channel().remoteAddress();
        }
        if (Objects.nonNull(address)) {
            strAddr = address.toString();
        }
        return strAddr;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            // 格式化日志
            String logStr = format(ctx, "REGISTERED");
            logger.log(internalLevel, logStr);
            // 进行回调
            if (Objects.nonNull(loggingCallBackFunc)) {
                this.loggingCallBackFunc.strLogCall(ctx.channel().id(), "REGISTERED", logStr, logAddr(ctx));
            }
        }
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            // 格式化日志
            String logStr = format(ctx, "UNREGISTERED");
            logger.log(internalLevel, logStr);
            // 进行回调
            if (Objects.nonNull(loggingCallBackFunc)) {
                this.loggingCallBackFunc.strLogCall(ctx.channel().id(), "UNREGISTERED", logStr, logAddr(ctx));
            }
        }
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            // 格式化日志
            String logStr = format(ctx, "ACTIVE");
            logger.log(internalLevel, logStr);
            // 进行回调
            if (Objects.nonNull(loggingCallBackFunc)) {
                this.loggingCallBackFunc.strLogCall(ctx.channel().id(), "UNREGISTERED", logStr, logAddr(ctx));
            }
        }
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "INACTIVE"));
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            // 格式化日志
            String logStr = format(ctx, "EXCEPTION", cause);
            logger.log(internalLevel, logStr, cause);
            // 进行回调
            if (Objects.nonNull(loggingCallBackFunc)) {
                this.loggingCallBackFunc.strLogCall(ctx.channel().id(), "EXCEPTION", logStr, logAddr(ctx));
            }
        }
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "USER_EVENT", evt));
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "BIND", localAddress));
        }
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(
            ChannelHandlerContext ctx,
            SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            // 格式化日志
            String logStr = format(ctx, "CONNECT", remoteAddress, localAddress);
            logger.log(internalLevel, logStr);
            // 进行回调
            if (Objects.nonNull(loggingCallBackFunc)) {
                this.loggingCallBackFunc.strLogCall(ctx.channel().id(), "CONNECT", logStr, logAddr(ctx));
            }
        }
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "DISCONNECT"));
        }
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            // 格式化日志
            String logStr = format(ctx, "CLOSE");
            logger.log(internalLevel, logStr);
            // 进行回调
            if (Objects.nonNull(loggingCallBackFunc)) {
                this.loggingCallBackFunc.strLogCall(ctx.channel().id(), "CLOSE", logStr, logAddr(ctx));
            }
        }
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "DEREGISTER"));
        }
        ctx.deregister(promise);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "READ COMPLETE"));
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            // 格式化日志
            String logStr = format(ctx, "READ", msg);
            logger.log(internalLevel, logStr);
            // 进行回调
            if (Objects.nonNull(loggingCallBackFunc)) {
                this.loggingCallBackFunc.strLogCall(ctx.channel().id(), "READ", logStr, logAddr(ctx));
            }
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            // 格式化日志
            String logStr = format(ctx, "WRITE", msg);
            logger.log(internalLevel, logStr);
            // 进行回调
            if (Objects.nonNull(loggingCallBackFunc)) {
                this.loggingCallBackFunc.strLogCall(ctx.channel().id(), "WRITE", logStr, logAddr(ctx));
            }
        }
        ctx.write(msg, promise);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "WRITABILITY CHANGED"));
        }
        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "FLUSH"));
        }
        ctx.flush();
    }

    /**
     * Formats an event and returns the formatted message.
     *
     * @param eventName the name of the event
     */
    protected String format(ChannelHandlerContext ctx, String eventName) {
        // 获取项目信息
        String peStr = this.pe.toStr();
        String chStr = ctx.channel().toString();
        return new StringBuilder(chStr.length() + 1 + eventName.length() + peStr.length())
                .append(peStr)
                .append(chStr)
                .append(' ')
                .append(eventName)
                .toString();
    }

    /**
     * Formats an event and returns the formatted message.
     *
     * @param eventName the name of the event
     * @param arg       the argument of the event
     */
    protected String format(ChannelHandlerContext ctx, String eventName, Object arg) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.pe.toStr());
        if (arg instanceof ByteBuf) {
            sb.append(formatByteBuf(ctx, eventName, (ByteBuf) arg));
        } else if (arg instanceof ByteBufHolder) {
            sb.append(formatByteBufHolder(ctx, eventName, (ByteBufHolder) arg));
        } else {
            sb.append(formatSimple(ctx, eventName, arg));
        }
        return sb.toString();
    }

    /**
     * Formats an event and returns the formatted message.  This method is currently only used for formatting
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}.
     *
     * @param eventName the name of the event
     * @param firstArg  the first argument of the event
     * @param secondArg the second argument of the event
     */
    protected String format(ChannelHandlerContext ctx, String eventName, Object firstArg, Object secondArg) {
        if (secondArg == null) {
            return formatSimple(ctx, eventName, firstArg);
        }
        // pe
        String peStr = this.pe.toStr();
        String chStr = ctx.channel().toString();
        String arg1Str = String.valueOf(firstArg);
        String arg2Str = secondArg.toString();
        StringBuilder buf = new StringBuilder(
                peStr.length() + chStr.length() + 1 + eventName.length() + 2 + arg1Str.length() + 2 + arg2Str.length());
        buf.append(peStr).append(chStr).append(' ').append(eventName).append(": ").append(arg1Str).append(", ").append(arg2Str);
        return buf.toString();
    }

    /**
     * Generates the default log message of the specified event whose argument is a {@link ByteBuf}.
     */
    private String formatByteBuf(ChannelHandlerContext ctx, String eventName, ByteBuf msg) {
        String chStr = ctx.channel().toString();
        int length = msg.readableBytes();
        if (length == 0) {
            StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 4);
            buf.append(chStr).append(' ').append(eventName).append(": 0B");
            return buf.toString();
        } else {
            int outputLength = chStr.length() + 1 + eventName.length() + 2 + 10 + 1;
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
                int hexDumpLength = 2 + rows * 80;
                outputLength += hexDumpLength;
            }
            StringBuilder buf = new StringBuilder(outputLength);
            buf.append(chStr).append(' ').append(eventName).append(": ").append(length).append('B');
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                buf.append(NEWLINE);
                appendPrettyHexDump(buf, msg);
            }

            return buf.toString();
        }
    }

    /**
     * Generates the default log message of the specified event whose argument is a {@link ByteBufHolder}.
     */
    private String formatByteBufHolder(ChannelHandlerContext ctx, String eventName, ByteBufHolder msg) {
        String chStr = ctx.channel().toString();
        String msgStr = msg.toString();
        ByteBuf content = msg.content();
        int length = content.readableBytes();
        if (length == 0) {
            StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length() + 4);
            buf.append(chStr).append(' ').append(eventName).append(", ").append(msgStr).append(", 0B");
            return buf.toString();
        } else {
            int outputLength = chStr.length() + 1 + eventName.length() + 2 + msgStr.length() + 2 + 10 + 1;
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
                int hexDumpLength = 2 + rows * 80;
                outputLength += hexDumpLength;
            }
            StringBuilder buf = new StringBuilder(outputLength);
            buf.append(chStr).append(' ').append(eventName).append(": ")
                    .append(msgStr).append(", ").append(length).append('B');
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                buf.append(NEWLINE);
                appendPrettyHexDump(buf, content);
            }

            return buf.toString();
        }
    }

    /**
     * Generates the default log message of the specified event whose argument is an arbitrary object.
     */
    private static String formatSimple(ChannelHandlerContext ctx, String eventName, Object msg) {
        String chStr = ctx.channel().toString();
        String msgStr = String.valueOf(msg);
        StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length());
        return buf.append(chStr).append(' ').append(eventName).append(": ").append(msgStr).toString();
    }
}
