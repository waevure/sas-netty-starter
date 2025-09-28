package http.server;

import com.sas.sasnettystarter.netty.handle.ReadHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

/**
 * @ClassName: StringCusReader
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 16:46
 * @Version: 1.0
 **/
public class StringCusHttpServerReader extends ReadHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // 1. 打印请求方法
        HttpMethod method = req.method();
        System.out.println("请求方法: " + method);

        // 2. 打印请求 URI（包含路径和 ?param=xxx）
        String uri = req.uri();
        System.out.println("请求 URI: " + uri);

        // 3. 如果需要获取 GET 参数，可以解析 URI
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        queryStringDecoder.parameters().forEach((key, value) -> {
            System.out.println("GET 参数: " + key + " = " + value);
        });

        // 4. 打印请求头
        HttpHeaders headers = req.headers();
        headers.forEach(entry -> {
            System.out.println("请求头: " + entry.getKey() + " = " + entry.getValue());
        });

        // 5. 打印 POST 请求体（如果有）
        if (method.equals(HttpMethod.POST)) {
            String body = req.content().toString(io.netty.util.CharsetUtil.UTF_8);
            System.out.println("POST Body: " + body);
        }
        // 构建响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                req.protocolVersion(),
                HttpResponseStatus.OK,
                ctx.alloc().buffer().writeBytes("Hello Netty HTTP Server!".getBytes())
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        // 写回客户端
        ctx.writeAndFlush(response);
    }
}
