package com.sas.sasnettystarter.netty.service;

import java.util.List;

/**
 * 业务处理接口
 * 业务程序中需要实现对应的处理，不然解析完无法处理
 * 子类可以采用bean的方式注入
 * 然后
 *
 * @author WQY
 * @version 1.0
 * @date 2024/1/18 15:16
 */
public interface ServiceHandler<T extends ServiceData> {


    /**
     * 处理类的接口需要在次进行验证是否可用
     *
     * @param type 类型
     * @return
     */
    boolean support(InstructServiceInterface type);

    /**
     * 处理单对象
     * 但对象和多对象按需实现即可
     * @param data 数据
     */
    void handle(T data);

    /**
     * 处理多对象
     * 但对象和多对象按需实现即可
     * @param data 数据
     */
    void handle(List<T> data);

}
