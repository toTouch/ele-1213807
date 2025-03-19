package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: lxc
 * @Date: 2021/4/15 16:02
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponQuery {
    private Long size;
    private Long offset;
    /**
     * 优惠券id
     */
    private Integer couponId;
    /**
     * 用户id
     */
    private  Long uid;
    /**
     * 用户手机号
     */
    private  String phone;

    private  String orderId;

    private List<Integer> statusList;

    private List<Integer> typeList;

    private Integer tenantId;

    private String userName;

    private Integer discountType;

    private Integer status;

    private Long beginTime;

    private Long endTime;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;

    private Integer superposition;
    
    private Long franchiseeId;
    
    
    /**
     * 发券类型
     */
    private List<Integer> couponType;
    
    /**
     * 发券方式
     */
    private Long couponWay;
    
    /**
     * 优惠卷名称
     */
    private String couponName;
}
