package nonetworkchannel;

import com.sas.sasnettystarter.netty.handle.PacketEncoder;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.utils.GsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: StringCusWriter
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 16:59
 * @Version: 1.0
 **/
@Slf4j
public class StringCusNoNetworkWriter extends PacketEncoder<NettyWriteBo> {
    @Override
    protected void encode(ChannelHandlerContext ctx, NettyWriteBo msg, ByteBuf out) throws Exception {
        log.info(GsonUtils.toString(msg));
        // 客户端刚连上时，主动发一条消息
        String welcome = "你还好吗，客户端!\n";
        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(welcome.getBytes()));
    }
}
