package com.xiliulou.electricity.service.user.biz;

/**
 * 用户业务聚合 Service
 *
 * @author xiaohui.song
 **/
public interface UserBizService {

    /**
     * 是否是老用户<br />
     * 判定规则：用户是否购买成功过租车套餐 or 换电套餐
     * <pre>
     *     true-老用户
     *     false-新用户
     * </pre>
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    Boolean isOldUser(Integer tenantId, Long uid);
}
