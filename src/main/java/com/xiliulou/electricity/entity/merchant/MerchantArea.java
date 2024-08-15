package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 区域
 * @date 2024/2/6 13:34:14
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_area")
public class MerchantArea {
    
    private Long id;
    
    /**
     * 区域名称
     */
    private String name;
    
    /**
     * 删除标记(0-未删除，1-已删除)
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
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
}
