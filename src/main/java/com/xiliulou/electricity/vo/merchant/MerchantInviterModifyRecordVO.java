package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 修改邀请人记录表
 * @date 2024/3/28 09:21:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantInviterModifyRecordVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 修改时间
     */
    private Long operateTime;
    
    /**
     * 原邀请人uid
     */
    private Long oldInviterUid;
    
    /**
     * 原邀请人名称
     */
    private String oldInviterName;
    
    /**
     * 邀请人来源：1-用户邀请，2-商户邀请
     */
    private Integer oldInviterSource;
    
    /**
     * 现邀请人uid
     */
    private Long inviterUid;
    
    /**
     * 现邀请人名称
     */
    private String inviterName;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 商户id
     */
    private Long merchantId;
    
    /**
     * 所属加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
}
