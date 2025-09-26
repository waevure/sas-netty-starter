package com.sas.sasnettystarter.netty.handle.bo;

import com.sas.sasnettystarter.netty.PeBo;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import lombok.Data;

/**
 * @author WQY
 * @version 1.0
 * @date 2024/1/25 14:35
 */
@Data
public class NettyBo extends PeBo {

    public NettyBo() {
    }

    public NettyBo(ProjectAbstract pe) {
        super(pe);
    }

    /**
     * 增加空格
     */
    public String addSpace(String msg) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < (msg.length()); i = i + 2) {
            if ((i + 2) != msg.length()) {
                buffer.append(msg.substring(i, i + 2)).append(" ");
            } else {
                buffer.append(msg.substring(i, i + 2));
            }
        }
        return buffer.toString();
    }


}
