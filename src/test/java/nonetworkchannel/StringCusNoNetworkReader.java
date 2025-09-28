package nonetworkchannel;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.handle.ReadHandler;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.utils.GsonUtils;
import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName: StringCusReader
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 16:46
 * @Version: 1.0
 **/
public class StringCusNoNetworkReader extends ReadHandler<NettyWriteBo> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyWriteBo msg) throws Exception {
        String ipPort = NetAddress.nettyRemoteAddress(ctx.channel()).ipPort();
        System.out.println(GsonUtils.toString(msg));
    }
}
