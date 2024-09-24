package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.enums.PackageTypeEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author BaoYu
 * @description:
 * @date 2023/12/1 11:07
 */
@Data
@Builder
public class FreeDepositUserDTO {
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 用户手机号
     */
    private String phoneNumber;
    
    /**
     * 用户身份证
     */
    private String idCard;
    
    /**
     * 用户真实姓名
     */
    private String realName;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 购买套餐类型
     *
     * @see PackageTypeEnum
     */
    private Integer packageType;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
}
