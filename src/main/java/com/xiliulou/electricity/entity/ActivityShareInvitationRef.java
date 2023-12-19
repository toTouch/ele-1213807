package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 邀请返券活动和套餐返现活动映射表
 * 悦来能源2023/12需求特殊处理：邀请返券的数据迁移到邀请返现，并在扫码相关邀请人的返券二维码时跳转到套餐返券逻辑
 * @date 2023/12/19 09:12:22
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_activity_share_invitation_ref")
public class ActivityShareInvitationRef {
    
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 邀请人uid
     */
    private Long inviterUid;
    
    /**
     * 邀请返券活动id
     */
    private Long shareActivityId;
    
    /**
     * 套餐返现活动id
     */
    private Long invitationActivityId;
    
    /**
     * 0--正常 1--删除
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
