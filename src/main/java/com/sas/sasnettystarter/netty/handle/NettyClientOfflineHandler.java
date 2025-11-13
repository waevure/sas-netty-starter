package com.sas.sasnettystarter.netty.handle;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.handle.bo.NettyOfflineBo;
import com.sas.sasnettystarter.netty.utils.GsonUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: NettyClientOnlineHandler
 * @Description: 客户端下线处理器
 * @Author: Wqy
 * @Date: 2025-09-26 14:25
 * @Version: 1.0
 **/
@Slf4j
public class NettyClientOfflineHandler extends LogicHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof NettyOfflineBo) {
            NettyOfflineBo offline = (NettyOfflineBo) evt;
            log.info("{}-{}-下线: {}",
                    offline.getPe().toStr(),
                    NetAddress.nettyRemoteAddress(ctx.channel()).ipPort(),
                    GsonUtils.toString(offline));
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
