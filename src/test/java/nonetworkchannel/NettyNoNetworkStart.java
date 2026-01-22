package nonetworkchannel;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyReadBo;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.log.LogMerge;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * netty主控板协议客户端启动
 *
 * @author WQY
 * @version 1.0
 * @date 2024/1/18 14:24
 */
@Slf4j
public class NettyNoNetworkStart {


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
                .openDefaultChannelStatus()
                // 添加指令分发器
                .addReadHandler(StringCusNoNetworkReader.class)
                .addWriteHandler(StringCusNoNetworkWriter.class);

        // 将projectInterface加入缓存
        NettyNoNetworkGuide.putProject(pa);

        //使用引导类创建netty
        NettyNoNetworkGuide.initStart(
                pa,
                NettyType.NO_NETWORK_CHANNEL,
                new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                link
        );
    }

    public static void main(String[] args) {
        NettyNoNetworkStart serverStart = new NettyNoNetworkStart();
        try {
            NettyNoNetworkProject pe = new NettyNoNetworkProject("NO_NETWORK客户端", "10001");
            serverStart.startNoNetworkChannel(pe);
            Thread.sleep(1000);
            // 连接server
            NettyNoNetworkGuide.noNetworkChannelOperations(pe).distributeOutInstruct(new NettyWriteBo(pe, new NetAddress("127.0.0.1", 6677), "wwwwhwh哈哈哈哈哈"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
