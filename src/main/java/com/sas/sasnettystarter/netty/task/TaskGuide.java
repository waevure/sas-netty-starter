package com.sas.sasnettystarter.netty.task;

import com.sas.sasnettystarter.netty.task.node.TaskBuild;
import com.sas.sasnettystarter.netty.task.node.TaskNode;

/**
 * TaskNode node = TaskGuide
 *                 .newBuild()
 *                 .addTaskNode(
 *                         new TaskNode()
 *                                 .name("测试任务1")
 *                                 .expireTime(10 * 1000L)
 *                                 .delayTime(5 * 1000L)
 *                                 .taskFunction(()->{ System.out.println("测试任务1"); return false;})
 *                 )
 *                 .addTaskNode(
 *                         new TaskNode()
 *                                 .name("测试任务2")
 *                                 // 过期时间和延时执行时间不填的话遵循第一个进行判断
 *                                 .expireTime(30 * 1000L)
 *                                 .delayTime(20 * 1000L)
 *                                 .taskFunction(()->{ System.out.println("测试任务2"); return true;})
 *                 )
 *                 .build();
 *         TaskGuide.addQueueTask(node);
 * @ClassName: TaskGuide
 * @Description: 作业引导
 * @Author: Wqy
 * @Date: 2024-05-21 15:57
 * @Version: 1.0
 **/
public class TaskGuide {

    // 创建实例
    public static TaskBuild newBuild() {
        return new TaskBuild();
    }

    // 创建任务对象
    public static TaskQueue newTaskQueue(){
        return new TaskQueue();
    }

    /**
     * 添加队列任务任务
     *
     * @param node
     */
    public static void addQueueTask(TaskNode node) {
        TaskQueue.TASK_QUEUE.add(node);
    }
}
