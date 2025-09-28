package udp.client;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.log.LogMerge;
import com.sas.sasnettystarter.netty.ops.udp.NettyUdpOperations;
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
public class NettyUdpClientStart {


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
    public void startUdpClient(ProjectAbstract pa) throws Exception {
        //创建链路信息
        NettyLink link = new NettyLink();
        link
                // 添加bootstrap-option
                .addBootstrapOption(ChannelOption.SO_BROADCAST, false)
                // 独立内存池
                .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                // 开启日志
                .logMerge(new LogMerge(LogLevel.INFO))
                // 添加指令分发器
                .addReadHandler(StringCusUdpClientReader.class)
                .addWriteHandler(StringCusUdpClientWriter.class);

        // 将projectInterface加入缓存
        NettyUdpClientGuide.putProject(pa);

        //使用引导类创建netty
        NettyUdpClientGuide.initStart(
                pa,
                NettyType.UDP,
                new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                link
        );

        // 核心线程数 2
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // 每隔 3 秒执行一次任务，延迟 1 秒启动
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + " 定时任务执行: " + System.currentTimeMillis());
                NettyUdpOperations ability = NettyUdpClientGuide.udpServerOperations(pa);
                // 下发指令
                ability.distributeInstruct(new NettyWriteBo(pa, new NetAddress("127.0.0.1", 6677)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 3, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        NettyUdpClientStart serverStart = new NettyUdpClientStart();
        try {
            NettyUdpClientProject pe = new NettyUdpClientProject("UDP客户端", "10001");
            serverStart.startUdpClient(pe);
            Thread.sleep(3000);
            NettyUdpClientGuide.udpServerOperations(pe).distributeInstruct(new NettyWriteBo(pe, new NetAddress("127.0.0.1", 6677)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
