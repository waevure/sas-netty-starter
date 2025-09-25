package udp.client;

import com.sas.sasnettystarter.netty.handle.PacketEncoder;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @ClassName: StringCusWriter
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 16:59
 * @Version: 1.0
 **/
public class StringCusUdpClientWriter extends PacketEncoder<NettyWriteBo> {
    @Override
    protected void encode(ChannelHandlerContext ctx, NettyWriteBo msg, ByteBuf out) throws Exception {
        // 客户端刚连上时，主动发一条消息
        String welcome = "你还好吗，客户端!\n";
        // 发送数据到服务端
        ctx.channel().writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer("你好服务端！", CharsetUtil.UTF_8),
                new InetSocketAddress(msg.getIp(), msg.getPort())));
    }
}
