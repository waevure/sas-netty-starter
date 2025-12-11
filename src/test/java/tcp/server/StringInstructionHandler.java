package tcp.server;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.TiFunction;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 指令状态
 *
 * @author WQY
 * @version 1.0
 * @date 2024/1/22 16:23
 */
@Slf4j
public class StringInstructionHandler implements TiFunction<ChannelHandlerContext, ByteBuf, ProjectAbstract, Boolean> {

    @Override
    public Boolean apply(ChannelHandlerContext ctx, ByteBuf msg, ProjectAbstract pa) {
        String ipPort = NetAddress.nettyRemoteAddress(ctx.channel()).ipPort();
        // 添加缓存
        NettyTcpServerGuide.tcpServerOperations(new NettyTcpServerProject("TCP服务端", "10001")).putCtx(ipPort, ctx.channel());
        // ByteBuf -> byte[]
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        // 你可以自己定义协议解析逻辑
        String data = new String(bytes); // 这里只是演示
        System.out.println("收到原始数据: " + data);
        // 写入
        // 客户端刚连上时，主动发一条消息
        String welcome = "欢迎连接服务器!\n";
        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(welcome.getBytes()));
        return true;
    }
}
