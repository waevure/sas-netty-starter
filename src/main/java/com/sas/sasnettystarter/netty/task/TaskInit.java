package com.sas.sasnettystarter.netty.task;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @ClassName: TaskInit
 * @Description: 任务初始化
 * @Author: Wqy
 * @Date: 2024-05-22 09:21
 * @Version: 1.0
 **/
public class TaskInit {

    // 初始化任务
    public void initTask(ScheduledExecutorService scheduledExecutorService){
        try {
            TaskGuide.newTaskQueue().taskExecute(scheduledExecutorService);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
