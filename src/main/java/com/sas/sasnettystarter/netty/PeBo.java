package com.sas.sasnettystarter.netty;

import lombok.Data;
import lombok.Getter;

/**
 * @ClassName: PeBo
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-26 15:44
 * @Version: 1.0
 **/
@Getter
public class PeBo {

    /**
     * 项目信息
     */
    private ProjectAbstract pe;

    public PeBo() {
    }

    public PeBo(ProjectAbstract pe){
        this.pe = pe;
    }

}
