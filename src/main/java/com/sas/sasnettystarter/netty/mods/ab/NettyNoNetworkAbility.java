package com.sas.sasnettystarter.netty.mods.ab;

import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;

/**
 * @InterfaceName: NettyNoNetworkAbility
 * @Description: netty-无网络的channel
 * @Author: Wqy
 * @Date: 2025-09-24 15:15
 * @Version: 1.0
 **/
public interface NettyNoNetworkAbility {

    /**
     * 下发指令
     * @param writeData
     */
    void distributeInstruct(NettyWriteBo writeData);

}
