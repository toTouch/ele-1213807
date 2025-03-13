package com.xiliulou.electricity.service.impl.asset;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.ElectricityCabinetBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.EleCabinetConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.RegularConstant;
import com.xiliulou.electricity.dto.asset.CabinetBatchOutWarehouseDTO;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeRecord;
import com.xiliulou.electricity.enums.RentReturnNormEnum;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.enums.asset.WarehouseOperateTypeEnum;
import com.xiliulou.electricity.enums.thirdParty.ThirdPartyOperatorTypeEnum;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.asset.AssetBatchExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.AssetEnableExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCabinetEnableAllocateQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCabinetListSnByFranchiseeQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCabinetUpdateFranchiseeAndStoreQueryModel;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetSnWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetEnableAllocateRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetSnSearchRequest;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetExtraService;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetServerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.asset.AssetWarehouseRecordService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeRecordService;
import com.xiliulou.electricity.service.thirdParty.PushDataToThirdService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangyongbo
 * @since 2023-11-21
 */
@Service("electricityCabinetV2Service")
@Slf4j
public class ElectricityCabinetServiceV2Impl implements ElectricityCabinetV2Service {
    
    protected XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("RELOAD_ELECTRICITY_CABINET_GEO", 3, "reload_electricity_cabinet_geo_thread");
    
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
    
    @Resource
    private StoreService storeService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private AssetWarehouseRecordService assetWarehouseRecordService;
    
    @Resource
    private MerchantPlaceFeeRecordService merchantPlaceFeeRecordService;
    
    @Resource
    private ElectricityCabinetExtraService electricityCabinetExtraService;
    
    @Resource
    private PushDataToThirdService pushDataToThirdService;
    
    
    @Override
    public Triple<Boolean, String, Object> save(ElectricityCabinetAddRequest electricityCabinetAddRequest) {
        //操作频繁
        boolean result = redisService.setNx(CacheConstant.ELE_SAVE_UID + SecurityUtils.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            // 获取型号
            ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinetAddRequest.getModelId());
            
            // 厂家名称和型号都存在
            if (Objects.isNull(electricityCabinetModel)) {
                return Triple.of(false, "100558", "型号不存在");
            }
            
            if (existByProductKeyAndDeviceName(electricityCabinetAddRequest.getProductKey(), electricityCabinetAddRequest.getDeviceName())) {
                return Triple.of(false, "ELECTRICITY.0002", "换电柜的三元组已存在");
            }
            
            // 换电柜
            ElectricityCabinet electricityCabinet = new ElectricityCabinet();
            BeanUtil.copyProperties(electricityCabinetAddRequest, electricityCabinet);
            electricityCabinet.setTenantId(TenantContextHolder.getTenantId());
            electricityCabinet.setCreateTime(System.currentTimeMillis());
            electricityCabinet.setUpdateTime(System.currentTimeMillis());
            electricityCabinet.setDelFlag(ElectricityCabinet.DEL_NORMAL);
            electricityCabinet.setStockStatus(StockStatusEnum.STOCK.getCode());
            
            // 下列字段设置默认
            electricityCabinet.setName(StringUtils.EMPTY);
            electricityCabinet.setLongitude(0.0);
            electricityCabinet.setLatitude(0.0);
            electricityCabinet.setServicePhone(StringUtils.EMPTY);
            electricityCabinet.setBusinessTime(StringUtils.EMPTY);
            electricityCabinet.setStoreId(0L);
            electricityCabinet.setFullyCharged(0.00);
            electricityCabinet.setExchangeType(electricityCabinetAddRequest.getExchangeType());
    
            DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.insert(electricityCabinet), i -> {
        
                // 新增缓存
                redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
                redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName(),
                        electricityCabinet);
        
                // 添加格挡
                electricityCabinetBoxService.batchInsertBoxByModelIdV2(electricityCabinetModel, electricityCabinet.getId());
                // 添加服务时间记录
                electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinet, electricityCabinet);
                
                // 新增柜机扩展参数
                ElectricityCabinetExtra electricityCabinetExtra = ElectricityCabinetExtra.builder().eid(electricityCabinet.getId().longValue())
                        .batteryCountType(EleCabinetConstant.BATTERY_COUNT_TYPE_NORMAL).tenantId(electricityCabinet.getTenantId()).delFlag(electricityCabinet.getDelFlag())
                        .createTime(electricityCabinet.getCreateTime()).updateTime(electricityCabinet.getUpdateTime()).build();
                electricityCabinetExtraService.insertOne(electricityCabinetExtra);
                
