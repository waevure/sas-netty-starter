package com.sas.sasnettystarter.netty.handle.bo;

import com.sas.sasnettystarter.netty.ProjectAbstract;
import lombok.Data;

/**
 * @ClassName: NettyOnlineBo
 * @Description: 上线通知
 * @Author: Wqy
 * @Date: 2025-09-26 14:15
 * @Version: 1.0
 **/
@Data
public class NettyOnlineBo extends NettyBo {

    private Long onlineTime = System.currentTimeMillis();

    public NettyOnlineBo(ProjectAbstract pe) {
        super(pe);
    }
}
