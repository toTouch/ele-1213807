package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 区域
 * @date 2024/2/6 15:15:50
 */
@Data
public class MerchantAreaVO {
    
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
     * 柜机数
     */
    private Integer cabinetNun;
    
    /**
     * 加盟商
     */
    private String franchiseeName;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
}
