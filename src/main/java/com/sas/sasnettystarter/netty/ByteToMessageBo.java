package com.sas.sasnettystarter.netty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 解码Bo
 *
 * @author WQY
 * @version 1.0
 * @date 2024/1/3 17:27
 */
@Slf4j
@Data
@AllArgsConstructor
public class ByteToMessageBo {

    /**
     * 数据长度
     */
    private final Integer length;

    /**
     * 消息体
     */
    private final StringBuffer buffer;

    /**
     * 数据存入时间
     */
    private final long messageTime = System.currentTimeMillis();

    /**
     * 数据有效时长
     */
    private final long MESSAGE_TIME_CONSTANT = 10 * 1000;

    /**
     * 数据是否有效
     *
     * @return
     */
    public boolean messageValidity() {
        //时效验证
        if (System.currentTimeMillis() - this.getMessageTime() > MESSAGE_TIME_CONSTANT) {
            return false;
        }
        return true;
    }

}
