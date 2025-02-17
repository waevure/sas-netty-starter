package com.sas.sasnettystarter.netty.mods;

import com.sas.sasnettystarter.netty.NettyType;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: NettyNoNetworkChannelMods
 * @Description: 无网络channel
 * @Author: Wqy
 * @Date: 2024-11-26 15:17
 * @Version: 1.0
 **/
@Data
@Slf4j
public class NettyNoNetworkChannelMods extends NettyMods {

    private EmbeddedChannel channel;

    public NettyNoNetworkChannelMods(EmbeddedChannel channel, NettyType type) {
        this.channel = channel;
        this.nettyType = type;
    }

    /**
     * 写入通道
     * @param bytes
     * @return
     */
    public Boolean writeInbound(byte[] bytes) {
        return channel.writeInbound(Unpooled.copiedBuffer(bytes));
    }

    @Override
    public boolean destroyServer() {
        log.info("Netty无网络channel开始销毁:{}",this);
        this.channel.close();
        // 销毁variable
        this.variable.destroy();
        log.info("Netty无网络channel销毁完成:{}",this);
        return true;
    }
}
