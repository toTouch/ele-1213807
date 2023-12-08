package com.xiliulou.electricity.request.asset;

import lombok.Builder;
import lombok.Data;
/**
 * @author HeYafeng
 * @description 资产调拨：柜机调拨request
 * @date 2023/11/29 20:17:32
 */
@Builder
@Data
public class ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest {
    /**
     * 柜机id
     */
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 调出加盟商id
     */
    private Long sourceFranchiseeId;
    
    /**
     * 调出门店id
     */
    private Long sourceStoreId;
    
    /**
     * 调入加盟商id
     */
    private Long targetFranchiseeId;
    
    /**
     * 调入门店id
     */
    private Long targetStoreId;
    
    private String sn;
    
    private String productKey;
    
    private String deviceName;
    
}
