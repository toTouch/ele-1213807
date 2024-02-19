package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-04-15:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantLevelDTO {
    /**
     * 拉新人数
     */
    private Long invitationUserCount;
    
    /**
     * 续费人数
     */
    private Long renewalUserCount;
}
