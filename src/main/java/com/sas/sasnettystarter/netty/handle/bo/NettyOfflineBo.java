package com.sas.sasnettystarter.netty.handle.bo;

import com.sas.sasnettystarter.netty.ProjectAbstract;
import lombok.Data;

/**
 * @ClassName: NettyOfflineBo
 * @Description: 离线通知
 * @Author: Wqy
 * @Date: 2025-09-26 14:15
 * @Version: 1.0
 **/
@Data
public class NettyOfflineBo extends NettyBo {

    private Long offlineTime = System.currentTimeMillis();

    public NettyOfflineBo(ProjectAbstract pe) {
        super(pe);
    }
}
