package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.request.asset.AssetAllocateDetailSaveRequest;
import com.xiliulou.electricity.vo.asset.AssetAllocateDetailVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产调拨详情服务
 * @date 2023/11/29 18:01:00
 */
public interface AssetAllocateDetailService {
    
    Integer batchInsert(List<AssetAllocateDetailSaveRequest> detailSaveRequestList);
    
    List<AssetAllocateDetailVO> listByPage(String orderNo, Integer tenantId);
}
