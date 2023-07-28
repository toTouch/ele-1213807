package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: lxc
 * @Date: 2021/4/15 16:02
 * @Description:
 */
@Data
@Builder
public class CouponQuery {
    private Long size;
    private Long offset;
    /**
     * 优惠券名称
     */
    private String name;
    /**
     * 优惠类型，1--减免券，2--打折券，3-体验劵
     */
    private Integer discountType;

    private Integer status;
    /**
     * 加盟商Id
     */
    private Long franchiseeId;
    
    private List<Long> franchiseeIds;
    /**
     * 适用类型  1--邀请活动优惠券  2--普通活动优惠券
     */
    private Integer applyType;

    private Integer tenantId;

    /**
     * 优惠券ID集
     */
    private List<Long> ids;

    private Integer superposition;

}
