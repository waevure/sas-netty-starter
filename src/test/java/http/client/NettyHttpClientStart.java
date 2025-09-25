package http.client;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.log.LogMerge;
import com.sas.sasnettystarter.netty.mods.ab.NettyHttpClientAbility;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
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
public class NettyHttpClientStart {


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
    public void startNoNetworkChannel(ProjectAbstract pa) throws Exception {
        //创建链路信息
        NettyLink link = new NettyLink();
        link
                // 独立内存池
                .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                // 开启日志
                .logMerge(new LogMerge(LogLevel.INFO))
                // HTTP 编解码器
                .addBeforePipeline(channel ->
                        {
                            channel.pipeline().addLast(new HttpClientCodec())
                                    // 聚合 HTTP 消息（把 HttpMessage + HttpContent 聚合成 FullHttpResponse）
                                    .addLast(new HttpObjectAggregator(1024 * 1024));
                            return true;
                        }
                )
                // 添加指令分发器
                .addReadHandler(StringCusHttpClientReader.class)
                .addWriteHandler(StringCusHttpClientWriter.class);

        // 将projectInterface加入缓存
        NettyHttpClientGuide.putProject(pa);

        //使用引导类创建netty
        NettyHttpClientGuide.initStart(
                pa,
                NettyType.C_HTTP,
                new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                link
        );
    }

    public static void main(String[] args) {
        NettyHttpClientStart serverStart = new NettyHttpClientStart();
        try {
            NettyHttpClientProject pe = new NettyHttpClientProject("HTTP-客户端", "10001");
            serverStart.startNoNetworkChannel(pe);
            Thread.sleep(1000);
            // 获取客户端能力
            NettyHttpClientAbility ability = NettyHttpClientGuide.httpClient(pe);
            // 连接
            ability.connectSync(new IpPortAddress("127.0.0.1", 8877));
            Thread.sleep(5000);
            Map<String, String> tm = new HashMap<>();
            tm.put("key", "value");
            tm.put("key2", "value2");
            // 连接server
            NettyHttpClientGuide.httpClient(pe).sendGetRequest(
                    "/test/ww",
                    tm,
                    new IpPortAddress("127.0.0.1", 8877),
                    null
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
