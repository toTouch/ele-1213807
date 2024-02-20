package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 10:29
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_channel_employee")
public class ChannelEmployee {
    
    /**
     * 渠道员ID
     */
    private Long id;
    
    /**
     * 渠道员UID
     */
    private Long uid;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 区域ID
     */
    private Long areaId;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
}
