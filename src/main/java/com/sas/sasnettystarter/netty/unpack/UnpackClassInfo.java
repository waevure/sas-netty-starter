package com.sas.sasnettystarter.netty.unpack;

import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;

/**
 * Netty粘包拆包
 * @author wqy
 * @version 1.0
 * @date 2020/9/12 11:23
 */
public interface UnpackClassInfo {

    /**
     * 构建定长拆包器
     * @return
     */
    static Class fixedLengthFrameDecoder(){
        return FixedLengthFrameDecoder.class;
    }
    /**
     * 构建行拆包器
     * @return
     */
    static Class lineBasedFrameDecoder(){
        return LineBasedFrameDecoder.class;
    }


    /**
     * 构建分隔符拆包器
     * @return
     */
    static Class delimiterBasedFrameDecoder(){
        return DelimiterBasedFrameDecoder.class;
    }

    /**
     * 构建分隔符拆包器
     * @return
     */
    static Class lengthFieldBasedFrameDecoder(){

        return LengthFieldBasedFrameDecoder.class;
    }



}
