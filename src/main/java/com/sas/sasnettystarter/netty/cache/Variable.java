package com.sas.sasnettystarter.netty.cache;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 对接平台缓存
 * @Author: wqy
 * @Date: 2019/9/18 14:31
 */
@Slf4j
public class Variable {

    /**
     * 通道缓存
     * key: ip:port
     */
    protected final HashMap<String, ChannelHandlerContext> MAP_CHANNEL = new HashMap<>();

    public ChannelHandlerContext getCtx(String key) {
        return this.MAP_CHANNEL.get(key);
    }

    public ChannelHandlerContext putCtx(String key, ChannelHandlerContext ctx) {
        return this.MAP_CHANNEL.put(key, ctx);
    }

    public ChannelHandlerContext removeCtx(String key) {
        return this.MAP_CHANNEL.remove(key);
    }

    public ChannelHandlerContext removeCtx(ChannelHandlerContext ctx) {
        for (String key : this.MAP_CHANNEL.keySet()) {
            if (this.MAP_CHANNEL.get(key) == ctx) {
                return this.MAP_CHANNEL.remove(key);
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
     * @return
     */
    public List<ChannelHandlerContext> channelActiveList() {
        return new ArrayList<ChannelHandlerContext>(this.MAP_CHANNEL.values());
    }

    /**
     * 销毁
     * @return
     */
    public boolean destroy(){
        this.MAP_CHANNEL.clear();
        return true;
    }

}
