package com.sas.sasnettystarter.netty.task.node;

import cn.hutool.core.util.ObjectUtil;

/**
 * @ClassName: TaskBuild
 * @Description: 任务构建
 * @Author: Wqy
 * @Date: 2024-05-21 16:14
 * @Version: 1.0
 **/
public class TaskBuild {

    // 开始node
    private TaskNode startNode;
    // 结束node
    private TaskNode endNode;

    // 添加节点
    public TaskBuild addTaskNode(TaskNode taskNode) {
        if (ObjectUtil.isNull(startNode)){
            this.startNode = taskNode;
        }else {
            this.endNode.addNextNode(taskNode);
        }
        this.endNode = taskNode;
        return this;
    }

    public TaskNode build(){
        return startNode;
    }

}
