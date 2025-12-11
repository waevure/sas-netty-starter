package com.sas.sasnettystarter.netty.task;

import cn.hutool.core.util.ObjectUtil;
import com.sas.sasnettystarter.netty.task.node.TaskNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @ClassName: TaskQueue
 * @Description: 任务队列
 * @Author: Wqy
 * @Date: 2024-05-21 16:34
 * @Version: 1.0
 **/
@Slf4j
public class TaskQueue {

    // 创建无界队列
    public static BlockingQueue<TaskNode> TASK_QUEUE = new LinkedBlockingQueue<>();

    // 任务执行
    public void taskExecute(ScheduledExecutorService scheduledExecutorService) throws InterruptedException {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            log.debug("任务队列扫描:{}", TaskQueue.TASK_QUEUE.size());
            TaskNode node = null;
            try {
                // 创建重入队列数组
                List<TaskNode> reenTryList = new ArrayList<>();
                // 获取此时队列数量
                int size = TaskQueue.TASK_QUEUE.size();
                // 每次只处理size个
                for (int i = 0; i < size; i++) {
                    // poll等待100毫秒
                    node = TaskQueue.TASK_QUEUE.poll(100, TimeUnit.MILLISECONDS);
                    // 如果node为空，说明已经没了，直接跳出
                    if (ObjectUtil.isNull(node)) {
                        break;
                    }
                    // 获取当前时间
                    Long nowTime = System.currentTimeMillis();
                    // 判断是否过了延时时间
                    if (Objects.nonNull(node.getDelayTime())){
                        if (!(nowTime > (node.getStartTime() + node.getDelayTime()))) {
                            // 不满足则加入重入数组
                            reenTryList.add(node);
                            log.info("任务不满足延时条件，放回队列:{}", node.getName());
                            continue;
                        }
                    }

                    // 到期时间
                    if (Objects.nonNull(node.getExpireTime())){
                        // 判断数据是否过期
                        if (nowTime > (node.getStartTime() + node.getExpireTime())) {
                            // 超过则过期
                            log.warn("任务过期:{}", node.getName());
                            return;
                        }
                    }

                    // 满足上述条件则递归
                    // 0-重入队列，1-执行成功，2-任务过期，3-执行失败
                    Integer rs = recursionExecute(node, nowTime);
                    if (1 == rs) {
                        log.info("任务执行成功:{}", node.getName());
                    } else if (0 == rs) {
                        reenTryList.add(node);
                        log.warn("RS任务不满足延时条件，放回队列:{}", node.getName());
                    } else if (2 == rs) {
                        log.warn("存在过期任务:{}", node.getName());
                    } else if (3 == rs) {
                        log.warn("存在执行失败节点:{}", node.getName());
                    }
                }
                // 放回队列
                reenTryList.stream().forEach(s -> TaskQueue.TASK_QUEUE.add(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 2, TimeUnit.SECONDS);
    }

    /**
     * 递归执行
     *
     * @param node
     * @param nowTime
     * @return 0-重入队列，1-执行成功，2-任务过期，3-执行失败
     */
    public Integer recursionExecute(TaskNode node, Long nowTime) {

        // 不为空才处理
        if (ObjectUtil.isNotNull(node.getDelayTime())) {
            // 判断是否过了延时时间
            if (!(nowTime > (node.getStartTime() + node.getDelayTime()))) {
                // 不满足则加入重入数组
                return 0;
            }
        }

        // 不为空才处理
        if (ObjectUtil.isNotNull(node.getExpireTime())) {
            // 判断数据是否过期
            if (nowTime > (node.getStartTime() + node.getExpireTime())) {
                // 超过则过期
                return 2;
            }
        }

        // 执行一次性方法
        if (ObjectUtil.isNotNull(node.getOnceFuncExecute())) {
            if (!node.getOnceFuncExecute()) {
                node.setOnceFuncExecute(true);
                node.getOnceFunc().execute();

            }
        }

        if (node.execute()) {
            // 为空直接返回
            if (ObjectUtil.isNull(node.getNextNode())) {
                return 1;
            }
            return recursionExecute(node.getNextNode(), nowTime);
        } else {
            log.warn("任务执行失败:{}", node.getName());
            return 3;
        }
    }

}
