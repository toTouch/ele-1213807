package com.xiliulou.electricity.dto.battery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author SJP
 * @date 2025-02-18 19:46
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatteryLabelModifyDto {
    
    /**
     * 预修改电池标签
     */
    private Integer newLabel;
    
    /**
     * 操作人uid
     */
    private Long operatorUid;
    
    /**
     * 领用人，管理员uid或者商户id
     */
    private Long receiverId;
    
    public BatteryLabelModifyDto(Integer newLabel) {
        this.newLabel = newLabel;
    }
}
