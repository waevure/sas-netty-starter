package tcp.client;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.handle.ReadHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName: StringCusReader
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 16:46
 * @Version: 1.0
 **/
public class StringCusClientReader extends ReadHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        String ipPort = NetAddress.nettyRemoteAddress(ctx.channel()).ipPort();
        // ByteBuf -> byte[]
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        // 你可以自己定义协议解析逻辑
        String data = new String(bytes); // 这里只是演示
        System.out.println("收到原始数据: " + data);
        // 写入
        // 客户端刚连上时，主动发一条消息
        String welcome = "欢迎发送数据到服务端!\n";
        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(welcome.getBytes()));
    }
}
