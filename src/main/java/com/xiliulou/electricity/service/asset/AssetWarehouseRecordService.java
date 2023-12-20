package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.request.asset.AssetSnWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetWarehouseRecordRequest;
import com.xiliulou.electricity.vo.asset.AssetWarehouseRecordVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房资产记录业务
 * @date 2023/12/20 10:27:20
 */
public interface AssetWarehouseRecordService {
    
    List<AssetWarehouseRecordVO> listByWarehouseId(AssetWarehouseRecordRequest assetWarehouseRecordRequest);
    
    Integer countTotal(AssetWarehouseRecordRequest assetWarehouseRecordRequest);
    
    void asyncRecord(Integer tenantId, Long uid, List<AssetSnWarehouseRequest> snWarehouseList, Integer type, Integer operateType);
}
