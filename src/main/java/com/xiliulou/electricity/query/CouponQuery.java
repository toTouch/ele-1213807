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

    /**
     * 加盟商Id
     */
    private Integer franchiseeId;

}
