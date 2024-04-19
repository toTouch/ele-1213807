package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("t_merchant_inviter_modify_record")
public class MerchantInviterModifyRecord {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 现邀请人uid
     */
    private Long inviterUid;
    
    /**
     * 现邀请人名称
     */
    private String inviterName;
    
    /**
     * 原邀请人uid
     */
    private Long oldInviterUid;
    
    /**
     * 原邀请人名称
     */
    private String oldInviterName;
    
    /**
     * 邀请人来源:1-邀请返券,2-邀请返现,3-套餐返现,4-渠道邀请,5-商户邀请
     */
    private Integer oldInviterSource;
    
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
    
    /**
     * 操作人
     */
    private Long operator;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 删除标记:0--正常 1--删除
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
}
