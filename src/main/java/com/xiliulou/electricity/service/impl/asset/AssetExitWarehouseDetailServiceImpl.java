package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.mapper.asset.AssetExitWarehouseDetailMapper;
import com.xiliulou.electricity.query.asset.AssetExitWarehouseDetailQueryModel;
import com.xiliulou.electricity.query.asset.AssetExitWarehouseDetailSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseDetailRequest;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author HeYafeng
 * @description 退库详情业务
 * @date 2023/11/27 17:15:28
 */
@Service
public class AssetExitWarehouseDetailServiceImpl implements AssetExitWarehouseDetailService {
    
    @Autowired
    private AssetExitWarehouseDetailMapper assetExitWarehouseDetailMapper;
    
    @Slave
    @Override
    public List<String> listSnByOrderNo(AssetExitWarehouseDetailRequest assetExitWarehouseDetailRequest) {
        AssetExitWarehouseDetailQueryModel assetExitWarehouseDetailQueryModel = new AssetExitWarehouseDetailQueryModel();
        BeanUtils.copyProperties(assetExitWarehouseDetailRequest, assetExitWarehouseDetailQueryModel);
        assetExitWarehouseDetailQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return assetExitWarehouseDetailMapper.selectListSnByOrderNo(assetExitWarehouseDetailQueryModel);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList, Long operator) {
        return assetExitWarehouseDetailMapper.batchInsert(detailSaveQueryModelList);
    }
}
