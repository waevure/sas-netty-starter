package com.sas.sasnettystarter.netty.unpack;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 拆包信息
 * 类信息用法参照{@link Unpack}
 *
 * @author wqy
 * @version 1.0
 * @date 2020/11/17 10:58
 */
@Data
@Slf4j
public class Unpacking {

    /**
     * 定长拆包构构造
     *
     * @param frameLengthOrMaxLength
     */
    public Unpacking(int frameLengthOrMaxLength) {
        this.frameLengthOrMaxLength = frameLengthOrMaxLength;
        this.type = 1;
    }

    /**
     * 分隔符拆包
     *
     * @param maxLength
     * @param separator
     */
    public Unpacking(int maxLength, String separator) {
        this.frameLengthOrMaxLength = maxLength;
        this.separator = separator;
        this.type = 2;
    }

    /**
     * 分隔符拆包
     *
     * @param maxLength
     * @param separator
     */
    public Unpacking(int maxLength, byte[] separator) {
        this.frameLengthOrMaxLength = maxLength;
        this.separatorBytes = separator;
        this.type = 2;
    }

    /**
     * 定长拆包
     *
     * @param maxLength
     * @param lengthFieldOffset 长度域偏移量，就是长度域的位置从第几个字节开始
     * @param lengthFieldLength 长度域的字节数
     */
    public Unpacking(int maxLength, int lengthFieldOffset, int lengthFieldLength) {
        this.frameLengthOrMaxLength = maxLength;
        this.lengthFieldOffset = lengthFieldOffset;
        this.lengthFieldLengt = lengthFieldLength;
        this.type = 3;
    }

    /**
     * 自定义解码器
     */
    public Unpacking(Class<? extends ByteToMessageDecoder> decoderClass) {
        this.decoderClass = decoderClass;
        this.type = 4;
    }

    /**
     * 类型
     */
    private int type;
    /**
     * 定长拆包长度或一帧的最大长度
     */
    private Integer frameLengthOrMaxLength;

    /**
     * 分隔符
     */
    private String separator;

    /**
     * 分隔符
     */
    private byte[] separatorBytes;

    /**
     * 长度域偏移量，就是长度域的位置从第几个字节开始
     */
    private Integer lengthFieldOffset;

    /**
     * 长度域的字节数
     */
    private Integer lengthFieldLengt;

    /**
     * 解码器
     */
    private Class<? extends ByteToMessageDecoder> decoderClass;

    /**
     * 构建handler
     *
     * @return
     */
    public ChannelHandler buildHandler() throws Exception {

        ChannelHandler channelHandler = null;

        if (this.type == 1) {
            channelHandler = Unpack.fixedLengthFrameDecoder(this.frameLengthOrMaxLength);
        } else if (this.type == 2) {
            if (this.separator != null){
                channelHandler = Unpack.delimiterBasedFrameDecoder(this.frameLengthOrMaxLength, this.separator);
            }else {
                channelHandler = Unpack.delimiterBasedFrameDecoder(this.frameLengthOrMaxLength, this.separatorBytes);
            }
        } else if (this.type == 3) {
            channelHandler = Unpack.lengthFieldBasedFrameDecoder(this.frameLengthOrMaxLength, this.lengthFieldOffset, this.lengthFieldLengt);
        } else if (this.type == 4) {
            channelHandler = decoderClass.newInstance();
        } else {
            channelHandler = Unpack.lineBasedFrameDecoder(this.frameLengthOrMaxLength);
        }

        return channelHandler;
    }

}
