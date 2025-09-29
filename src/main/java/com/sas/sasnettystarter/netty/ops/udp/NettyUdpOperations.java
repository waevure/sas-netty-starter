package com.sas.sasnettystarter.netty.ops.udp;

import com.sas.sasnettystarter.netty.handle.bo.NettyBo;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;

/**
 * @InterfaceName: NettyUdpOperations
 * @Description: netty-udp能力
 * @Author: Wqy
 * @Date: 2025-09-24 15:15
 * @Version: 1.0
 **/
public interface NettyUdpOperations {

    /**
     * 下发指令
     *
     * @param writeData 下发数据体
     */
    <T extends NettyWriteBo> void distributeInstruct(T writeData);

    /**
     * 下发指令
     *
     * @param writeData 下发数据体
     */
     void distributeObjInstruct(Object writeData);


}
