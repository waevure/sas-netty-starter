package com.sas.sasnettystarter.netty.cache;

import com.sas.sasnettystarter.netty.IpPortAddress;
import com.sas.sasnettystarter.netty.PeBo;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Author: wqy
 * @Date: 2019/9/18 14:31
 */
@Slf4j
public class VariableChannelCache extends PeBo {

    public VariableChannelCache(ProjectAbstract pe) {
        super(pe);
    }

    /**
     * 通道缓存
     * key: ip:port
     */
    private final Map<String, ChannelHandlerContext> MAP_CHANNEL = new ConcurrentHashMap<>();

    public ChannelHandlerContext getCtx(String key) {
        return this.MAP_CHANNEL.get(key);
    }

    public ChannelHandlerContext putCtx(String key, ChannelHandlerContext ctx) {
        return this.MAP_CHANNEL.compute(key, (k, oldCtx) -> {
            if (oldCtx != null && oldCtx.channel().isActive()) {
                log.warn("{}-{}-已存在，进行关闭并添加新连接", this.getPe().toStr(), k);
                oldCtx.close(); // 关闭旧连接
            }
            return ctx; // 放入新连接
        });
    }

    public ChannelHandlerContext removeCtx(String key) {
        return this.MAP_CHANNEL.remove(key);
    }

    public ChannelHandlerContext removeCtx(ChannelHandlerContext ctx) {
        for (Iterator<Map.Entry<String, ChannelHandlerContext>> it = MAP_CHANNEL.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ChannelHandlerContext> entry = it.next();
            if (entry.getValue() == ctx) {
                it.remove(); // 直接通过迭代器删除，效率高
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 通道状态map
     * key:ip:port
     * value:是否活跃
     *
     * @return
     */
    public Map<String, Boolean> channelActiveMap() {
        Map<String, Boolean> map = new HashMap<>();
        for (String key : this.MAP_CHANNEL.keySet()) {
            map.put(key, this.MAP_CHANNEL.get(key).channel().isActive());
        }
        return map;
    }

    /**
     * 获取通道列表
     *
     * @return
     */
    public List<ChannelHandlerContext> channelActiveList() {
        return new ArrayList<>(this.MAP_CHANNEL.values());
    }

    /**
     * 销毁
     *
     * @return
     */
    public boolean destroy(ProjectAbstract pe) {
        // 关闭通道
        for (ChannelHandlerContext ctx : this.MAP_CHANNEL.values()) {
            if (ctx.channel().isOpen()) {
                ctx.close().syncUninterruptibly();
                log.info("{}[{}]销毁-通道关闭", pe.toStr(), IpPortAddress.nettyRemoteAddress(ctx.channel()).ipPort());
            }
        }
        // 清理map
        this.MAP_CHANNEL.clear();
        return true;
    }

}
