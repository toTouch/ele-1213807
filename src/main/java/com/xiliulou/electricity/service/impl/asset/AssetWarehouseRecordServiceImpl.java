package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.asset.AssetWarehouseRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.asset.AssetWarehouseRecordMapper;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseRecordQueryModel;
import com.xiliulou.electricity.request.asset.AssetSnWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetWarehouseRecordRequest;
import com.xiliulou.electricity.service.asset.AssetWarehouseRecordService;
import com.xiliulou.electricity.service.asset.AssetWarehouseService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
import com.xiliulou.electricity.vo.asset.AssetWarehouseRecordVO;
import jodd.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 库房资产记录业务
 * @date 2023/12/20 10:27:20
 */
@Service
public class AssetWarehouseRecordServiceImpl implements AssetWarehouseRecordService {
    
    protected XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("ASSET_WAREHOUSE_RECORD_HANDLE_THREAD_POOL", 3,
            "asset_warehouse_record_handle_thread_pool");
    
    @Resource
    private AssetWarehouseRecordMapper assetWarehouseRecordMapper;
    
    @Resource
    private AssetWarehouseService assetWarehouseService;
    
    @Slave
    @Override
    public List<AssetWarehouseRecordVO> listByWarehouseId(AssetWarehouseRecordRequest assetWarehouseRecordRequest) {
        List<AssetWarehouseRecordVO> rsp = null;
    
        AssetWarehouseRecordQueryModel queryModel = new AssetWarehouseRecordQueryModel();
        BeanUtils.copyProperties(assetWarehouseRecordRequest, queryModel);
        // TODO 处理同一单号数据
        List<AssetWarehouseRecord> assetWarehouseRecordList = assetWarehouseRecordMapper.selectListByWarehouseId(queryModel);
        if (CollectionUtils.isNotEmpty(assetWarehouseRecordList)) {
            rsp = assetWarehouseRecordList.stream().map(item -> {
                AssetWarehouseRecordVO assetWarehouseRecordVO = new AssetWarehouseRecordVO();
                BeanUtils.copyProperties(item, assetWarehouseRecordVO);
            
                AssetWarehouseNameVO assetWarehouseNameVO = assetWarehouseService.queryById(item.getWarehouseId());
                assetWarehouseRecordVO.setWarehouseName(assetWarehouseNameVO.getName());
            
                return assetWarehouseRecordVO;
            
            }).collect(Collectors.toList());
        }
    
        if (CollectionUtils.isEmpty(rsp)) {
            rsp = Collections.emptyList();
        }
    
        return rsp;
    }
    
    @Slave
    @Override
    public Integer countTotal(AssetWarehouseRecordRequest assetWarehouseRecordRequest) {
        AssetWarehouseRecordQueryModel queryModel = new AssetWarehouseRecordQueryModel();
        BeanUtils.copyProperties(assetWarehouseRecordRequest, queryModel);
        
        return assetWarehouseRecordMapper.countTotal(queryModel);
    }
    
    /**
     * @description 异步记录
     * @date 2023/12/20 16:12:13
     * @author HeYafeng
     */
    @Override
    public void asyncRecord(Integer tenantId, Long uid, List<AssetSnWarehouseRequest> snWarehouseList, Integer type, Integer operateType) {
        if (CollectionUtils.isNotEmpty(snWarehouseList)) {
            String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_WAREHOUSE_RECORD, uid);
            long nowTime = System.currentTimeMillis();
            List<AssetWarehouseRecord> batchInsertList = new ArrayList<>();
            
            snWarehouseList.forEach(item -> {
                String sn = item.getSn();
                Long warehouseId = item.getWarehouseId();
                
                if (Objects.nonNull(sn) && StringUtil.isNotEmpty(sn) && Objects.nonNull(warehouseId) && !Objects.equals(warehouseId, NumberConstant.ZERO_L)) {
                    AssetWarehouseRecord assetWarehouseRecord = AssetWarehouseRecord.builder().recordNo(orderNo).type(type).operateType(operateType).sn(sn).warehouseId(warehouseId)
                            .operator(uid).tenantId(tenantId).delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
                    
                    batchInsertList.add(assetWarehouseRecord);
                }
            });
            
            executorService.execute(() -> {
                assetWarehouseRecordMapper.batchInsert(batchInsertList);
            });
        }
    }
    
}
