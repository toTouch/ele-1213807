package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.List;

/**
 * 加盟商活动绑定表(TFranchiseeBindCoupon)实体类
 *
 * @author makejava
 * @since 2021-04-17 13:33:05
 */
@Data
public class ShareActivityRuleQuery {
    /**
     * 触发人数
     */
    private Integer triggerCount;
    /**
     * 优惠券id
     */
    private Integer couponId;


}
