package com.sas.sasnettystarter.netty.handle.bo;

/**
 * netty写操作Bo父类
 * @author WQY
 * @version 1.0
 * @date 2023/12/2 10:43
 */
public class NettyWriteBo extends NettyBo{

    public String ip;

    public Integer port;

    public String msg;

    public NettyWriteBo() {
    }

    public NettyWriteBo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public NettyWriteBo(String ip, Integer port, String msg) {
        this.ip = ip;
        this.port = port;
        this.msg = msg;
    }

    /**
     * 获取ip:port
     * @return
     */
    public String ipPortStr(){
        return this.ip + ":" + this.port;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg(){
        return this.msg;
    }

    public String getIp(){
        return this.ip;
    }

    public Integer getPort(){
        return this.port;
    }


}
