package com.xiliulou.electricity.service.user.biz;

import java.math.BigDecimal;

/**
 * 用户业务聚合 Service
 *
 * @author xiaohui.song
 **/
public interface UserBizService {

    /**
     * 获取名下的总滞纳金（单电、单车、车电一体）
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @return 总金额
     */
    BigDecimal querySlippageTotal(Integer tenantId, Long uid);

    /**
     * 退押解绑用户信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param type 操作类型：0-退电、1-退车、2-退车电
     * @return true(成功)、false(失败)
     */
    boolean depositRefundUnbind(Integer tenantId, Long uid, Integer type);

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

    /**
     * 处理用户参与邀请返券活动
     * @param joinUid 参与用户ID
     * @param packageId 套餐ID
     */
    void joinShareActivityProcess(Long joinUid, Long packageId);

    /**
     * 处理用户参与邀请返现活动
     * @param joinUid 参与用户ID
     * @param packageId 套餐ID
     */
    void joinShareMoneyActivityProcess(Long joinUid, Long packageId, Integer tenantId);

    void joinChannelActivityProcess(Long joinUid);

}
