package com.xiliulou.electricity.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/13 14:52:33
 */
@UtilityClass
public class ThirdMallConfigHolder {
    
    private final ThreadLocal<Integer> THIRD_MALL_CONFIG_LOCAL = new TransmittableThreadLocal<>();
    
    /**
     * TTL 设置租户ID
     *
     * @param tenantId
     */
    public void setTenantId(Integer tenantId) {
        THIRD_MALL_CONFIG_LOCAL.set(tenantId);
    }
    
    /**
     * 获取TTL中的租户ID
     *
     * @return
     */
    public Integer getTenantId() {
        return THIRD_MALL_CONFIG_LOCAL.get();
    }
    
    /**
     * 清除当前线程中的租户 慎用
     */
    public void clear() {
        THIRD_MALL_CONFIG_LOCAL.remove();
    }
}
