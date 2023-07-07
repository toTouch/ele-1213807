package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-07-14:43
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryMemberCardQuery {
    private Long size;

    private Long offset;

    private Integer tenantId;

    @NotBlank(message = "套餐名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Range(min = 0, max = 8, message = "套餐名称不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    @NotNull(message = "押金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Range(min = 0, max = 8, message = "押金不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private Double deposit;

    @NotNull(message = "租金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Double rentPrice;

    @NotNull(message = "租金单价不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Double rentPriceUnit;

    @NotNull(message = "租期不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer validDays;

    @NotNull(message = "租期单位不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer rentUnit;

    @NotNull(message = "加盟商不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer franchiseeId;

    @NotNull(message = "租赁类型不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer rentType;

    @NotNull(message = "是否赠送优惠券不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer sendCoupon;

    @NotNull(message = "上架状态不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer status;

    @NotNull(message = "套餐限制不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer limitCount;
    /**
     * 使用次数
     */
    private Long useCount;
    /**
     * 优惠券id
     */
    private Integer couponId;

    @NotNull(message = "是否退租金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Range(min = 0, max = 8, message = "是否退租金不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer isRefund;
    /**
     * 退租金天数限制
     */
    private Integer refundLimit;

    @NotNull(message = "是否免押不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer freeDeposite;

    @NotNull(message = "退押审核不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer refundDepositeAudit;

    @NotNull(message = "滞纳金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Double serviceCharge;

    @NotNull(message = "套餐显隐不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer display;

    /**
     * 备注
     */
    private String remark;

}
