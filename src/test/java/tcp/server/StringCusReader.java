package tcp.server;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.ReadHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: StringCusReader
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 16:46
 * @Version: 1.0
 **/
@Slf4j
public class StringCusReader extends ReadHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        String ipPort = IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort();
        // ByteBuf -> byte[]
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        // 你可以自己定义协议解析逻辑
        String data = new String(bytes); // 这里只是演示
        log.info("收到原始数据: " + data);
        // 写入
        // 客户端刚连上时，主动发一条消息
        String welcome = "欢迎连接服务器!\n";
        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(welcome.getBytes()));
    }
}
