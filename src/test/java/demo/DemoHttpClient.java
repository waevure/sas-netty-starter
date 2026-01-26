package demo;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.ops.http.NettyHttpClientOperations;
import com.sas.sasnettystarter.netty.log.LogMerge;
import http.client.NettyHttpClientGuide;
import http.client.NettyHttpClientProject;
import http.client.StringCusHttpClientReader;
import http.client.StringCusHttpClientWriter;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;

import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DemoHttpClient {

    public static void main(String[] args) {
        try {
            ProjectAbstract pe = new NettyHttpClientProject("HTTP-Client-Demo", "D20002");
            PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);

            NettyLink link = new NettyLink()
                    .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                    .logMerge(new LogMerge(LogLevel.INFO))
                    .addBeforePipeline(channel -> {
                        channel.pipeline()
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(1024 * 1024));
                        return true;
                    })
                    .addReadHandler(StringCusHttpClientReader.class)
                    .addWriteHandler(StringCusHttpClientWriter.class);

            NettyHttpClientGuide.putProject(pe);
            NettyHttpClientGuide.initStart(
                    pe,
                    NettyType.C_HTTP,
                    new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                    link
            );

            NettyHttpClientOperations ops = NettyHttpClientGuide.httpClientOperations(pe);
            NetAddress address = new NetAddress("127.0.0.1", 8877);
            ops.connectSync(address);
            ops.sendGetRequest("/demo", Collections.singletonMap("key", "value"), address, null);

            Thread.sleep(5_000);
            NettyHttpClientGuide.destroyServer(pe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
