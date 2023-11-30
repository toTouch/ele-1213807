package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 查询柜机sn
 * @date 2023/11/27 14:24:36
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityCabinetSnSearchRequest {
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 门店id
     */
    private Long storeId;
    
    /**
     * 库存状态
     */
    private Integer stockStatus;
    
    private String sn;
    
    private Long size;
    
    private Long offset;
}
