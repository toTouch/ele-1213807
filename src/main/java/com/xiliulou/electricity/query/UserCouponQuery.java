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


    private List<Integer> statusList;

    private List<Integer> typeList;

    private Integer tenantId;

    private String userName;

    private Integer discountType;

    private Integer status;
}
