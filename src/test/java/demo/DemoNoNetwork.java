package demo;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.log.LogMerge;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import nonetworkchannel.NettyNoNetworkGuide;
import nonetworkchannel.NettyNoNetworkProject;
import nonetworkchannel.StringCusNoNetworkReader;
import nonetworkchannel.StringCusNoNetworkWriter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DemoNoNetwork {

    public static void main(String[] args) {
        try {
            ProjectAbstract pe = new NettyNoNetworkProject("NoNetwork-Demo", "D40001");
            PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);

            NettyLink link = new NettyLink()
                    .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                    .logMerge(new LogMerge(LogLevel.INFO))
                    .openDefaultChannelStatus()
                    .addReadHandler(StringCusNoNetworkReader.class)
                    .addWriteHandler(StringCusNoNetworkWriter.class);

            NettyNoNetworkGuide.putProject(pe);
            NettyNoNetworkGuide.initStart(
                    pe,
                    NettyType.NO_NETWORK_CHANNEL,
                    new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                    link
            );

            NettyNoNetworkGuide.noNetworkChannelOperations(pe)
                    .distributeOutInstruct(new NettyWriteBo(pe, new NetAddress("127.0.0.1", 6677), "demo"));

            Thread.sleep(2_000);
            NettyNoNetworkGuide.destroyServer(pe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
