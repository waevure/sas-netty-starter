package udp.client;

import com.sas.sasnettystarter.netty.handle.ReadHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * @ClassName: StringCusReader
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 16:46
 * @Version: 1.0
 **/
public class StringCusUdpClientReader extends ReadHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String msg = packet.content().toString(io.netty.util.CharsetUtil.UTF_8);
        System.out.println("服务端收到: " + msg + " 来自 " + packet.sender());

        // 回复客户端
        String response = "服务端已收到: " + msg;
        DatagramPacket respPacket = new DatagramPacket(
                Unpooled.copiedBuffer(response, io.netty.util.CharsetUtil.UTF_8),
                packet.sender() // 必须指定目标地址
        );
        ctx.writeAndFlush(respPacket);
    }
}
