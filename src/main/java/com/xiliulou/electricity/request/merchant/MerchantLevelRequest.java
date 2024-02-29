package com.xiliulou.electricity.request.merchant;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-04-15:54
 */
@Data
public class MerchantLevelRequest {
    @NotNull(message = "id不能为空")
    private Long id;
    
    /**
     * 商户等级名称
     */
    private String name;
    
    /**
     * 拉新人数
     */
    private Long invitationUserCount;
    
    /**
     * 续费人数
     */
    private Long renewalUserCount;
}
