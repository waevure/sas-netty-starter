package demo;

import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.log.LogMerge;
import com.sas.sasnettystarter.netty.unpack.Unpacking;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import tcp.server.NettyTcpServerGuide;
import tcp.server.NettyTcpServerProject;
import tcp.server.StringCusReader;
import tcp.server.StringCusWriter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DemoTcpServer {

    public static void main(String[] args) {
        try {
            ProjectAbstract pe = new NettyTcpServerProject("TCP-Server-Demo", "D10001");
            PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);

            NettyLink link = new NettyLink()
                    .addServerPort(6677)
                    .addBootstrapOption(ChannelOption.SO_BROADCAST, false)
                    .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                    .logMerge(new LogMerge(LogLevel.INFO))
                    .addUnpack(new Unpacking(1024, ";"))
                    .openDefaultChannelStatus()
                    .addReadHandler(StringCusReader.class)
                    .addWriteHandler(StringCusWriter.class);

            NettyTcpServerGuide.putProject(pe);
            NettyTcpServerGuide.initStart(
                    pe,
                    NettyType.S_TCP,
                    new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                    link
            );

            Thread.sleep(10_000);
            NettyTcpServerGuide.destroyServer(pe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
