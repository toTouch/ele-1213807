package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-04-10:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantAttrRequest {
    
    /**
     * 邀请时效
     */
    @Max(value = 99999, message = "邀请时效不合法")
    private Integer invitationValidTime;
    
    /**
     * 邀请时效单位
     */
    private Integer validTimeUnit;
    
    /**
     * 邀请保护期
     */
    @Max(value = 99999, message = "邀请保护期不合法")
    @Min(value = 0, message = "邀请保护期不合法")
    private Integer invitationProtectionTime;
    
    /**
     * 邀请保护期单位
     */
    private Integer protectionTimeUnit;
    
}
