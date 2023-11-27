package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseDetailSaveQueryModel;

import java.util.List;

/**
 * @author HeYafeng
 * @description 退库详情服务
 * @date 2023/11/27 17:15:48
 */
public interface AssetExitWarehouseDetailService {
    
    R batchInsert(List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList, Long operator);

}
