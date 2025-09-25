package http.client;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.handle.ReadHandler;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.utils.GsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * @ClassName: StringCusReader
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 16:46
 * @Version: 1.0
 **/
public class StringCusHttpClientReader extends ReadHandler<FullHttpResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        System.out.println("响应状态: " + response.status());
        System.out.println("响应内容: " + response.content().toString(io.netty.util.CharsetUtil.UTF_8));
    }
}
