package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.request.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetOutWarehouseRequest;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetServerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author zhangyongbo
 * @since 2023-11-21
 */
@Service("electricityCabinetV2Service")
@Slf4j
public class ElectricityCabinetV2ServiceImpl implements ElectricityCabinetV2Service {
    
    @Resource
    private ElectricityCabinetModelService electricityCabinetModelService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private ElectricityCabinetMapper electricityCabinetMapper;
    
    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Resource
    private ElectricityCabinetServerService electricityCabinetServerService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    
    @Override
    public Triple<Boolean, String, Object> save(ElectricityCabinetAddRequest electricityCabinetAddRequest) {
        
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        // 获取型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinetAddRequest.getModelId());
        
        // 厂家名称和型号都存在
        if (Objects.isNull(electricityCabinetModel)) {
            return Triple.of(false, "100558", "型号不存在");
        }
        
        if (existByProductKeyAndDeviceName(electricityCabinetAddRequest.getProductKey(), electricityCabinetAddRequest.getDeviceName())) {
            return Triple.of(false, "ELECTRICITY.0002", "换电柜的三元组已存在");
        }
        
        //操作频繁
        boolean result = redisService.setNx(CacheConstant.ELE_SAVE_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        //换电柜
        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        BeanUtil.copyProperties(electricityCabinetAddRequest, electricityCabinet);
        electricityCabinet.setTenantId(TenantContextHolder.getTenantId());
        electricityCabinet.setCreateTime(System.currentTimeMillis());
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        electricityCabinet.setStockStatus(StockStatusEnum.STOCK.getCode());
        
        DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.insert(electricityCabinet), i -> {
            
            //新增缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName(), electricityCabinet);
            
            //添加快递柜格挡
            electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
            //添加服务时间记录
            electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinet, electricityCabinet);
        });
        
          return Triple.of(true, null, electricityCabinet.getId());
    }
    
    @Override
    public boolean existByProductKeyAndDeviceName(String productKey, String deviceName) {
        Integer count = electricityCabinetMapper.existByProductKeyAndDeviceName(productKey, deviceName);
        if (Objects.nonNull(count)) {
            return true;
        }
        return false;
    }
    
    @Override
    public Triple<Boolean, String, Object> outWarehouse(ElectricityCabinetOutWarehouseRequest outWarehouseRequest){
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(outWarehouseRequest.getId());
        if(Objects.isNull(electricityCabinet)){
            return Triple.of(false, "ELECTRICITY.0005", "未找到换电柜");
        }
        return null;
    }
}
