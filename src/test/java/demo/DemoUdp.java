package demo;

import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.log.LogMerge;
import com.sas.sasnettystarter.netty.ops.udp.NettyUdpOperations;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import udp.server.NettyUdpServerGuide;
import udp.server.NettyUdpServerProject;
import udp.server.StringCusUdpServerReader;
import udp.server.StringCusUdpServerWriter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DemoUdp {

    public static void main(String[] args) {
        try {
            ProjectAbstract pe = new NettyUdpServerProject("UDP-Demo", "D30001");
            PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);

            NettyLink link = new NettyLink()
                    .addServerPort(9977)
                    .addBootstrapOption(ChannelOption.SO_BROADCAST, false)
                    .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                    .logMerge(new LogMerge(LogLevel.INFO))
                    .addReadHandler(StringCusUdpServerReader.class)
                    .addWriteHandler(StringCusUdpServerWriter.class);

            NettyUdpServerGuide.putProject(pe);
            NettyUdpServerGuide.initStart(
                    pe,
                    NettyType.UDP,
                    new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                    link
            );

            NettyUdpOperations ops = NettyUdpServerGuide.udpServerOperations(pe);
            ops.distributeInstruct(new NettyWriteBo(pe));

            Thread.sleep(5_000);
            NettyUdpServerGuide.destroyServer(pe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
