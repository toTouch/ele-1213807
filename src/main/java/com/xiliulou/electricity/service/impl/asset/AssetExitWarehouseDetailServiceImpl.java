package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.mapper.asset.AssetExitWarehouseDetailMapper;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseDetailSaveQueryModel;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HeYafeng
 * @description 退库详情服务
 * @date 2023/11/27 17:15:28
 */
@Service
public class AssetExitWarehouseDetailServiceImpl implements AssetExitWarehouseDetailService {
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private AssetExitWarehouseDetailMapper assetExitWarehouseDetailMapper;
    
    @Override
    public R batchInsert(List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList, Long operator) {
    
    
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_DETAIL_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
    
        assetExitWarehouseDetailMapper.batchInsert(detailSaveQueryModelList);
    
        return null;
    }
}
