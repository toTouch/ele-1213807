package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseRecordRequest;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseSaveRequest;
import com.xiliulou.electricity.vo.asset.AssetExitWarehouseVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 退库业务
 * @date 2023/11/27 09:34:54
 */
public interface AssetExitWarehouseRecordService {
    
    R save(AssetExitWarehouseSaveRequest assetExitWarehouseSaveRequest, Long operator);
    
    List<AssetExitWarehouseVO> listByPage(AssetExitWarehouseRecordRequest assetExitWarehouseRecordRequest);
    
    Integer countTotal(AssetExitWarehouseRecordRequest assetExitWarehouseRecordRequest);
    
    Integer insertOne(AssetExitWarehouseSaveQueryModel recordSaveQueryModel);
}