                // 异步记录
                Long warehouseId = electricityCabinetAddRequest.getWarehouseId();
                if (Objects.nonNull(warehouseId) && !Objects.equals(warehouseId, NumberConstant.ZERO_L)) {
                    Long uid = Objects.requireNonNull(SecurityUtils.getUserInfo()).getUid();
                    String sn = electricityCabinetAddRequest.getSn();
                    
                    assetWarehouseRecordService.asyncRecordOne(TenantContextHolder.getTenantId(), uid, warehouseId, sn, AssetTypeEnum.ASSET_TYPE_CABINET.getCode(),
                            WarehouseOperateTypeEnum.WAREHOUSE_OPERATE_TYPE_IN.getCode());
                }
    
                // 给第三方推送柜机信息
                pushDataToThirdService.asyncPushCabinet(TtlTraceIdSupport.get(), electricityCabinet.getTenantId(), electricityCabinet.getId().longValue(),
                        ThirdPartyOperatorTypeEnum.ELE_CABINET_ADD.getType());
            });
            
            return Triple.of(true, null, electricityCabinet.getId());
        } finally {
            redisService.delete(CacheConstant.ELE_SAVE_UID + SecurityUtils.getUid());
        }
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
    public Triple<Boolean, String, Object> outWarehouse(ElectricityCabinetOutWarehouseRequest outWarehouseRequest) {
        //校验加盟商
        Franchisee franchisee = franchiseeService.queryByIdFromCache(outWarehouseRequest.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return Triple.of(false, "ELECTRICITY.0038", "未找到加盟商");
        }
        
        // 校验门店
        Store store = storeService.queryByIdFromCache(outWarehouseRequest.getStoreId());
        if (Objects.isNull(store)) {
            return Triple.of(false, "ELECTRICITY.0018", "门店不存在");
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(outWarehouseRequest.getId());
        if (Objects.isNull(electricityCabinet)) {
            return Triple.of(false, "ELECTRICITY.0005", "未找到换电柜");
        }
        
        if (Objects.equals(StockStatusEnum.UN_STOCK.getCode(), electricityCabinet.getStockStatus())) {
            return Triple.of(false, "100559", "已选择项中有已出库电柜，请重新选择后操作");
        }
        
        //  如果场地费不为空则需要判断 不能小于零 小数最多两位  整数不能大于8位
        if (Objects.nonNull(outWarehouseRequest.getPlaceFee())) {
            // 场地费必须大于零
            if (Objects.equals(outWarehouseRequest.getPlaceFee().compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                return Triple.of(false, "120235", "场地费必须大于等于零");
            }
        
            // 场地费不能是负数
            String placeFeeStr = outWarehouseRequest.getPlaceFee().toString();
            if (!RegularConstant.PLACE_PATTERN.matcher(placeFeeStr).matches()) {
                return Triple.of(false, "120234", "场地费保留两位小数且整数部分不能超过8位");
            }
        }
        
        
        //判断参数
        if (Objects.nonNull(outWarehouseRequest.getBusinessTimeType())) {
            if (Objects.equals(outWarehouseRequest.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.ALL_DAY)) {
                electricityCabinet.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
            }
            if (Objects.equals(outWarehouseRequest.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.CUSTOMIZE_TIME)) {
                if (Objects.isNull(outWarehouseRequest.getBeginTime()) || Objects.isNull(outWarehouseRequest.getEndTime())
                        || outWarehouseRequest.getBeginTime() > outWarehouseRequest.getEndTime()) {
                    return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
                }
                electricityCabinet.setBusinessTime(outWarehouseRequest.getBeginTime() + "-" + outWarehouseRequest.getEndTime());
            }
            if (Objects.isNull(electricityCabinet.getBusinessTime())) {
                return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
            }
        }
    
        // 获取场地费变更记录
        MerchantPlaceFeeRecord finalMerchantPlaceFeeRecord = getPlaceFeeRecord(electricityCabinet, outWarehouseRequest, SecurityUtils.getUserInfo());
        
        BeanUtil.copyProperties(outWarehouseRequest, electricityCabinet);
        electricityCabinet.setStockStatus(StockStatusEnum.UN_STOCK.getCode());
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        
        DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.updateEleById(electricityCabinet), i -> {
            //更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
            //修改柜机服务时间信息
            electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinet, electricityCabinet);
            //缓存柜机GEO信息
            electricityCabinetService.addElectricityCabinetLocToGeo(electricityCabinet);
            
            // 默认设置租电规则为全部可租电，最少保留一块电池
            ElectricityCabinetExtra cabinetExtra = new ElectricityCabinetExtra();
            cabinetExtra.setEid(Long.valueOf(electricityCabinet.getId()));
            cabinetExtra.setRentTabType(RentReturnNormEnum.ALL_RENT.getCode());
            cabinetExtra.setReturnTabType(RentReturnNormEnum.MIN_RETURN.getCode());
            cabinetExtra.setUpdateTime(System.currentTimeMillis());
            electricityCabinetExtraService.update(cabinetExtra);
            
            // 异步记录
            List<ElectricityCabinetBO> electricityCabinetBOList = electricityCabinetMapper.selectListByIdList(List.of(outWarehouseRequest.getId()));
            if (CollectionUtils.isNotEmpty(electricityCabinetBOList)) {
                Long warehouseId = electricityCabinetBOList.get(NumberConstant.ZERO).getWarehouseId();
        
                if (Objects.nonNull(warehouseId) && !Objects.equals(warehouseId, NumberConstant.ZERO_L)) {
                    Long uid = Objects.requireNonNull(SecurityUtils.getUserInfo()).getUid();
                    String sn = outWarehouseRequest.getSn();
            
                    assetWarehouseRecordService.asyncRecordOne(TenantContextHolder.getTenantId(), uid, warehouseId, sn, AssetTypeEnum.ASSET_TYPE_CABINET.getCode(),
                            WarehouseOperateTypeEnum.WAREHOUSE_OPERATE_TYPE_OUT.getCode());
                }
            }
    
            // 增加场地费变更记录
            if (Objects.nonNull(finalMerchantPlaceFeeRecord)) {
                merchantPlaceFeeRecordService.asyncInsertOne(finalMerchantPlaceFeeRecord);
            }
        });
        
        return Triple.of(true, null, null);
    }
    
    private MerchantPlaceFeeRecord getPlaceFeeRecord(ElectricityCabinet electricityCabinet, ElectricityCabinetOutWarehouseRequest outWarehouseRequest, TokenUser user) {
        BigDecimal oldFee = BigDecimal.ZERO;
        BigDecimal newFee = BigDecimal.ZERO;
        // 判断新的场地费用和就的场地费用是否存在变化如果存在变化则将变换存入到历史表
        if (Objects.nonNull(electricityCabinet.getPlaceFee())) {
            oldFee = electricityCabinet.getPlaceFee();
        } else {
            oldFee = new BigDecimal(NumberConstant.MINUS_ONE);
        }
        
        if (Objects.nonNull(outWarehouseRequest.getPlaceFee())) {
            newFee = outWarehouseRequest.getPlaceFee();
        } else {
            newFee = new BigDecimal(NumberConstant.MINUS_ONE);
        }
        
        
        MerchantPlaceFeeRecord merchantPlaceFeeRecord = null;
        // 场地费有变化则进行记录
        if (!Objects.equals(newFee.compareTo(oldFee), NumberConstant.ZERO)) {
            merchantPlaceFeeRecord = new MerchantPlaceFeeRecord();
            merchantPlaceFeeRecord.setCabinetId(outWarehouseRequest.getId());
            if (!Objects.equals(newFee.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                merchantPlaceFeeRecord.setNewPlaceFee(newFee);
            }
            if (!Objects.equals(oldFee.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                merchantPlaceFeeRecord.setOldPlaceFee(oldFee);
            }
            
            if (Objects.nonNull(user)) {
                merchantPlaceFeeRecord.setModifyUserId(user.getUid());
                merchantPlaceFeeRecord.setTenantId(electricityCabinet.getTenantId());
                long currentTimeMillis = System.currentTimeMillis();
                merchantPlaceFeeRecord.setCreateTime(currentTimeMillis);
                merchantPlaceFeeRecord.setUpdateTime(currentTimeMillis);
            }
        }
        
        return merchantPlaceFeeRecord;
    }
    
    @Override
    public Triple<Boolean, String, Object> batchOutWarehouse(ElectricityCabinetBatchOutWarehouseRequest batchOutWarehouseRequest) {
        if (!redisService.setNx(CacheConstant.ELE_BATCH_OUT_WAREHOUSE + SecurityUtils.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
    
        //  如果场地费不为空则需要判断 不能小于零 小数最多两位  整数不能大于8位
        if (Objects.nonNull(batchOutWarehouseRequest.getPlaceFee())) {
            // 场地费必须大于零
            if (Objects.equals(batchOutWarehouseRequest.getPlaceFee().compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                return Triple.of(false, "120235", "场地费必须大于等于零");
            }
        
            // 小数最多两位  整数不能大于8位
            String placeFeeStr = batchOutWarehouseRequest.getPlaceFee().toString();
            if (!RegularConstant.PLACE_PATTERN.matcher(placeFeeStr).matches()) {
                return Triple.of(false, "120234", "场地费保留两位小数且整数部分不能超过8位");
            }
        }
        
        List<Integer> eleIdList = batchOutWarehouseRequest.getIdList();
        // 校验已出库的
        List<ElectricityCabinet> electricityCabinetList = electricityCabinetMapper.homeOne(eleIdList, TenantContextHolder.getTenantId());
        List<ElectricityCabinet> unStockList = electricityCabinetList.stream()
                .filter(electricityCabinet -> Objects.equals(StockStatusEnum.UN_STOCK.getCode(), electricityCabinet.getStockStatus())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(unStockList)) {
            return Triple.of(false, "100559", "已选择项中有已出库电柜，请重新选择后操作");
        }
        
        // 设置营业时间参数
        String businessTime = null;
        if (Objects.nonNull(batchOutWarehouseRequest.getBusinessTimeType())) {
            // 设置全天
            if (Objects.equals(batchOutWarehouseRequest.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.ALL_DAY)) {
                businessTime = ElectricityCabinetAddAndUpdate.ALL_DAY;
            }
            
            // 自定义时间段
            if (Objects.equals(batchOutWarehouseRequest.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.CUSTOMIZE_TIME)) {
                if (Objects.isNull(batchOutWarehouseRequest.getBeginTime()) || Objects.isNull(batchOutWarehouseRequest.getEndTime())
                        || batchOutWarehouseRequest.getBeginTime() > batchOutWarehouseRequest.getEndTime()) {
                    return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
                }
                businessTime = batchOutWarehouseRequest.getBeginTime() + "-" + batchOutWarehouseRequest.getEndTime();
            }
            
            if (Objects.isNull(businessTime)) {
                return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
            }
        }
        
        List<Integer> emptyNameIdList = electricityCabinetList.stream().filter(electricityCabinet -> StringUtils.isBlank(electricityCabinet.getName()))
                .map(ElectricityCabinet::getId).collect(Collectors.toList());
        List<Integer> nameIdList = electricityCabinetList.stream().filter(electricityCabinet -> StringUtils.isNotBlank(electricityCabinet.getName())).map(ElectricityCabinet::getId)
                .collect(Collectors.toList());
        
        CabinetBatchOutWarehouseDTO batchOutWareHouseDTO = new CabinetBatchOutWarehouseDTO();
        BeanUtil.copyProperties(batchOutWarehouseRequest, batchOutWareHouseDTO);
        batchOutWareHouseDTO.setUpdateTime(System.currentTimeMillis());
        batchOutWareHouseDTO.setBusinessTime(businessTime);
        batchOutWareHouseDTO.setTenantId(TenantContextHolder.getTenantId());
        
        // 名称需要批量修改的（如果没有名称，则统一修改为当前传入的name值）
        if (CollectionUtils.isNotEmpty(emptyNameIdList)) {
            batchOutWareHouseDTO.setIdList(emptyNameIdList);
            electricityCabinetMapper.batchOutWarehouse(batchOutWareHouseDTO);
        }
        
        // 名称不需要批量修改的（如果有名称，则不需要修改当前的name）
        if (CollectionUtils.isNotEmpty(nameIdList)) {
            batchOutWareHouseDTO.setName(null);
            batchOutWareHouseDTO.setIdList(nameIdList);
            electricityCabinetMapper.batchOutWarehouse(batchOutWareHouseDTO);
        }
        
        electricityCabinetList.forEach(item -> {
            // 默认设置租电规则为全部可租电，最少保留一块电池
            ElectricityCabinetExtra cabinetExtra = new ElectricityCabinetExtra();
            cabinetExtra.setEid(Long.valueOf(item.getId()));
            cabinetExtra.setRentTabType(RentReturnNormEnum.ALL_RENT.getCode());
            cabinetExtra.setReturnTabType(RentReturnNormEnum.MIN_RETURN.getCode());
            cabinetExtra.setUpdateTime(System.currentTimeMillis());
            electricityCabinetExtraService.update(cabinetExtra);
            
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + item.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + item.getProductKey() + item.getDeviceName());
            //缓存柜机GEO信息
            redisService.addGeo(CacheConstant.CACHE_ELECTRICITY_CABINET_GEO + TenantContextHolder.getTenantId(), item.getId().toString(), new Point(batchOutWarehouseRequest.getLongitude(), batchOutWarehouseRequest.getLatitude()));
        });
    
        // 保存场地费变更记录
        merchantPlaceFeeRecordService.asyncRecords(electricityCabinetList, batchOutWarehouseRequest, SecurityUtils.getUserInfo(), TenantContextHolder.getTenantId());
        
        // 异步记录
        List<ElectricityCabinetBO> electricityCabinetBOList = electricityCabinetMapper.selectListByIdList(batchOutWarehouseRequest.getIdList());
        if (CollectionUtils.isNotEmpty(electricityCabinetBOList)) {
            List<AssetSnWarehouseRequest> snWarehouseList = electricityCabinetBOList.stream()
                    .filter(item -> Objects.nonNull(item.getWarehouseId()))
                    .map(item -> AssetSnWarehouseRequest.builder().sn(item.getSn()).warehouseId(item.getWarehouseId()).build()).collect(Collectors.toList());
        
            Long uid = Objects.requireNonNull(SecurityUtils.getUserInfo()).getUid();
        
            assetWarehouseRecordService.asyncRecords(TenantContextHolder.getTenantId(), uid, snWarehouseList, AssetTypeEnum.ASSET_TYPE_CABINET.getCode(),
                    WarehouseOperateTypeEnum.WAREHOUSE_OPERATE_TYPE_BATCH_OUT.getCode());
        }
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public Integer existsByWarehouseId(Long wareHouseId) {
        
        return electricityCabinetMapper.existsByWarehouseId(wareHouseId);
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetVO> listByFranchiseeIdAndStockStatus(ElectricityCabinetSnSearchRequest electricityCabinetSnSearchRequest) {
        
        ElectricityCabinetListSnByFranchiseeQueryModel electricityCabinetListSnByFranchiseeQueryModel = new ElectricityCabinetListSnByFranchiseeQueryModel();
        BeanUtil.copyProperties(electricityCabinetSnSearchRequest, electricityCabinetListSnByFranchiseeQueryModel);
        electricityCabinetListSnByFranchiseeQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<ElectricityCabinetVO> rspList = null;
        List<ElectricityCabinetBO> electricityCabinetBOList = electricityCabinetMapper.selectListByFranchiseeIdAndStockStatus(electricityCabinetListSnByFranchiseeQueryModel);
        if (CollectionUtils.isNotEmpty(electricityCabinetBOList)) {
            rspList = electricityCabinetBOList.stream().map(item -> {
                
                ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
                BeanUtil.copyProperties(item, electricityCabinetVO);
                
                return electricityCabinetVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchExitWarehouse(AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest) {
        AssetBatchExitWarehouseQueryModel assetBatchExitWarehouseQueryModel = new AssetBatchExitWarehouseQueryModel();
        BeanUtil.copyProperties(assetBatchExitWarehouseRequest, assetBatchExitWarehouseQueryModel);
        assetBatchExitWarehouseQueryModel.setUpdateTime(System.currentTimeMillis());
        
        return electricityCabinetMapper.batchExitWarehouse(assetBatchExitWarehouseQueryModel);
    }
    
    @Override
    public Integer batchUpdateFranchiseeIdAndStoreId(List<ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest> batchUpdateFranchiseeAndStoreRequestList) {
        Integer count = NumberConstant.ZERO;
        
        for (ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest updateFranchiseeAndStoreRequest : batchUpdateFranchiseeAndStoreRequestList) {
            ElectricityCabinetUpdateFranchiseeAndStoreQueryModel updateFranchiseeAndStoreQueryModel = new ElectricityCabinetUpdateFranchiseeAndStoreQueryModel();
            BeanUtil.copyProperties(updateFranchiseeAndStoreRequest, updateFranchiseeAndStoreQueryModel);
            updateFranchiseeAndStoreQueryModel.setUpdateTime(System.currentTimeMillis());
            
            electricityCabinetMapper.updateFranchiseeIdAndStoreId(updateFranchiseeAndStoreQueryModel);
            count += 1;
            
            //清理缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + updateFranchiseeAndStoreRequest.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + updateFranchiseeAndStoreRequest.getProductKey() + updateFranchiseeAndStoreRequest.getDeviceName());
        }
        
        return count;
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetVO> listEnableAllocateCabinet(ElectricityCabinetEnableAllocateRequest enableAllocateRequest) {
        ElectricityCabinetEnableAllocateQueryModel enableAllocateQueryModel = new ElectricityCabinetEnableAllocateQueryModel();
        BeanUtil.copyProperties(enableAllocateRequest, enableAllocateQueryModel);
        enableAllocateQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<ElectricityCabinetVO> rspList = null;
        List<ElectricityCabinetBO> electricityCabinetBOList = electricityCabinetMapper.selectListEnableAllocateCabinet(enableAllocateQueryModel);
        if (CollectionUtils.isNotEmpty(electricityCabinetBOList)) {
            rspList = electricityCabinetBOList.stream().map(item -> {
                ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
                BeanUtil.copyProperties(item, electricityCabinetVO);
                
                return electricityCabinetVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            rspList = Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetVO> listEnableExitWarehouseCabinet(AssetEnableExitWarehouseQueryModel queryModel) {
        List<ElectricityCabinetBO> electricityCabinetBOList = electricityCabinetMapper.selectListEnableExitWarehouseCabinet(queryModel);
        
        List<ElectricityCabinetVO> rspList = null;
        if (CollectionUtils.isNotEmpty(electricityCabinetBOList)) {
            rspList = electricityCabinetBOList.stream().map(item -> {
                ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
                BeanUtil.copyProperties(item, electricityCabinetVO);
                
                return electricityCabinetVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            rspList = Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetVO> listBySnList(List<String> snList, Integer tenantId, Long franchiseeId) {
        List<ElectricityCabinetBO> electricityCabinetBOList = electricityCabinetMapper.selectListBySnList(snList, tenantId, franchiseeId);
        
        List<ElectricityCabinetVO> rspList = null;
        if (CollectionUtils.isNotEmpty(electricityCabinetBOList)) {
            rspList = electricityCabinetBOList.stream().map(item -> {
                ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
                BeanUtil.copyProperties(item, electricityCabinetVO);
                
                return electricityCabinetVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            rspList = Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Override
    public Integer reloadEleCabinetGeo() {
        if (!redisService.setNx(CacheConstant.CACHE_RELOAD_ELECTRICITY_CABINET_GEO, "1", 120 * 1000L, false)) {
            return 0;
        }
    
        executorService.execute(() -> {
            Long offset = 0L;
            Long size = 200L;
            while (true) {
                List<ElectricityCabinetVO> eleCabinets = electricityCabinetMapper.selectListByPage(size, offset);
                if (!DataUtil.collectionIsUsable(eleCabinets)) {
                    break;
                }
        
                eleCabinets.forEach(e -> {
                    if (Objects.isNull(e.getLatitude()) || Objects.isNull(e.getLongitude())) {
                        return;
                    }
                    //缓存柜机GEO信息
                    redisService.addGeo(CacheConstant.CACHE_ELECTRICITY_CABINET_GEO + e.getTenantId(), e.getId().toString(), new Point(e.getLongitude(), e.getLatitude()));
                });
                offset += size;
            }
    
            redisService.delete(CacheConstant.CACHE_RELOAD_ELECTRICITY_CABINET_GEO);
        });
        
        return 1;
    }
    
}
