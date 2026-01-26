package demo;

import com.sas.sasnettystarter.netty.NetAddress;
import com.sas.sasnettystarter.netty.NettyLink;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.log.LogMerge;
import com.sas.sasnettystarter.netty.ops.tcp.NettyTcpClientOperations;
import com.sas.sasnettystarter.netty.unpack.Unpacking;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import tcp.client.NettyTcpClientGuide;
import tcp.client.NettyTcpClientProject;
import tcp.client.StringCusClientReader;
import tcp.client.StringCusClientWriter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DemoTcpClient {

    public static void main(String[] args) {
        try {
            ProjectAbstract pe = new NettyTcpClientProject("TCP-Client-Demo", "D10002");
            PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);

            NettyLink link = new NettyLink()
                    .addBootstrapOption(ChannelOption.SO_BROADCAST, false)
                    .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
                    .logMerge(new LogMerge(LogLevel.INFO))
                    .addUnpack(new Unpacking(1024, ";"))
                    .addReadHandler(StringCusClientReader.class)
                    .addWriteHandler(StringCusClientWriter.class);

            NettyTcpClientGuide.putProject(pe);
            NettyTcpClientGuide.initStart(
                    pe,
                    NettyType.C_TCP,
                    new ThreadPoolExecutor(1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)),
                    link
            );

            NettyTcpClientOperations ops = NettyTcpClientGuide.tcpClientOperations(pe);
            ops.connectSync(new NetAddress("127.0.0.1", 6677));
            ops.distributeInstruct(new NetAddress("127.0.0.1", 6677).ipPort(), new NettyWriteBo(pe));

            Thread.sleep(5_000);
            NettyTcpClientGuide.destroyServer(pe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
