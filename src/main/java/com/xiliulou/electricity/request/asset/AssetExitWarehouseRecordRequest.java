package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 退库记录
 * @date 2023/11/27 19:30:31
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetExitWarehouseRecordRequest {
    
    /**
     * 盘点订单号
     */
    private String orderNo;
    
    /**
     * 盘点加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 资产类型(1-电柜, 2-电池, 3-车辆)
     */
    private Integer type;
    
    /**
     * 操作人
     */
    private Long uid;
    
    private Long size;
    
    private Long offset;
    
}
