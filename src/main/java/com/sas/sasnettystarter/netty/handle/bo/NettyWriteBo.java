package com.sas.sasnettystarter.netty.handle.bo;

import com.sas.sasnettystarter.netty.ProjectAbstract;

/**
 * netty写操作Bo父类
 *
 * @author WQY
 * @version 1.0
 * @date 2023/12/2 10:43
 */
public class NettyWriteBo extends NettyBo {

    public String ip;

    public Integer port;

    public String msg;

    public NettyWriteBo(ProjectAbstract pe) {
        super(pe);
    }

    public NettyWriteBo(ProjectAbstract pe, String ip, Integer port) {
        super(pe);
        this.ip = ip;
        this.port = port;
    }

    public NettyWriteBo(ProjectAbstract pe, String ip, Integer port, String msg) {
        super(pe);
        this.ip = ip;
        this.port = port;
        this.msg = msg;
    }

    /**
     * 获取ip:port
     *
     * @return
     */
    public String ipPortStr() {
        return this.ip + ":" + this.port;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getIp() {
        return this.ip;
    }

    public Integer getPort() {
        return this.port;
    }


}
