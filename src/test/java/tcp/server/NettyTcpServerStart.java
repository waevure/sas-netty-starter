package tcp.server;

import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.log.LogMerge;
import com.sas.sasnettystarter.netty.mods.operation.NettyTcpServerOperations;
import com.sas.sasnettystarter.netty.unpack.Unpacking;
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
public class NettyTcpServerStart {


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
    public void startTcpServer(ProjectAbstract pa) throws Exception {
        //创建链路信息
        NettyLink link = new NettyLink();

        // 设置服务端口号
        link.addServerPort(5577);
        // 设备启动成功回调
        // link.addStartSuccessCallback(function);
        link
                // 添加bootstrap-option
                .addBootstrapOption(ChannelOption.SO_BROADCAST, false)
                // 独立内存池
                .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                // 开启日志
                .logMerge(new LogMerge(LogLevel.INFO))
                // 添加字符串解码器
                .addUnpack(new Unpacking(1024, ";"))
                // 开启状态管理,添加指令分发器
                //.openDefaultChannelStatus(new StringInstructionHandler())// 不启用状态管理器可以把下面的读放开
                .openDefaultChannelStatus()// 不启用状态管理器可以把下面的读放开
                // 读
                .addReadHandler(StringCusReader.class)
                // 写
                .addWriteHandler(StringCusWriter.class);

        // 将projectInterface加入缓存
        NettyTcpServerGuide.putProject(pa);

        //使用引导类创建netty
        NettyTcpServerGuide.initStart(
                pa,
                NettyType.S_TCP,
                new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                link
        );

        // 核心线程数 2
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // 每隔 3 秒执行一次任务，延迟 1 秒启动
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println(Thread.currentThread().getName() + " 定时任务执行: " + System.currentTimeMillis());
            // 获取tcp服务端能力
            NettyTcpServerOperations ability = NettyTcpServerGuide.tcpServerOperations(pa);
            // 下发指令
            for (String key : ability.registerClientChannel().keySet()) {
                ability.distributeInstruct(key, new NettyWriteBo());
            }
        }, 1, 3, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        NettyTcpServerStart serverStart = new NettyTcpServerStart();
        try {
            NettyTcpServerProject pe = new NettyTcpServerProject("TCP服务端", "10001");
            serverStart.startTcpServer(pe);
            // 1分钟销毁
            Thread.sleep(1000 * 60);
            // 销毁项目
            NettyTcpServerGuide.destroyServer(pe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
