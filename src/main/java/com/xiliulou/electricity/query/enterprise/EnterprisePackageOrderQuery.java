package com.xiliulou.electricity.query.enterprise;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/22 15:14
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterprisePackageOrderQuery {

    @NotNull(message = "套餐不能为空!", groups = {CreateGroup.class})
    private Long packageId;

    @NotNull(message = "企业信息不能为空!", groups = {CreateGroup.class})
    private Long enterpriseId;

    @NotNull(message = "购买用户不能为空!", groups = {CreateGroup.class})
    private Long uid;
    
    /**
     * 电池类型
     */
    private Integer model;

    private Integer tenantId;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 保险ID
     */
    private Integer insuranceId;
    

    //优惠券
    private Integer userCouponId;

    private String productKey;

    private String deviceName;

    private Long beginTime;

    private Long endTime;
    
    private Long size;
    
    private Long offset;
    
}
