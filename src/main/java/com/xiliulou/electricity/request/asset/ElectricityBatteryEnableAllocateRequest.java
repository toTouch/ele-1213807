package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 电池调拨request
 * @date 2023/11/30 14:56:06
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityBatteryEnableAllocateRequest {
    
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 电池物理状态 0：在仓，1：不在仓 此处选择：在仓
     */
    private Integer physicsStatus;
    
    /**
     * 电池业务状态：1：已录入，2：租借，3：归还，4：异常交换 此处选择：已录入、归还
     */
    private List<Integer> businessStatusList;
    
    /**
     * 调拨电池的id
     */
    private List<Long> idList;
    
    private String sn;
    
    private Long size;
    
    private Long offset;
}
