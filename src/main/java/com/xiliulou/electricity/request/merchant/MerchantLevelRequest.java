package com.xiliulou.electricity.request.merchant;

import lombok.Data;

import javax.validation.constraints.Max;
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
    //    @NotBlank(message = "商户等级名称不能为空")
    //    @Length(min = 1, max = 10, message = "商户等级名称不合法")
    private String name;
    
    /**
     * 拉新人数
     */
    @Max(value = 99999999, message = "拉新人数不合法")
    private Long invitationUserCount;
    
    /**
     * 续费人数
     */
    @Max(value = 99999999, message = "续费人数不合法")
    private Long renewalUserCount;
}
