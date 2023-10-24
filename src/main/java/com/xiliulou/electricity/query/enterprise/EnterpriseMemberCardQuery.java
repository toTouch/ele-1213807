package com.xiliulou.electricity.query.enterprise;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author BaoYu
 * @date 2023-09-14 11:30
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseMemberCardQuery {

    private Long size;

    private Long offset;

    private Integer tenantId;

    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;

    @NotBlank(message = "套餐名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    @NotNull(message = "押金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal deposit;

    @NotNull(message = "租金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal rentPrice;

    //    @NotNull(message = "租金单价不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal rentPriceUnit;

    @NotNull(message = "租期不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer  validDays;

    /**
     * 租期单位默认为天
     */
    @NotNull(message = "租期单位不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer rentUnit;

    @NotNull(message = "加盟商不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long franchiseeId;

    /**
     * 电池型号
     */
    private List<String> batteryModels;

    @NotNull(message = "租赁类型不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer rentType;

    /**
     * 默认不赠送
     */
    //@NotNull(message = "是否赠送优惠券不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer sendCoupon;

    /**
     * 默认为上架状态
     */
    //@NotNull(message = "上架状态不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer status;

    //@NotNull(message = "套餐限制不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer limitCount;
    /**
     * 使用次数
     */
    private Long useCount;
    /**
     * 优惠券id
     */
    private Integer couponId;

    /**
     * 默认不可退
     */
    //@NotNull(message = "是否退租金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer isRefund;
    /**
     * 退租金天数限制
     */
    private Integer refundLimit;

    private String remark;

    @NotNull(message = "是否免押不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer freeDeposite;

    //    @NotNull(message = "滞纳金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal serviceCharge;

    private Integer delFlag;

    private List<Long> franchiseeIds;

    private Long uid;

    private String batteryV;

    private List<Integer> rentTypes;

    private List<Long> packageIds;

    private Long enterpriseId;
    
    private String orderNo;

}
