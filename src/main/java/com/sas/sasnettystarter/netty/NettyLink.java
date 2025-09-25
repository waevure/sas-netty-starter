package com.sas.sasnettystarter.netty;

import com.sas.sasnettystarter.netty.handle.LogicHandler;
import com.sas.sasnettystarter.netty.handle.PacketEncoder;
import com.sas.sasnettystarter.netty.handle.ReadHandler;
import com.sas.sasnettystarter.netty.log.LogMerge;
import com.sas.sasnettystarter.netty.unpack.Unpacking;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * netty链路信息
 *
 * @author WQY
 * @version 1.0
 * @date 2024/1/18 13:44
 */
@Data
public class NettyLink {

    /**
     * server端口号
     * 只有NettyType为S时生效
     */
    private Integer serverPort;

    /**
     * 拆包解包器
     */
    private Unpacking decoder;

    /**
     * 是否启用默认处理器
     */
    private Boolean isOpenDefault = false;

    /**
     * 设置日志
     */
    private LogMerge logMerge = null;

    /**
     * 如果isOpenDefault true该参数才可能生效，该参数可以为空
     */
    private TiFunction<ChannelHandlerContext, Object, ProjectAbstract, Object> defaultFunctionRead;

    /**
     * bootstrap-options
     */
    private LinkedHashMap<ChannelOption<?>, Object> bootstrapOptions = new LinkedHashMap<>();

    /**
     * Pipelines前的处理器，在日志处理器之后构建，也可以在这里面放所有的逻辑处理器
     */
    private List<Function<Channel,Boolean>> beforePipelines = new ArrayList<>();

    /**
     * 指令分发，状态管理器
     * 按添加顺序添加
     */
    private List<Class<? extends LogicHandler>> logicHandlers = new ArrayList<>();

    /**
     * 读逻辑通道
     * 按添加顺序添加
     */
    private List<Class<? extends ReadHandler>> readHandlers = new ArrayList<>();

    /**
     * 写逻辑处理器
     * 也就是响应编码器
     * 按添加顺序添加
     */
    private List<Class<? extends PacketEncoder>> writeHandlers = new ArrayList<>();

    /**
     * 启动成功回调
     */
    private Function<Channel, Boolean> startSuccessCallback;


    /**
     * 添加option
     *
     * @param option
     * @param value
     * @param <T>
     * @return
     */
    public <T> NettyLink addBootstrapOption(ChannelOption<T> option, T value) {
        this.bootstrapOptions.put(option, value);
        return this;
    }

    /**
     * 日志设置
     *
     * @param logMerge
     * @return
     */
    public NettyLink logMerge(LogMerge logMerge) {
        this.logMerge = logMerge;
        return this;
    }

    /**
     * 添加粘包拆包器
     */
    public NettyLink addUnpack(Unpacking decoder) {
        this.decoder = decoder;
        return this;
    }

    /**
     * 开启默认通道状态管理
     *
     * @param function
     * @return
     */
    public <P,R> NettyLink openDefaultChannelStatus(TiFunction<ChannelHandlerContext, P, ProjectAbstract, R> function) {
        this.isOpenDefault = true;
        this.defaultFunctionRead = (TiFunction<ChannelHandlerContext, Object, ProjectAbstract, Object>) function;
        return this;
    }

    /**
     * 开启默认通道状态管理
     *
     * @return
     */
    public <P,R> NettyLink openDefaultChannelStatus() {
        this.isOpenDefault = true;
        return this;
    }

    /**
     * 添加Pipeline前的处理器
     * @param function
     * @return
     */
    public NettyLink addBeforePipeline(Function<Channel,Boolean> function) {
        this.beforePipelines.add(function);
        return this;
    }

    /**
     * 添加指令分发器，通道状态管理器等
     *
     * @param logicHandler
     */
    public NettyLink addLogicHandler(Class<? extends LogicHandler> logicHandler) {
        this.logicHandlers.add(logicHandler);
        return this;
    }

    /**
     * 添加读处理器
     *
     * @param readHandler
     */
    public NettyLink addReadHandler(Class<? extends ReadHandler> readHandler) {
        this.readHandlers.add(readHandler);
        return this;
    }

    /**
     * 添加写处理器(响应编码器)
     *
     * @param writeHandler
     */
    public NettyLink addWriteHandler(Class<? extends PacketEncoder> writeHandler) {
        this.writeHandlers.add(writeHandler);
        return this;
    }

    /**
     * 添加服务端口号
     *
     * @param serverPort
     * @return
     */
    public NettyLink addServerPort(Integer serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    /**
     * 添加成功回调
     * @param function
     * @return
     */
    public NettyLink addStartSuccessCallback(Function<Channel, Boolean> function){
        this.startSuccessCallback = function;
        return this;
    }

}
