package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.queryModel.asset.AssetAllocateRecordSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordRequest;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordSaveRequest;

/**
 * @author HeYafeng
 * @description 资产调拨服务
 * @date 2023/11/29 13:32:55
 */
public interface AssetAllocateRecordService {
    
    R save(AssetAllocateRecordRequest assetAllocateRecordRequest, Long uid);
    
    Integer insertOne(AssetAllocateRecordSaveRequest assetAllocateRecordSaveRequest);
}
