package com.sas.sasnettystarter.netty.mods;

import cn.hutool.core.util.ObjectUtil;
import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import com.sas.sasnettystarter.netty.cache.Variable;
import com.sas.sasnettystarter.netty.exception.NettyServiceException;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Function;

/**
 * netty启动组
 *
 * @author WQY
 * @version 1.0
 * @date 2023/12/12 09:49
 */
@Slf4j
@Getter
public abstract class NettyMods {

    /**
     * 项目信息
     */
    public ProjectAbstract pe;

    /**
     * netty类型
     */
    public NettyType nettyType;

    /**
     * 缓存信息值
     */
    public Variable variable;

    /**
     * 启动成功回调
     */
    public Function<Channel, Boolean> startSuccessCallback;

    public NettyMods() {
    }

    public NettyMods(ProjectAbstract pe, NettyType nettyType) {
        this.pe = pe;
        this.nettyType = nettyType;
        this.variable = new Variable();
    }

    /**
     * 等待关闭
     */
    public void awaitSync(){};

    /**
     * 等待关闭
     * @param port
     */
    public void awaitSync(Integer port) {}


    public Variable variable() {return this.variable;}

    /**
     * 传入WriteBo对象
     * 放入通道链路
     *
     * @param writeBo
     * @return
     */
    public <T extends NettyWriteBo> void writeAndFlush(T writeBo) {
        ChannelHandlerContext ctx = this.variable.getCtx(writeBo.ipPortStr());
        if (Objects.nonNull(ctx)) {
            ctx.channel().writeAndFlush(writeBo);
        } else {
            log.error("{}-发送数据失败,链路不存在-{}", writeBo.ipPortStr(), writeBo.getMsg());
            throw new NettyServiceException("发送数据失败,链路不存在");
        }
    }

    /**
     * 关闭连接
     *
     * @param ipPortAddress
     */
    public void closeConnect(IpPortAddress ipPortAddress) {
        ChannelHandlerContext ctx = this.variable.getCtx(ipPortAddress.ipPort());
        ctx.channel().close();
    }

    /**
     * 通道状态
     *
     * @param ipPortAddress
     * @return
     */
    public Boolean channelActive(IpPortAddress ipPortAddress) {
        ChannelHandlerContext context = this.variable.getCtx(ipPortAddress.ipPort());
        if (ObjectUtil.isNotNull(context)) {
            return context.channel().isActive();
        }
        return false;
    }


    /**
     * 销毁服务
     * @return
     */
    public abstract boolean destroyServer() throws Exception;

    /**
     * 打印netty工作组状态
     */
    public abstract void printNettyServerBootstrapGroupStatus();

    /**
     * 打印netty工作组状态
     */
    public abstract void printNettyBootstrapGroupStatus();
}
