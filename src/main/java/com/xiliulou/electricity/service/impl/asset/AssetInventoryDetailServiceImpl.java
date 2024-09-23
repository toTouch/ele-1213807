package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetInventoryDetailBO;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.mapper.asset.AssetInventoryDetailMapper;
import com.xiliulou.electricity.query.asset.AssetInventoryDetailBatchInventoryQueryModel;
import com.xiliulou.electricity.query.asset.AssetInventoryDetailQueryModel;
import com.xiliulou.electricity.query.asset.AssetInventoryDetailSaveQueryModel;
import com.xiliulou.electricity.query.asset.AssetInventoryQueryModel;
import com.xiliulou.electricity.query.asset.AssetInventoryUpdateDataQueryModel;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailBatchInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailRequest;
import com.xiliulou.electricity.request.asset.ElectricityBatterySnSearchRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.asset.AssetInventoryDetailService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.asset.AssetInventoryDetailVO;
import com.xiliulou.electricity.vo.asset.AssetInventoryVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 资产盘点详情业务
 * @date 2023/11/20 17:25:44
 */

@Service
public class AssetInventoryDetailServiceImpl implements AssetInventoryDetailService {
    
    @Autowired
    private AssetInventoryDetailMapper assetInventoryDetailMapper;
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    private AssetInventoryService assetInventoryService;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Resource
    private RedisService redisService;
    
    @Slave
    @Override
    public List<AssetInventoryDetailVO> listByOrderNo(AssetInventoryDetailRequest assetInventoryRequest) {
        // 模型转换
        AssetInventoryDetailQueryModel assetInventoryDetailQueryModel = new AssetInventoryDetailQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryDetailQueryModel);
        assetInventoryDetailQueryModel.setTenantId(TenantContextHolder.getTenantId());
        assetInventoryDetailQueryModel.setInventoryStatus(assetInventoryRequest.getStatus());
        
        List<AssetInventoryDetailVO> rspList = Collections.emptyList();
        
        List<AssetInventoryDetailBO> assetInventoryDetailBOList = assetInventoryDetailMapper.selectListByOrderNo(assetInventoryDetailQueryModel);
        if (CollectionUtils.isNotEmpty(assetInventoryDetailBOList)) {
            rspList = assetInventoryDetailBOList.stream().map(item -> {
                AssetInventoryDetailVO assetInventoryDetailVO = new AssetInventoryDetailVO();
                BeanUtils.copyProperties(item, assetInventoryDetailVO);
                if (Objects.nonNull(item.getFranchiseeId())) {
                    assetInventoryDetailVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(item.getFranchiseeId())).orElse(new Franchisee()).getName());
                }
                
                return assetInventoryDetailVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Slave
    @Override
    public Integer countTotal(AssetInventoryDetailRequest assetInventoryRequest) {
        // 模型转换
        AssetInventoryDetailQueryModel assetInventoryDetailQueryModel = new AssetInventoryDetailQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryDetailQueryModel);
        assetInventoryDetailQueryModel.setTenantId(TenantContextHolder.getTenantId());
        assetInventoryDetailQueryModel.setInventoryStatus(assetInventoryRequest.getStatus());
        
        return assetInventoryDetailMapper.countTotal(assetInventoryDetailQueryModel);
    }
    
