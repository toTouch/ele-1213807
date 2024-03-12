package com.xiliulou.electricity.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.xiliulou.electricity.entity.User;
import lombok.experimental.UtilityClass;

/**
 * @author : eclair
 * @date : 2024/2/18 17:18
 */
@UtilityClass
public class MerchantUserContextHolder {
    private final ThreadLocal<User> MERCHANT_USER_LOCAL = new TransmittableThreadLocal<>();

    /**
     * TTL 设置租户ID
     */
    public void setUser(User user) {
        MERCHANT_USER_LOCAL.set(user);
    }

    /**
     * 获取TTL中的租户ID
     *
     * @return
     */
    public User getTenantId() {
        return MERCHANT_USER_LOCAL.get();
    }

    public void clear() {
        MERCHANT_USER_LOCAL.remove();
    }
}
