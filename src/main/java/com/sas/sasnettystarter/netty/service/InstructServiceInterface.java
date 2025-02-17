package com.sas.sasnettystarter.netty.service;

/**
 * 业务指令接口
 * 子类实现
 * 推荐使用枚举实现
 * @author WQY
 * @version 1.0
 * @date 2024/1/18 15:39
 */
public interface InstructServiceInterface {

    /**
     * 获取指令
     * @return
     */
    String instruct();


}