    /**
     * @description 异步执行：将电池数据导入到资产详情表
     * @date 2023/11/21 14:40:16
     * @author HeYafeng
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer asyncBatteryProcess(ElectricityBatterySnSearchRequest snSearchRequest, String orderNo, Long operator) {
        List<ElectricityBatteryVO> electricityBatteryVOList = electricityBatteryService.listSnByFranchiseeId(snSearchRequest);
        if (CollectionUtils.isNotEmpty(electricityBatteryVOList)) {
            List<AssetInventoryDetailSaveQueryModel> inventoryDetailSaveQueryModelList = electricityBatteryVOList.stream().map(item -> AssetInventoryDetailSaveQueryModel.builder().orderNo(orderNo).sn(item.getSn())
                    .type(AssetTypeEnum.ASSET_TYPE_BATTERY.getCode()).franchiseeId(snSearchRequest.getFranchiseeId())
                    .inventoryStatus(AssetConstant.ASSET_INVENTORY_DETAIL_STATUS_NO).operator(operator).tenantId(snSearchRequest.getTenantId())
                    .delFlag(AssetConstant.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build()).collect(Collectors.toList());
            
            // 批量新增
            if (CollectionUtils.isNotEmpty(inventoryDetailSaveQueryModelList)) {
                assetInventoryDetailMapper.batchInsert(inventoryDetailSaveQueryModelList);
            }
        }
        return electricityBatteryVOList.size();
    }
    
    @Slave
    @Override
    public List<AssetInventoryDetailVO> listBySnListAndOrderNo(List<String> snList, String orderNo) {
        List<AssetInventoryDetailVO> rspList = Collections.emptyList();
        
        List<AssetInventoryDetailBO> assetInventoryDetailBOList = assetInventoryDetailMapper.selectListBySnListAndOrderNo(snList, orderNo);
        if (CollectionUtils.isNotEmpty(assetInventoryDetailBOList)) {
            rspList = assetInventoryDetailBOList.stream().map(item -> {
                
                AssetInventoryDetailVO assetInventoryDetailVO = new AssetInventoryDetailVO();
                BeanUtils.copyProperties(item, assetInventoryDetailVO);
                
                return assetInventoryDetailVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchInventory(AssetInventoryDetailBatchInventoryRequest inventoryRequest, Long operator) {
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_BATCH_INVENTORY_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Integer count = 0;
            List<String> snList = inventoryRequest.getSnList();
            if (CollectionUtils.isNotEmpty(snList)) {
                String orderNo = inventoryRequest.getOrderNo();
                Integer tenantId = TenantContextHolder.getTenantId();
                
                // tenant校验
                AssetInventoryQueryModel queryModel = AssetInventoryQueryModel.builder().orderNo(orderNo).build();
                AssetInventoryVO inventoryVO = assetInventoryService.queryByOrderNo(queryModel);
                if (Objects.nonNull(inventoryVO) && !Objects.equals(inventoryVO.getTenantId(), tenantId)) {
                    return R.ok();
                }
                
                List<AssetInventoryDetailVO> assetInventoryDetailVOList = listBySnListAndOrderNo(snList, inventoryRequest.getOrderNo());
                if (CollectionUtils.isNotEmpty(assetInventoryDetailVOList)) {
                    for (AssetInventoryDetailVO assetInventoryDetailVO : assetInventoryDetailVOList) {
                        if (Objects.equals(AssetConstant.ASSET_INVENTORY_DETAIL_STATUS_YES, assetInventoryDetailVO.getInventoryStatus())) {
                            return R.fail("300808", "您选择的电池中存在已盘点的数据，请刷新页面以获取最新状态后再进行操作");
                        }
                    }
                }
                
                AssetInventoryDetailBatchInventoryQueryModel assetInventoryDetailBatchInventoryQueryModel = AssetInventoryDetailBatchInventoryQueryModel.builder().orderNo(orderNo)
                        .status(inventoryRequest.getStatus()).snList(snList).operator(operator).tenantId(tenantId).updateTime(System.currentTimeMillis())
                        .build();
    
                //批量盘点
                if (CollectionUtils.isNotEmpty(inventoryRequest.getSnList())) {
                    count = batchInventoryBySnList(assetInventoryDetailBatchInventoryQueryModel);
                }
                
                // 查询剩余盘点数量
                Integer pendingTotal = assetInventoryDetailMapper.countPendingTotal(orderNo, tenantId);
                
                Integer status = AssetConstant.ASSET_INVENTORY_STATUS_TAKING;
                if (Objects.equals(pendingTotal, NumberConstant.ZERO)) {
                    status = AssetConstant.ASSET_INVENTORY_STATUS_FINISHED;
                }
                
                //同步盘点数据
                AssetInventoryUpdateDataQueryModel assetInventoryUpdateDataQueryModel = AssetInventoryUpdateDataQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                        .orderNo(inventoryRequest.getOrderNo()).inventoryCount(count).operator(operator).status(status)
                        .updateTime(System.currentTimeMillis()).build();
                
                assetInventoryService.updateByOrderNo(assetInventoryUpdateDataQueryModel);
            }
            
            return R.ok(count);
            
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_BATCH_INVENTORY_LOCK + operator);
        }
    }
    
    @Override
    public Integer batchInventoryBySnList(AssetInventoryDetailBatchInventoryQueryModel queryModel) {
        return assetInventoryDetailMapper.batchInventoryBySnList(queryModel);
    }
}
