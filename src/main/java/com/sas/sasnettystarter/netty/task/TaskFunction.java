package com.sas.sasnettystarter.netty.task;

/**
 * @InterfaceName: TaskFunction
 * @Description: 任务执行函数
 * @Author: Wqy
 * @Date: 2024-05-21 16:04
 * @Version: 1.0
 **/
@FunctionalInterface
public interface TaskFunction {

    // 执行函数
    boolean execute();

}
