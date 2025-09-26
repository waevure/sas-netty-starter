package com.sas.sasnettystarter.netty.ops.tcp;

import com.sas.sasnettystarter.netty.NettyGuideAbstract;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.cache.Variable;
import com.sas.sasnettystarter.netty.exception.NettyLinkException;
import com.sas.sasnettystarter.netty.handle.ChannelStatusManager;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import com.sas.sasnettystarter.netty.ops.core.NettyServerBaseContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * 启动成功后，该类负责TcpServer的维护
 * 对外操作能力由NettyTcpServerOperations负责，其余NettyTcpServer能力由本类提供
 * 该类分为两种用法
 * 1.开启默认状态管理器，就是在启动构建的时候这么构建
 *  //创建链路信息
 *         NettyLink link = new NettyLink();
 *
 *         // 设置服务端口号
 *         link.addServerPort(5577);
 *         // 设备启动成功回调
 *         // link.addStartSuccessCallback(function);
 *         link
 *                 // 添加bootstrap-option
 *                 .addBootstrapOption(ChannelOption.SO_BROADCAST, false)
 *                 // 独立内存池
 *                 .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
 *                 // 开启日志
 *                 .logMerge(new LogMerge(LogLevel.INFO))
 *                 // 添加字符串解码器
 *                 .addUnpack(new Unpacking(1024, ";"))
 *                 // 开启状态管理,添加指令分发器
 *                 //.openDefaultChannelStatus(new StringInstructionHandler())// 不启用状态管理器可以把下面的读放开
 *                 .openDefaultChannelStatus()// 不启用状态管理器可以把下面的读放开
 *                 // 读
 *                 .addReadHandler(StringCusReader.class)
 *                 // 写
 *                 .addWriteHandler(StringCusWriter.class);
 * 主要就是openDefaultChannelStatus(),是否开启，开启的时候是否指定指令分发器。指定的话不用手动释放引用计数对象。不开启的话如果下一个处理器不是Netty下含有自动释放的则需要手动释放。
 * 开启状态管理器的话由{@link ChannelStatusManager}负责链路和通道的状态管理，通道的状态维护在{@link Variable}的MAP_CHANNEL中，key为ip:port，
 * 后续使用的话可以通过{@link NettyGuideAbstract#support(ProjectAbstract)#getNettyServerContext()#getVariable()}进行操作通道，下发指令，关闭通道之类的。
 * 同时上线和下线的时候{@link ChannelStatusManager}会发布用户事件，用户可以在构建的时候添加上下线事件处理器
 * .addOnlineUserLogic(NettyClientOnlineHandler.class)
 * .addOfflineUserLogic(NettyClientOfflineHandler.class)
 * 不想添加默认会触发NettyClientOnlineHandler和NettyClientOfflineHandler。
 * 如果使用的是{@link NettyServerBaseContext#getKeyMap()}管理的channel，则需要自己定义上下线处理器，然后进行移除或添加。
 * 2.不开启默认状态管理器
 *  //创建链路信息
 *         NettyLink link = new NettyLink();
 *
 *         // 设置服务端口号
 *         link.addServerPort(5577);
 *         // 设备启动成功回调
 *         // link.addStartSuccessCallback(function);
 *         link
 *                 // 添加bootstrap-option
 *                 .addBootstrapOption(ChannelOption.SO_BROADCAST, false)
 *                 // 独立内存池
 *                 .addBootstrapOption(ChannelOption.ALLOCATOR, allocator)
 *                 // 开启日志
 *                 .logMerge(new LogMerge(LogLevel.INFO))
 *                 // 添加字符串解码器
 *                 .addUnpack(new Unpacking(1024, ";"))
 *                 // 读
 *                 .addReadHandler(StringCusReader.class)
 *                 // 写
 *                 .addWriteHandler(StringCusWriter.class);
 * 不开启状态管理器的话，可以在处理器或者逻辑处理器中进行添加，但是断开的话不是很好移除。所以建议仿照{@link ChannelStatusManager}写一个状态管理器,
 * 然后添加到拆包后面.addLogicHandler(ChannelStatusManager.class);管理状态的时候最好使用{@link NettyGuideAbstract#tcpServerOperations(ProjectAbstract)}获取tcpServer能力，
 * 然后{@link NettyTcpServer#putCtx(String, ChannelHandlerContext)}或{@link NettyTcpServer#getCtx(String)}进行添加或删除，这样销毁的时候会自动销毁校友连接。
 * 自己管理通道则需要自己销毁连接。
 * @ClassName: NettyTcpServer
 * @Description:
 * @Author: Wqy
 * @Date: 2025-09-24 15:21
 * @Version: 1.0
 **/
@Slf4j
public class NettyTcpServer extends NettyServerBaseContext implements NettyTcpServerOperations {
    /**
     * @param nettyType   类型
     * @param bootstrap   netty引导类
     * @param bossGroup   boss组
     * @param workerGroup worker组
     */
    public NettyTcpServer(ProjectAbstract pe, NettyType nettyType, ServerBootstrap bootstrap, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        super(pe, nettyType, bootstrap, bossGroup, workerGroup);
    }

    /**
     * 等待退出
     */
    @Override
    public void awaitCloseSync(Integer port) {
        try {
            log.info("{}-TCP-服务端-准备启动中:{}", this.getPe().toStr(), port);
            ChannelFuture f = this.getServerBootstrap().bind(port).sync();
            log.info("{}-TCP-服务端-启动完成:{}", this.getPe().toStr(), port);
            this.setChannelFuture(f);
            // 进行回调
            if (Objects.nonNull(this.getStartSuccessCallback())) {
                this.getStartSuccessCallback().apply(f.channel());
            }
            //使用f.channel().closeFuture().sync()方法进行阻塞,等待服务端链路关闭之后main函数才退出。
            f.channel().closeFuture().sync();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            /**优雅退出，释放NIO线程组*/
            this.getBossGroup().shutdownGracefully().syncUninterruptibly();
            this.getWorkerGroup().shutdownGracefully().syncUninterruptibly();
            // 打印状态
            this.printNettyServerBootstrapGroupStatus();
        }
    }

    /**
     * 添加ctx
     *
     * @param key
     * @param ctx
     */
    @Override
    public void putCtx(String key, ChannelHandlerContext ctx) {
        this.getKeyMap().put(key, ctx);
    }

    /**
     * 获取
     *
     * @param key
     * @return
     */
    @Override
    public ChannelHandlerContext getCtx(String key) {
        return this.getKeyMap().get(key);
    }

    /**
     * 下发指令
     *
     * @param key
     * @param writeData
     */
    @Override
    public void distributeInstruct(String key, NettyWriteBo writeData) {
        // 获取通道
        ChannelHandlerContext ctx = this.getCtx(key);
        if (Objects.isNull(ctx)) {
            throw new NettyLinkException(String.format("%s-TCP-服务端-链路未注册:%s", this.getPe().toStr(), key));
        }
        if (!ctx.channel().isActive()) {
            throw new NettyLinkException(String.format("%s-TCP-服务端-链路未激活:%s", this.getPe().toStr(), key));
        }
        ctx.channel().writeAndFlush(writeData);
    }

    /**
     * 全部注册通道
     *
     * @return
     */
    @Override
    public Map<String, ChannelHandlerContext> registerClientChannel() {
        return this.getKeyMap();
    }

    @Override
    public boolean destroyServer() {
        try {
            // 项目信息
            String peStr = this.getPe().toStr();
            // 打印日志：开始销毁
            log.info("{}-TCP-服务端-开始销毁", peStr);
            // 关闭客户端缓存
            for (String key : this.getKeyMap().keySet()) {
                ChannelHandlerContext ctx = this.getKeyMap().get(key);
                if (ctx.channel().isOpen()) {
                    ctx.close().syncUninterruptibly();
                    log.info("{}-{}-关闭", peStr, key);
                } else {
                    log.info("{}-{}-已关闭", peStr, key);
                }
            }
            // 关闭所有客户端连接
            this.getVariable().destroy(this.getPe());
            // 关闭 Netty 的 Channel（也就是服务端的监听端口，accept 新连接的入口）
            if (this.getChannelFuture() != null) {
                Channel channel = this.getChannelFuture().channel();
                if (channel != null && channel.isOpen()) {
                    channel.close().syncUninterruptibly();
                }
            }
            // 打印日志：销毁完成
            log.info("{}-TCP-服务端-销毁完成", peStr);
            return true;
        } catch (Exception ex) {
            log.error("{} - TCP 服务端销毁失败: {}", this.getPe().toStr(), ex.getMessage(), ex);
            return false;
        }
    }
}
