package com.sas.sasnettystarter.netty.mods;

import cn.hutool.core.util.ObjectUtil;
import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.NettyType;
import com.sas.sasnettystarter.netty.cache.Variable;
import com.sas.sasnettystarter.netty.exception.NettyServiceException;
import com.sas.sasnettystarter.netty.handle.bo.NettyWriteBo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
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
public abstract class NettyMods {

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

    public void awaitSync(){};

    public void awaitSync(Integer port) {}

    public NettyType getNettyType() {
        return nettyType;
    }

    public void setNettyType(NettyType nettyType) {
        this.nettyType = nettyType;
    }

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
    public abstract boolean destroyServer() throws InterruptedException;
}
