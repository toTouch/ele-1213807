package com.xiliulou.electricity.entity.battery;

import lombok.Data;

/**
 * @author SJP
 * @date 2025-02-14 15:29
 **/
@Data
public class BatteryLabelRecord {
    /**
     * sn码
     */
    private String sn;
    
    /**
     * 旧电池标签
     */
    private Integer oldLabel;
    
    /**
     * 新电池标签
     */
    private Integer newLabel;
    
    /**
     * 操作人uid
     */
    private Long operatorUid;
    
    /**
     * 操作人名称
     */
    private String operatorName;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 加盟商名称
     */
    private String franchiseeName;
    
    /**
     * 修改时间
     */
    private String exchangeTime;
    
    private String createTime;
}
