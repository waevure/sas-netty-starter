package com.sas.sasnettystarter.netty;

import com.sas.sasnettystarter.netty.mods.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 * Netty引导类
 *
 * @author WQY
 * @version 1.0
 * @date 2023/12/3 10:21
 */
@Slf4j
public abstract class NettyGuideAbstract {

    protected static final Map<ProjectAbstract, NettySupport> NETTY_GUIDE = new HashMap<>();

    /**
     * 根据protocolEnum(协议创建客户端)
     *
     * @param projectAbstract
     * @param executor
     */
    public static void startNettyClient(ProjectAbstract projectAbstract, ThreadPoolExecutor executor, NettyLink nettyLink) throws Exception {
        //启动
        //创建Netty支持
        NettySupport supportBoard = new NettySupport();
        //缓存引导数据，协议-NettySupport
        NettyGuideAbstract.NETTY_GUIDE.put(projectAbstract, supportBoard);
        //初始化链接
        initStart(projectAbstract, NettyType.C, executor, nettyLink);
    }

    /**
     * 根据protocolEnum(协议创建客户端)
     *
     * @param projectAbstract
     * @param executor
     */
    public static void startNettyHttpClient(ProjectAbstract projectAbstract, ThreadPoolExecutor executor, NettyLink nettyLink) throws Exception {
        //启动
        //创建Netty支持
        NettySupport supportBoard = new NettySupport();
        //缓存引导数据，协议-NettySupport
        NettyGuideAbstract.NETTY_GUIDE.put(projectAbstract, supportBoard);
        //初始化链接
        initStart(projectAbstract, NettyType.C_HTTP, executor, nettyLink);
    }

    /**
     * 根据protocolEnum(协议创建服务端)
     *
     * @param projectAbstract
     * @param executor
     */
    public static void startNettyServer(ProjectAbstract projectAbstract, ThreadPoolExecutor executor, NettyLink nettyLink) throws Exception {
        //启动
        //创建Netty支持
        NettySupport supportBoard = new NettySupport();
        //缓存引导数据，协议-NettySupport
        NettyGuideAbstract.NETTY_GUIDE.put(projectAbstract, supportBoard);
        //初始化链接
        initStart(projectAbstract, NettyType.S, executor, nettyLink);
    }

    /**
     * 根据projectAbstract获取无网络的channel
     *
     * @param projectAbstract
     */
    public static NettyMods newNoNetworkChannel(ProjectAbstract projectAbstract, NettyLink nettyLink) throws Exception {
        //启动
        //创建Netty支持
        NettySupport supportBoard = new NettySupport();
        //缓存引导数据，协议-NettySupport
        NettyGuideAbstract.NETTY_GUIDE.put(projectAbstract, supportBoard);
        //初始化链接
        return initStart(projectAbstract, NettyType.NO_NETWORK_CHANNEL, nettyLink);
    }

    /**
     * 获取support
     *
     * @param ptEnum
     * @return
     */
    public static NettySupport support(ProjectAbstract ptEnum) {
        return NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
    }

    /**
     * 获取客户端模块
     *
     * @param ptEnum 类型
     */
    public static NettyClientMods client(ProjectAbstract ptEnum) {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return null;
        }
        NettyMods mods = support.nettyMods();
        // 客户端
        if (NettyType.C == mods.nettyType) {
            return (NettyClientMods) mods;
        }

        log.error("--------{}该模块不为tcp客户端-------", ptEnum);

