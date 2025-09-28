package com.sas.sasnettystarter.netty.handle.bo;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.ProjectAbstract;

/**
 * netty写操作Bo父类
 *
 * @author WQY
 * @version 1.0
 * @date 2023/12/2 10:43
 */
public class NettyWriteBo extends NettyBo {

    private NetAddress netAddress;

    public String msg;

    public NettyWriteBo(ProjectAbstract pe) {
        super(pe);
    }

    public NettyWriteBo(ProjectAbstract pe, NetAddress netAddress) {
        super(pe);
    }

    public NettyWriteBo(ProjectAbstract pe, NetAddress netAddress, String msg) {
        super(pe);
        this.netAddress = netAddress;
        this.msg = msg;
    }

    /**
     * 获取ip:port
     *
     * @return
     */
    public String ipPortStr() {
        return netAddress.ipPort();
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getIp() {
        return this.getNetAddress().getIp();
    }

    public Integer getPort() {
        return this.getNetAddress().getPort();
    }

    public NetAddress getNetAddress() {
        return netAddress;
    }
}
