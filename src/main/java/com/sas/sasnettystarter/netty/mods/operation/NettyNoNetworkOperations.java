package com.sas.sasnettystarter.netty.mods.operation;

import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;

/**
 * @InterfaceName: NettyNoNetworkOperations
 * @Description: netty-无网络的channel
 * @Author: Wqy
 * @Date: 2025-09-24 15:15
 * @Version: 1.0
 **/
public interface NettyNoNetworkOperations {

    /**
     * 下发指令
     * @param writeData
     */
    void distributeInstruct(NettyWriteBo writeData);

}
