package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MerchantChannelEmployeeBindHistory
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-03-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_channel_employee_bind_history ")
public class MerchantChannelEmployeeBindHistory {
    
    /**
     * 主键id
     */
    private Long id;
    
    /**
     * 商户uid
     */
    private Long merchantUid;
    
    /**
     * 渠道员uid
     */
    private Long channelEmployeeUid;
    
    /**
     * 绑定类型 0：绑定 1：解绑
     */
    private Integer bindStatus;
    
    /**
     * 绑定时间
     */
    private Long bindTime;
    
    /**
     * 解绑时间
     */
    private Long unBindTime;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    
    /**
     * 更新时间
     */
    private Long updateTime;
}
