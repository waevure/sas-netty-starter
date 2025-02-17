package com.sas.sasnettystarter.netty.handle.bo;

/**
 * netty读操作Domain父类
 * @author WQY
 * @version 1.0
 * @date 2023/12/2 10:43
 */
public abstract class NettyReadBo extends NettyBo{

    public String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