        return null;

    }

    /**
     * 获取客户端模块
     *
     * @param ptEnum 类型
     */
    public static NettyHttpClientMods httpClient(ProjectAbstract ptEnum) {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return null;
        }
        NettyMods mods = support.nettyMods();
        // 客户端
        if (NettyType.C == mods.nettyType) {
            return (NettyHttpClientMods) mods;
        }

        log.error("--------{}该模块不为http客户端-------", ptEnum);

        return null;

    }

    /**
     * 获取服务端模块
     *
     * @param ptEnum 类型
     */
    public static NettyServerMods server(ProjectAbstract ptEnum) {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return null;
        }
        NettyMods mods = support.nettyMods();
        // 客户端
        if (NettyType.S == mods.nettyType) {
            return (NettyServerMods) mods;
        }

        log.error("--------{}该模块不为tcp服务端-------", ptEnum);

        return null;

    }

    /**
     * 获取无网络的channel
     *
     * @param ptEnum 类型
     */
    public static NettyNoNetworkChannelMods noNetworkChannel(ProjectAbstract ptEnum) {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return null;
        }
        NettyMods mods = support.nettyMods();
        // 客户端
        if (NettyType.NO_NETWORK_CHANNEL == mods.nettyType) {
            return (NettyNoNetworkChannelMods) mods;
        }

        log.error("--------{}该模块不为无网络的channel-------", ptEnum);

        return null;
    }

    /**
     * 销毁Netty服务模块
     *
     * @param ptEnum 类型
     */
    public static boolean destroyServer(ProjectAbstract ptEnum) throws InterruptedException {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return false;
        }
        NettyMods mods = support.nettyMods();
        if (Objects.nonNull(mods)) {
            mods.destroyServer();
            // 移除map中对象
            NettyGuideAbstract.NETTY_GUIDE.remove(ptEnum);
            return true;
        }
        return false;
    }

    /**
     * 初始化启动
     */
    private static void initStart(ProjectAbstract pe, NettyType nettyType, ThreadPoolExecutor executor, NettyLink nettyLink) throws Exception {
        // 获取mods
        NettyMods finalMods = NettyGuideAbstract.buildMods(pe, nettyType, nettyLink);
        //获取NettySupport
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(pe);
        //异步等待
        executor.execute(() -> {
            //等待关闭
            support.initStartMods(finalMods);
            if (Objects.nonNull(nettyLink.getServerPort()) && nettyLink.getServerPort() > 0) {
                // 等待关闭
                support.awaitSync(nettyLink.getServerPort());
            } else {
                // 等待关闭
                support.awaitSync();
            }
        });
    }

    /**
     * 构建mods
     *
     * @param pe
     * @param nettyLink
     * @return
     */
    private static NettyMods buildMods(ProjectAbstract pe, NettyType nettyType, NettyLink nettyLink) throws Exception {
        //获取NettySupport
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(pe);
        //获取构建器
        NettySupport.Builder builder = support.builder();
        // 为空则添加默认的
        if (nettyLink.getBootstrapOptions().isEmpty()) {
            //添加option
            builder.addOption(ChannelOption.TCP_NODELAY, true);
            builder.addOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 5);
        } else {
            for (ChannelOption option : nettyLink.getBootstrapOptions().keySet()) {
                builder.addOption(option, nettyLink.getBootstrapOptions().get(option));
            }
        }
        //添加日志
        builder.addLogMerge(nettyLink.getLogMerge());
        //添加处理器
        builder.addStickyPackageUnpacking(nettyLink.getDecoder());

        //对默认处理器进行配置
        if (nettyLink.getIsOpenDefault()) {
            log.info("func配置:{}", nettyLink.getDefaultFunctionRead().getClass().getPackage().toString());
            builder.openDefaultChannelStatus(nettyLink.getDefaultFunctionRead());
        }

        // 添加Pipeline前的处理器
        if (Objects.nonNull(nettyLink.getBeforePipelines()) && !nettyLink.getBeforePipelines().isEmpty()) {
            for (Function<Channel, Boolean> function : nettyLink.getBeforePipelines()) {
                builder.addBeforePipeline(function);
            }
        }
        //添加指令分发器，状态处理器
        nettyLink.getLogicHandlers().stream().forEach(logicHandler -> {
            builder.addPipeline(logicHandler);
        });
        //添加读指令
        nettyLink.getReadHandlers().stream().forEach(readHandler -> {
            builder.addPipeline(readHandler);
        });
        //添加写指令
        nettyLink.getWriteHandlers().stream().forEach(writeHandler -> {
            builder.addPipeline(writeHandler);
        });
        // 添加启动成功回调
        if (Objects.nonNull(nettyLink.getStartSuccessCallback())) {
            // 添加成功回调
            builder.addStartSuccessCallback(nettyLink.getStartSuccessCallback());
        }

        //启动netty
        NettyMods mods;
        if (NettyType.C == nettyType) {
            mods = builder.startClient(pe);
        } else if (NettyType.C_HTTP == nettyType) {
            mods = builder.startHttpClient(pe);
        } else if (NettyType.NO_NETWORK_CHANNEL == nettyType) {
            mods = builder.buildNoNetworkChannel(pe);
        } else {
            mods = builder.startServer(pe);
        }

        return mods;

    }


    /**
     * 初始化启动
     */
    private static NettyMods initStart(ProjectAbstract pe, NettyType nettyType, NettyLink nettyLink) throws Exception {

        //构建mods
        NettyMods mods = NettyGuideAbstract.buildMods(pe, nettyType, nettyLink);
        //获取NettySupport
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(pe);
        //mods赋值
        support.initStartMods(mods);

        return mods;
    }

}
