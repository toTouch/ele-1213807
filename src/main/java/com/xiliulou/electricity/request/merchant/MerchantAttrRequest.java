package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
    private Integer invitationValidTime;
    
    /**
     * 邀请时效单位
     */
    private Integer validTimeUnit;
    
    /**
     * 邀请保护期
     */
    private Integer invitationProtectionTime;
    
    /**
     * 邀请保护期单位
     */
    private Integer protectionTimeUnit;
    
}
