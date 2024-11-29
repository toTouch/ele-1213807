/**
 * Create date: 2024/7/30
 */

package com.xiliulou.electricity.ttl;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/30 13:48
 */
public class ChannelSourceContextHolder {
    
    private static final ThreadLocal<String> CHANNEL_SOURCE_LOCAL = new TransmittableThreadLocal<>();
    
    /**
     * TTL 设置渠道来源
     *
     * @param channel
     */
    public static void set(String channel) {
        CHANNEL_SOURCE_LOCAL.set(channel);
    }
    
    /**
     * 获取TTL渠道来源
     *
     * @return
     */
    public static String get() {
        return CHANNEL_SOURCE_LOCAL.get();
    }
    
    public static void clear() {
        CHANNEL_SOURCE_LOCAL.remove();
    }
    
}
