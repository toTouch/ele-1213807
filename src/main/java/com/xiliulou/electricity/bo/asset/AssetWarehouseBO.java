package com.xiliulou.electricity.bo.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 库房BO
 * @date 2023/11/21 20:20:13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetWarehouseBO {
    
    /**
     * 主键Id
     */
    private Long id;
    
    /**
     * 库房名称
     */
    private String name;
    
    /**
     * 库房状态(0-启用， 1-禁用)
     */
    private Integer status;
    
    /**
     * 管理人姓名
     */
    private String managerName;
    
    /**
     * 联系方式座机、手机号均可
     */
    private String managerPhone;
    
    /**
     * 库房地址
     */
    private String address;
    
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
     * 加盟商id
     */
    private Long franchiseeId;
}
