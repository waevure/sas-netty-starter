package com.sas.sasnettystarter.netty.mods.ab;

import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;

/**
 * @InterfaceName: NettyUdpAbility
 * @Description: netty-udp能力
 * @Author: Wqy
 * @Date: 2025-09-24 15:15
 * @Version: 1.0
 **/
public interface NettyUdpAbility {

    /**
     * 下发指令
     * @param writeData 下发数据体
     */
    void distributeInstruct(NettyWriteBo writeData);


}
