package demo;

import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.log.LogMerge;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import http.server.NettyHttpServerGuide;
import http.server.NettyHttpServerProject;
import http.server.StringCusHttpServerReader;
import http.server.StringCusHttpServerWriter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DemoHttpServer {

    public static void main(String[] args) {
        try {
            ProjectAbstract pe = new NettyHttpServerProject("HTTP-Server-Demo", "D20001");
            PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);

            NettyLink link = new NettyLink()
                    .addServerPort(8877)
                    .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                    .logMerge(new LogMerge(LogLevel.INFO))
                    .addBeforePipeline(channel -> {
                        channel.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(1024 * 1024));
                        return true;
                    })
                    .openDefaultChannelStatus()
                    .addReadHandler(StringCusHttpServerReader.class)
                    .addWriteHandler(StringCusHttpServerWriter.class);

            NettyHttpServerGuide.putProject(pe);
            NettyHttpServerGuide.initStart(
                    pe,
                    NettyType.S_HTTP,
                    new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                    link
            );

            Thread.sleep(10_000);
            NettyHttpServerGuide.destroyServer(pe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
