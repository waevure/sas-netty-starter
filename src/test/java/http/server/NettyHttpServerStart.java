package http.server;

import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.log.LogMerge;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty主控板协议客户端启动
 *
 * @author WQY
 * @version 1.0
 * @date 2024/1/18 14:24
 */
@Slf4j
public class NettyHttpServerStart {


    private PooledByteBufAllocator allocator = new PooledByteBufAllocator(
            true, // 使用直接内存
            Runtime.getRuntime().availableProcessors() * 2,    // Heap Arena 数量
            Runtime.getRuntime().availableProcessors() * 2,    // Direct Arena 数量
            8192, // 页大小
            11,   // 最大阶数
            512,  // 微型缓存大小
            256,   // 小型缓存大小
            128 // 普通缓存大小
    );

    /**
     * 启动tcp服务
     *
     * @param pa 项目标识/服务标识
     */
    public void startHttpServerChannel(ProjectAbstract pa) throws Exception {
        //创建链路信息
        NettyLink link = new NettyLink();
        link.addServerPort(8877);
        link
                // 独立内存池
                .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                // 开启日志
                .logMerge(new LogMerge(LogLevel.INFO))
                // HTTP 编解码器
                .addBeforePipeline(channel ->
                        {
                            channel.pipeline()
                                    .addLast(new HttpServerCodec())
                                    // 聚合 HTTP 消息（把 HttpMessage + HttpContent 聚合成 FullHttpResponse）
                                    .addLast(new HttpObjectAggregator(1024 * 1024));
                            return true;
                        }
                )
                .openDefaultChannelStatus()
                // 添加指令分发器
                .addReadHandler(StringCusHttpServerReader.class)
                .addWriteHandler(StringCusHttpServerWriter.class);

        // 将projectInterface加入缓存
        NettyHttpServerGuide.putProject(pa);

        //使用引导类创建netty
        NettyHttpServerGuide.initStart(
                pa,
                NettyType.S_HTTP,
                new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                link
        );
    }

    public static void main(String[] args) {
        NettyHttpServerStart serverStart = new NettyHttpServerStart();
        try {
            NettyHttpServerProject pe = new NettyHttpServerProject("HTTP-服务端", "10001");
            serverStart.startHttpServerChannel(pe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
