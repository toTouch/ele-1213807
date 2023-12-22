package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetWarehouseRecordBO;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.asset.AssetWarehouseDetail;
import com.xiliulou.electricity.entity.asset.AssetWarehouseRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.asset.AssetWarehouseRecordMapper;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseRecordQueryModel;
import com.xiliulou.electricity.request.asset.AssetSnWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetWarehouseRecordRequest;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.asset.AssetWarehouseDetailService;
import com.xiliulou.electricity.service.asset.AssetWarehouseRecordService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.asset.AssetWarehouseRecordVO;
import jodd.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private UserService userService;
    
    @Resource
    private AssetWarehouseDetailService assetWarehouseDetailService;
    
    @Slave
    @Override
    public List<AssetWarehouseRecordVO> listByWarehouseId(AssetWarehouseRecordRequest assetWarehouseRecordRequest) {
        List<AssetWarehouseRecordVO> rsp = null;
        
        AssetWarehouseRecordQueryModel queryModel = new AssetWarehouseRecordQueryModel();
        BeanUtils.copyProperties(assetWarehouseRecordRequest, queryModel);
        
        // sn模糊查询，先查detail表
        Map<String, List<AssetWarehouseDetail>> recordNoMap = null;
        String sn = assetWarehouseRecordRequest.getSn();
        if (StringUtil.isNotEmpty(sn)) {
            List<AssetWarehouseDetail> detailList = assetWarehouseDetailService.listBySn(assetWarehouseRecordRequest.getWarehouseId(), assetWarehouseRecordRequest.getSn());
            if (CollectionUtils.isNotEmpty(detailList)) {
                recordNoMap = detailList.stream().collect(Collectors.groupingBy(AssetWarehouseDetail::getRecordNo));
                queryModel.setRecordNoSet(recordNoMap.keySet());
            }
        }
        
        List<AssetWarehouseRecordBO> recordBOList = assetWarehouseRecordMapper.selectListByRecordNoSet(queryModel);
        if (CollectionUtils.isNotEmpty(recordBOList)) {
            rsp = new ArrayList<>();
    
            recordBOList = recordBOList.stream().distinct().collect(Collectors.toList());
            
            for (AssetWarehouseRecordBO record : recordBOList) {
                AssetWarehouseRecordVO warehouseRecordVO = new AssetWarehouseRecordVO();
                BeanUtils.copyProperties(record, warehouseRecordVO);
                
                List<AssetWarehouseDetail> detailList;
                if (MapUtils.isNotEmpty(recordNoMap)) {
                    detailList = recordNoMap.get(record.getRecordNo());
                } else {
                    detailList = assetWarehouseDetailService.listByRecordNo(record.getRecordNo());
                }
                
                List<String> snList = detailList.stream().map(AssetWarehouseDetail::getSn).collect(Collectors.toList());
                warehouseRecordVO.setSnList(snList);
                
                User user = userService.queryByUidFromCache(record.getOperator());
                warehouseRecordVO.setOperatorName(user.getName());
                
                rsp.add(warehouseRecordVO);
            }
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
    
        // sn模糊查询，先查detail表
        Map<String, List<AssetWarehouseDetail>> recordNoMap = null;
        String sn = assetWarehouseRecordRequest.getSn();
        if (StringUtil.isNotEmpty(sn)) {
            List<AssetWarehouseDetail> detailList = assetWarehouseDetailService.listBySn(assetWarehouseRecordRequest.getWarehouseId(), assetWarehouseRecordRequest.getSn());
            if (CollectionUtils.isNotEmpty(detailList)) {
                recordNoMap = detailList.stream().collect(Collectors.groupingBy(AssetWarehouseDetail::getRecordNo));
                queryModel.setRecordNoSet(recordNoMap.keySet());
            }
        }
    
        return assetWarehouseRecordMapper.countTotal(queryModel);
    }
    
    /**
     * @description 异步记录
     * @date 2023/12/20 16:12:13
     * @author HeYafeng
     */
    @Override
    public void asyncRecords(Integer tenantId, Long uid, List<AssetSnWarehouseRequest> snWarehouseList, Integer type, Integer operateType) {
        if (CollectionUtils.isNotEmpty(snWarehouseList)) {
            String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_WAREHOUSE_RECORD, uid);
            long nowTime = System.currentTimeMillis();
            
            List<AssetWarehouseRecord> batchInsertRecordList = new ArrayList<>();
            List<AssetWarehouseDetail> batchInsertDetailList = new ArrayList<>();
            
            snWarehouseList.forEach(item -> {
                Long warehouseId = item.getWarehouseId();
                String sn = item.getSn();
                AssetWarehouseRecord assetWarehouseRecord = AssetWarehouseRecord.builder().recordNo(orderNo).type(type).operateType(operateType).warehouseId(warehouseId)
                        .operator(uid).tenantId(tenantId).delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
                batchInsertRecordList.add(assetWarehouseRecord);
                
                AssetWarehouseDetail assetWarehouseDetail = AssetWarehouseDetail.builder().recordNo(orderNo).warehouseId(warehouseId).type(type).sn(sn).tenantId(tenantId)
                        .delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
                batchInsertDetailList.add(assetWarehouseDetail);
            });
            
            executorService.execute(() -> {
                assetWarehouseRecordMapper.batchInsert(batchInsertRecordList);
                assetWarehouseDetailService.batchInsert(batchInsertDetailList);
            });
        }
    }
    
    @Override
    public void asyncRecordOne(Integer tenantId, Long uid, Long warehouseId, String sn, Integer type, Integer operateType) {
        String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_WAREHOUSE_RECORD, uid);
        long nowTime = System.currentTimeMillis();
        
        AssetWarehouseRecord assetWarehouseRecord = AssetWarehouseRecord.builder().recordNo(orderNo).type(type).operateType(operateType).warehouseId(warehouseId).operator(uid)
                .tenantId(tenantId).delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
        
        AssetWarehouseDetail assetWarehouseDetail = AssetWarehouseDetail.builder().recordNo(orderNo).warehouseId(warehouseId).type(type).sn(sn).tenantId(tenantId)
                .delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
        
        assetWarehouseRecordMapper.insertOne(assetWarehouseRecord);
        assetWarehouseDetailService.insertOne(assetWarehouseDetail);
    }
    
    @Override
    public void asyncRecordByWarehouseId(Integer tenantId, Long uid, Long warehouseId, List<String> snList, Integer type, Integer operateType) {
        if (CollectionUtils.isNotEmpty(snList)) {
            String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_WAREHOUSE_RECORD, uid);
            long nowTime = System.currentTimeMillis();
            
            AssetWarehouseRecord assetWarehouseRecord = AssetWarehouseRecord.builder().recordNo(orderNo).type(type).operateType(operateType).warehouseId(warehouseId).operator(uid)
                    .tenantId(tenantId).delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
            
            List<AssetWarehouseDetail> batchInsertDetailList = new ArrayList<>();
            snList.forEach(sn -> {
                AssetWarehouseDetail assetWarehouseDetail = AssetWarehouseDetail.builder().recordNo(orderNo).warehouseId(warehouseId).type(type).sn(sn).tenantId(tenantId)
                        .delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
                batchInsertDetailList.add(assetWarehouseDetail);
            });
            
            executorService.execute(() -> {
                assetWarehouseRecordMapper.insertOne(assetWarehouseRecord);
                assetWarehouseDetailService.batchInsert(batchInsertDetailList);
            });
        }
    }
    
}
