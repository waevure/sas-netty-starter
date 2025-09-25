package com.sas.sasnettystarter.netty;

import com.sas.sasnettystarter.netty.ops.core.NettyProjectContext;
import com.sas.sasnettystarter.netty.ops.embedded.NettyNoNetworkOperations;
import com.sas.sasnettystarter.netty.ops.http.NettyHttpClientOperations;
import com.sas.sasnettystarter.netty.ops.tcp.NettyTcpClientOperations;
import com.sas.sasnettystarter.netty.ops.tcp.NettyTcpServerOperations;
import com.sas.sasnettystarter.netty.ops.udp.NettyUdpOperations;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

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
     * 初始化启动
     *
     * @param pe        项目类型
     * @param nettyType netty类型
     * @param executor  线程池信息
     * @param nettyLink netty链路信息
     * @throws Exception
     */
    public static void initStart(ProjectAbstract pe, NettyType nettyType, ThreadPoolExecutor executor, NettyLink nettyLink) throws Exception {
        // 构建support
        NettySupport support = new NettySupport(pe, nettyType, executor, nettyLink).buildMods();
        // 添加本地缓存
        NettyGuideAbstract.NETTY_GUIDE.put(pe, support);
        // 提交任务
        support.getExecutor().execute(new NettyRunnable(support));
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
    public static NettyTcpClientOperations tcpClientOperations(ProjectAbstract ptEnum) {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return null;
        }
        NettyProjectContext mods = support.getMods();
        // 客户端
        if (NettyType.C_TCP == mods.nettyType) {
            return (NettyTcpClientOperations) mods;
        }

        log.error("--------{}该模块不为tcp客户端-------", ptEnum);

        return null;

    }

    /**
     * 获取客户端模块
     *
     * @param ptEnum 类型
     */
    public static NettyHttpClientOperations httpClientOperations(ProjectAbstract ptEnum) {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return null;
        }
        NettyProjectContext mods = support.getMods();
        // 客户端
        if (NettyType.C_HTTP == mods.nettyType) {
            return (NettyHttpClientOperations) mods;
        }

        log.error("--------{}该模块不为http客户端-------", ptEnum);

        return null;

    }

    /**
     * 获取服务端模块
     *
     * @param ptEnum 类型
     */
    public static NettyTcpServerOperations tcpServerOperations(ProjectAbstract ptEnum) {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return null;
        }
        NettyProjectContext mods = support.getMods();
        // 服务端
        if (NettyType.S_TCP == mods.nettyType) {
            return (NettyTcpServerOperations) mods;
        }

        log.error("--------{}该模块不为tcp服务端-------", ptEnum);

        return null;

    }


    /**
     * 获取udp服务
     *
     * @param ptEnum 类型
     */
    public static NettyUdpOperations udpServerOperations(ProjectAbstract ptEnum) {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return null;
        }
        NettyProjectContext mods = support.getMods();
        // 服务端
        if (NettyType.UDP == mods.nettyType) {
            return (NettyUdpOperations) mods;
        }

        log.error("--------{}该模块不为udp服务端-------", ptEnum);

        return null;

    }

    /**
     * 获取无网络的channel
     *
     * @param ptEnum 类型
     */
    public static NettyNoNetworkOperations noNetworkChannelOperations(ProjectAbstract ptEnum) {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return null;
        }
        NettyProjectContext mods = support.getMods();
        // 客户端
        if (NettyType.NO_NETWORK_CHANNEL == mods.nettyType) {
            return (NettyNoNetworkOperations) mods;
        }

        log.error("--------{}该模块不为无网络的channel-------", ptEnum);

        return null;
    }

    /**
     * 销毁Netty服务模块
     *
     * @param ptEnum 类型
     */
    public static boolean destroyServer(ProjectAbstract ptEnum) throws Exception {
        //获取netty_support
        NettySupport support = NettyGuideAbstract.NETTY_GUIDE.get(ptEnum);
        if (Objects.isNull(support)) {
            return false;
        }
        support.destroy();
        // 移除map中对象
        NettyGuideAbstract.NETTY_GUIDE.remove(ptEnum);
        return true;
    }

}
