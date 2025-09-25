package com.sas.sasnettystarter.netty.mods.operation;

import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

/**
 * @InterfaceName: NettyTcpServerOperations
 * @Description: netty-tcp服务端能力
 * @Author: Wqy
 * @Date: 2025-09-24 15:15
 * @Version: 1.0
 **/
public interface NettyTcpServerOperations {

    /**
     * 添加ctx缓存
     * @param key
     * @param ctx
     */
    void putCtx(String key, ChannelHandlerContext ctx);

    /**
     * 获取ctx缓存
     * @param key
     * @return
     */
    ChannelHandlerContext getCtx(String key);

    /**
     * 下发指令
     * @param key
     * @param writeData
     */
    void distributeInstruct(String key, NettyWriteBo writeData);

    /**
     * 全部注册通道
     * @return
     */
    Map<String, ChannelHandlerContext> registerClientChannel();

    /**
     * 销毁TcpServer服务
     * @return
     */
    boolean destroyServer();
}
