package com.sas.sasnettystarter.netty.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 解码父类
 * 当我们继承了 PacketDecoder 这个类之后，
 * 我们只需要实现一下 decode() 方法，这里的 in 大家可以看到，
 * 传递进来的时候就已经是 ByteBuf 类型，所以我们不再需要强转，
 * 第三个参数是 List 类型，我们通过往这个 List 里面添加解码后的结果对象，
 * 就可以自动实现结果往下一个 handler 进行传递，这样，我们就实现了解码的逻辑 handler。
 * 另外，值得注意的一点，对于 Netty 里面的 ByteBuf，我们使用 4.1.6.Final 版本，
 * 默认情况下用的是堆外内存，在 ByteBuf 这一小节中我们提到，
 * 堆外内存我们需要自行释放，在我们前面小节的解码的例子中，
 * 其实我们已经漏掉了这个操作，这一点是非常致命的，随着程序运行越来越久，
 * 内存泄露的问题就慢慢暴露出来了， 而这里我们使用 ByteToMessageDecoder，
 * Netty 会自动进行内存的释放，我们不用操心太多的内存管理方面的逻辑
 * @author wqy
 * @version 1.0
 * @date 2020/9/14 10:03
 */
public abstract class PacketDecoder extends ByteToMessageDecoder {

    /**
     * 读取in中的数据，然后写入到out中，传递到下一个方法。
     * 链路传递数据的时候ReadHandler<T>会根据Object类型进行匹配
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
}
