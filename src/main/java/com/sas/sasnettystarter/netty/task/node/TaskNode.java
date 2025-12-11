package com.sas.sasnettystarter.netty.task.node;

import com.sas.sasnettystarter.netty.task.TaskFunction;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: TaskNode
 * @Description: 任务节点
 * @Author: Wqy
 * @Date: 2024-05-21 16:01
 * @Version: 1.0
 **/
@Data
@Slf4j
public class TaskNode {

    // 任务名称
    private String name;
    // 启动时间/创建时间，时间戳
    private Long startTime;
    // 过期时间，单位毫秒
    private Long expireTime;
    // 延时时间
    private Long delayTime;
    // 是否正在执行
    private Boolean running = false;
    // 轮询执行方法函数
    private TaskFunction taskFunction;
    // 只调用一次的方法,本次任务周期内只调用一次的方法
    private TaskFunction onceFunc;
    // onceFunc是否执行
    private Boolean onceFuncExecute = null;
    // 下一个节点
    private TaskNode nextNode;

    public TaskNode() {
        this.startTime = System.currentTimeMillis();
    }

    public TaskNode name(String name) {
        this.name = name;
        return this;
    }

    public TaskNode expireTime(Long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public TaskNode delayTime(Long delayTime) {
        this.delayTime = delayTime;
        return this;
    }

    public TaskNode running(Boolean running) {
        this.running = running;
        return this;
    }

    public TaskNode taskFunction(TaskFunction taskFunction) {
        this.taskFunction = taskFunction;
        return this;
    }

    public TaskNode onceFunc(TaskFunction onceFunc) {
        this.onceFunc = onceFunc;
        this.onceFuncExecute = false;
        return this;
    }

    // 添加下一个节点
    public TaskNode addNextNode(TaskNode nextNode) {
        this.nextNode = nextNode;
        return this.nextNode;
    }

    public boolean execute() {
        if (this.taskFunction.execute()) {
            log.debug("{}---执行成功", this.name);
            return true;
        }
        log.error("{}---执行失败", this.name);
        return false;
    }


}
