

package com.xiliulou.electricity.ttl;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.xiliulou.electricity.constant.CommonConstant;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/4/19 09:16
 */
public class TtlTraceIdSupport {
    
    
    static TransmittableThreadLocal<String> TTL_MDC = new TransmittableThreadLocal<>() {
        
        /**
         * 在多线程数据传递的时候，将数据复制一份给MDC
         */
        @Override
        protected void beforeExecute() {
            final String traceId = get();
            MDC.put(CommonConstant.TRACE_ID, traceId);
        }
        
        @Override
        protected void afterExecute() {
            MDC.clear();
        }
        
    };
    
    /**
     * description: 生成设置 traceId
     *
     * @author caobotao.cbt
     * @date 2024/4/23 16:07
     */
    public static void set() {
        set(UUID.randomUUID().toString().replaceAll("-", ""));
    }
    
    /**
     * description: 设置 traceId
     *
     * @author caobotao.cbt
     * @date 2024/4/23 16:07
     */
    public static void set(String traceId) {
        TTL_MDC.set(traceId);
        MDC.put(CommonConstant.TRACE_ID, traceId);
    }
    
    /**
     * description: 获取traceId
     *
     * @author caobotao.cbt
     * @date 2024/4/23 16:07
     */
    public static String get() {
        return MDC.get(CommonConstant.TRACE_ID);
    }
    
    /**
     * description: 清理
     *
     * @author caobotao.cbt
     * @date 2024/4/23 16:08
     */
    public static void clear() {
        MDC.clear();
        TTL_MDC.remove();
    }
}
