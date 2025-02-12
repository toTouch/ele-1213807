package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

/**
 * @author : renhang
 * @description CouponPackageEditQuery
 * @date : 2025-01-16 15:29
 **/
@Data
public class CouponPackageEditQuery {

    private Long id;

    @NotNull(message = "优惠券包名称不能为空")
    private String name;

    @NotNull(message = "加盟商不能为空")
    private Long franchiseeId;

    /**
     * 优惠券包优惠券
     */
    @Valid
    @NotEmpty(message = "优惠券不能为空")
    @Size(min = 1, max = 20, message = "优惠券数量不合法")
    private List<CouponPackageItemQuery> itemList;

    /**
     * 是否可购买，0否1是
     */
    private Integer isCanBuy;

    /**
     * 购买金额
     */
    private Double amount;


    @Data
    public static class CouponPackageItemQuery {

        private Long couponId;

        @Min(value = 1, message = "发放数量最小为1")
        @Max(value = 20, message = "发放数量最小为20")
        private Integer count;
    }
}
