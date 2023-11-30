package com.xiliulou.electricity.queryModel.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 电池调拨查询model
 * @date 2023/11/30 14:54:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityBatteryCanAllocateQueryModel {
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 电池物理状态 0：在仓，1：不在仓
     */
    private Integer physicsStatus;
    /**
     * 电池业务状态：1：已录入，2：租借，3：归还，4：异常交换
     */
    private List<Integer> businessStatusList;
    
    private Long size;
    
    private Long offset;
}
