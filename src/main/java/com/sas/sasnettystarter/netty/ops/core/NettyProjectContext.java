package com.sas.sasnettystarter.netty.ops.core;

import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty项目信息
 *
 * 主要用于管理这个netty是干什么用的
 *
 * @author WQY
 * @version 1.0
 * @date 2023/12/12 09:49
 */
@Slf4j
@Getter
public abstract class NettyProjectContext {

    /**
     * 项目信息
     */
    private ProjectAbstract pe;

    /**
     * netty类型
     */
    private NettyType nettyType;



    public NettyProjectContext() {
    }

    public NettyProjectContext(ProjectAbstract pe, NettyType nettyType) {
        this.pe = pe;
        this.nettyType = nettyType;
    }
}
