package com.sas.sasnettystarter.netty.ops.tcp;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;

/**
 * @InterfaceName: NettyTcpClientOperations
 * @Description: netty-tcp客户端能力
 * @Author: Wqy
 * @Date: 2025-09-24 15:15
 * @Version: 1.0
 **/
public interface NettyTcpClientOperations {

    /**
     * 同步连接
     * @param ipPortAddress
     * @return
     */
    Boolean connectSync(IpPortAddress ipPortAddress);

    /**
     * 下发指令
     * @param key          服务端的ip:port
     * @param writeData
     */
    void distributeInstruct(String key, NettyWriteBo writeData);

    /**
     * 销毁TcpClient服务
     * 释放客户端连接
     * @return
     */
    boolean destroyServer();
}
