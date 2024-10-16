package com.xiliulou.electricity.query;

import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
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
    
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    @NotBlank(message = "套餐名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    @NotNull(message = "押金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal deposit;
    
    @NotNull(message = "租金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal rentPrice;
    
    //@NotNull(message = "租金单价不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal rentPriceUnit;
    
    @NotNull(message = "租期不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer validDays;
    
    @NotNull(message = "租期单位不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer rentUnit;
    
    @NotNull(message = "加盟商不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long franchiseeId;
    
    //@NotNull(message = "租赁类型不能为空", groups = {CreateGroup.class, UpdateGroup.class})
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
    
    /**
     * 套餐绑定的所有优惠券id
     */
    private List<Long> couponIdsTransfer;
    
    /**
     * 套餐绑定的所有优惠券id
     */
    private String couponIds;
    
    @NotNull(message = "是否退租金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer isRefund;
    
    /**
     * 退租金天数限制
     */
    private Integer refundLimit;
    
    @NotNull(message = "是否免押不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer freeDeposite;
    
    //    @NotNull(message = "滞纳金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal serviceCharge;
    
    private String remark;
    
    private List<String> batteryModels;
    
    private Integer delFlag;
    
    private List<Long> franchiseeIds;
    
    private String batteryV;
    
    private Long uid;
    
    private List<Integer> rentTypes;
    
    /**
     * 套餐业务类型：0，换电套餐；1，车电一体套餐, 2. 企业渠道换电套餐；4，分期套餐
     *
     * @see BatteryMemberCardBusinessTypeEnum
     */
    private Integer businessType;
    
    private List<Integer> businessTypes;
    
    private List<Long> idList;
    
    /**
     * 电池型号
     */
    private String batteryModel;
    
    /**
     * 电池长型号
     */
    private String originalBatteryModel;
    
    /**
     * 用户分组id
     */
    private String userInfoGroupId;
    
    /**
     * 套餐绑定的所有用户分组id
     */
    private List<Long> userInfoGroupIdsTransfer;
    
    /**
     * 用户分组id，查询用户可用套餐时使用
     */
    private List<String> userInfoGroupIdsForSearch;
    
    /**
     * 分组类型，0-系统分组，1-用户分组。
     */
    private Integer groupType;
    
    /**
     * 是否需要查询企业套餐数据，0-否，1-是
     */
    private Integer catchEnterprise;
    
    /**
     * 分期套餐服务费
     */
    private BigDecimal installmentServiceFee;
    
    /**
     * 分期套餐首期费用
     */
    private BigDecimal downPayment;
}
