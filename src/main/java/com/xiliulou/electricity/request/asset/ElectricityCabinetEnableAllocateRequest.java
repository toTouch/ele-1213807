package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 查询可调拨柜机
 * @date 2023/11/30 18:52:25
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityCabinetEnableAllocateRequest {
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 门店id
     */
    private Long storeId;
    
    private String sn;
    
    private Long size;
    
    private Long offset;

}
