package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-04-15:42
 */
@Data
public class MerchantLevelVO {
    
    private Long id;
    
    /**
     * 商户等级
     */
    private String level;
    
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
