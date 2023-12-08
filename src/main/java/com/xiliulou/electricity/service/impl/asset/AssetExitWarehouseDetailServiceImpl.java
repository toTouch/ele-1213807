package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.mapper.asset.AssetExitWarehouseDetailMapper;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseDetailQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseDetailSaveQueryModel;
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
    private RedisService redisService;
    
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
    public R batchInsert(List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList, Long operator) {
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_DETAIL_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            return R.ok(assetExitWarehouseDetailMapper.batchInsert(detailSaveQueryModelList));
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_DETAIL_LOCK + operator);
        }
    }
}
