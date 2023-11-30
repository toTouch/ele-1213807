package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.electricity.mapper.asset.AssetAllocateDetailMapper;
import com.xiliulou.electricity.queryModel.asset.AssetAllocateDetailSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetAllocateDetailSaveRequest;
import com.xiliulou.electricity.service.asset.AssetAllocateDetailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 资产调拨详情服务
 * @date 2023/11/29 18:01:27
 */
@Service
public class AssetAllocateDetailServiceImpl implements AssetAllocateDetailService {
    
    @Autowired
    private AssetAllocateDetailMapper assetAllocateDetailMapper;
    
    @Override
    public Integer batchInsert(List<AssetAllocateDetailSaveRequest> detailSaveRequestList) {
        List<AssetAllocateDetailSaveQueryModel> detailSaveQueryModelList = detailSaveRequestList.stream().map(item -> {
            AssetAllocateDetailSaveQueryModel assetAllocateDetailSaveQueryModel = new AssetAllocateDetailSaveQueryModel();
            BeanUtils.copyProperties(item, assetAllocateDetailSaveQueryModel);
            
            return assetAllocateDetailSaveQueryModel;
            
        }).collect(Collectors.toList());
        
        return assetAllocateDetailMapper.batchInsert(detailSaveQueryModelList);
    }
}
