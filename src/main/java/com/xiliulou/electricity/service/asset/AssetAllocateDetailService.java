package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.queryModel.asset.AssetAllocateDetailSaveQueryModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产调拨详情服务
 * @date 2023/11/29 18:01:00
 */
public interface AssetAllocateDetailService {
    
    Integer batchInsert(List<AssetAllocateDetailSaveQueryModel> detailSaveQueryModelList);
}
