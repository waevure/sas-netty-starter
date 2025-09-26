package com.sas.sasnettystarter.netty.ops.embedded;

import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.ops.core.NettyServerBaseContext;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: NettyNoNetworkChannel
 * @Description: 无网络channel
 * @Author: Wqy
 * @Date: 2024-11-26 15:17
 * @Version: 1.0
 **/
@Data
@Slf4j
public class NettyNoNetworkChannel extends NettyServerBaseContext implements NettyNoNetworkOperations {

    private EmbeddedChannel channel;

    public NettyNoNetworkChannel(EmbeddedChannel channel, ProjectAbstract pe, NettyType type) {
        super(pe, type);
        this.channel = channel;
    }

    @Override
    public void distributeInstruct(NettyWriteBo writeData) {
        this.channel.writeInbound(writeData);
    }

    @Override
    public void awaitCloseSync(Integer port) {

    }

    @Override
    public boolean destroyServer() {
        log.info("Netty无网络channel开始销毁:{}", this);
        this.channel.close();
        // 销毁variable
        this.getVariable().destroy(this.getPe());
        log.info("Netty无网络channel销毁完成:{}", this);
        return true;
    }
}
