package com.sas.sasnettystarter.netty.ops.tcp;

import com.sas.sasnettystarter.netty.handle.bo.NettyReadBo;
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
     * 执行netty的inbound
     * @param key
     * @param readBo
     */
    void distributeInInstruct(String key, NettyReadBo readBo);

    /**
     * 下发指令
     * 执行netty的inbound
     * @param ctx
     * @param readBo
     */
    void distributeInInstruct(ChannelHandlerContext ctx, NettyReadBo readBo);

    /**
     * 下发指令
     * 执行netty的outbound
     * @param key
     * @param writeData
     */
    void distributeOutInstruct(String key, NettyWriteBo writeData);

    /**
     * 下发指令
     * 执行netty的outbound
     * @param ctx
     * @param writeData
     */
    void distributeOutInstruct(ChannelHandlerContext ctx, NettyWriteBo writeData);

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
