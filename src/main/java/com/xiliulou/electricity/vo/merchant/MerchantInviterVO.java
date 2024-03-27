package com.xiliulou.electricity.vo.merchant;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 邀请人
 * @date 2024/3/27 09:10:47
 */
@Builder
@Data
public class MerchantInviterVO {
    
    private Long id;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 邀请人名称
     */
    private String inviterName;
    
    /**
     * 邀请人来源：1-邀请返券,2-邀请返现,3-套餐返现,4-渠道邀请,5-商户邀请
     */
    private Integer inviterSource;
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
}
