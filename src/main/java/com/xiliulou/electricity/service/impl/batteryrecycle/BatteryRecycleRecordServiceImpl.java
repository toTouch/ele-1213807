package com.xiliulou.electricity.service.impl.batteryrecycle;

import cn.hutool.core.date.DateUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.batteryrecycle.BatteryRecycleRecord;
import com.xiliulou.electricity.enums.batteryrecycle.BatteryRecycleStatusEnum;
import com.xiliulou.electricity.mapper.batteryrecycle.BatteryRecycleRecordMapper;
import com.xiliulou.electricity.query.batteryRecycle.BatteryRecycleQueryModel;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecycleCancelRequest;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecycleSaveOrUpdateRequest;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecyclePageRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.batteryRecycle.BatteryRecycleRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.recycle.BatteryRecycleCancelResultVO;
import com.xiliulou.electricity.vo.recycle.BatteryRecycleSaveResultVO;
import com.xiliulou.electricity.vo.recycle.BatteryRecycleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 电池回收记录表(TBatteryRecycleRecord)表服务实现类
 *
 * @author maxiaodong
 * @since 2024-10-30 10:47:58
 */
@Service
@Slf4j
public class BatteryRecycleRecordServiceImpl implements BatteryRecycleRecordService {
    
    @Resource
    private BatteryRecycleRecordMapper batteryRecycleRecordMapper;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private UserService userService;
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("BATTERY-RECYCLE-THREAD-POOL", 2, "batteryRecycleThread:");
    
    
    @Override
    public Triple<Boolean, String, Object> save(BatteryRecycleSaveOrUpdateRequest saveRequest, Long uid) {
        if (!redisService.setNx(CacheConstant.BATTERY_RECYCLE_SAVE_UID + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        if (saveRequest.getBatterySnList().size() > 10000) {
            return Triple.of(false, "100670", "已超出最大数量 10000 条，请检查");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        List<ElectricityBattery> existsBatteryList = checkBatterySnList(saveRequest.getBatterySnList(), tenantId, saveRequest.getBindFranchiseeIdList());
        if (ObjectUtils.isEmpty(existsBatteryList)) {
            BatteryRecycleSaveResultVO recycleSaveResultVO = BatteryRecycleSaveResultVO.builder().successCount(0).failCount(saveRequest.getBatterySnList().size())
                    .notExistBatterySnList(saveRequest.getBatterySnList()).build();
            return Triple.of(true, "", recycleSaveResultVO);
        }
        
        Map<String, ElectricityBattery> batteryMap = existsBatteryList.stream().collect(Collectors.toMap(ElectricityBattery::getSn, Function.identity(), (v1, v2) -> v1));
        List<String> existsBatterySnList = new ArrayList<>(batteryMap.keySet());
        List<String> notExistsBatterySnList = ListUtils.subtract(saveRequest.getBatterySnList(), existsBatterySnList);
        
        threadPool.execute(() -> {
            // 检测今天是否存在回收记录
            try {
                String monthDay = DateUtil.format(new Date(), DateFormatConstant.MONTH_DAY_FORMAT);
                String key = String.format(CacheConstant.BATTERY_RECYCLE_BATCH_NO, tenantId, monthDay);
                String batchNo = redisService.get(key);
                Integer number = 0;
                
                if (StringUtils.isEmpty(batchNo)) {
                    BatteryRecycleRecord batteryRecycleRecordExist = batteryRecycleRecordMapper.selectListLastRecycleRecordByTime(
                            DateUtils.getStartTimeByTimeStamp(System.currentTimeMillis()), System.currentTimeMillis(), tenantId);
                    if (Objects.nonNull(batteryRecycleRecordExist)) {
                        number = Integer.valueOf(batteryRecycleRecordExist.getBatchNo().substring(monthDay.length()));
                    }
                } else {
                    number = Integer.valueOf(batchNo.substring(monthDay.length()));
                }
                
                number = number + 1;
                batchNo = monthDay + number;
                
                redisService.set(String.format(CacheConstant.BATTERY_RECYCLE_BATCH_NO, tenantId, monthDay), batchNo, 24 * 60 * 60 * 1000L, TimeUnit.MILLISECONDS);
                
                String finalBatchNo = batchNo;
                List<List<String>> partition = ListUtils.partition(existsBatterySnList, 500);
                partition.stream().forEach(snList -> {
                    List<BatteryRecycleRecord> batteryRecycleRecords = snList.stream().map(sn -> {
                        BatteryRecycleRecord batteryRecycleRecord = new BatteryRecycleRecord();
                        batteryRecycleRecord.setBatchNo(finalBatchNo);
                        batteryRecycleRecord.setSn(sn);
                        ElectricityBattery electricityBattery = batteryMap.get(sn);
                        if (Objects.nonNull(electricityBattery)) {
                            batteryRecycleRecord.setBatteryId(electricityBattery.getId());
                            batteryRecycleRecord.setFranchiseeId(electricityBattery.getFranchiseeId());
                        } else {
                            batteryRecycleRecord.setBatteryId(NumberConstant.ZERO_L);
                            batteryRecycleRecord.setFranchiseeId(NumberConstant.ZERO_L);
                        }
                        batteryRecycleRecord.setRecycleReason(saveRequest.getRecycleReason());
                        batteryRecycleRecord.setStatus(BatteryRecycleStatusEnum.INIT.getCode());
                        batteryRecycleRecord.setOperatorId(uid);
                        batteryRecycleRecord.setTenantId(tenantId);
                        batteryRecycleRecord.setCreateTime(System.currentTimeMillis());
                        batteryRecycleRecord.setUpdateTime(System.currentTimeMillis());
                        return batteryRecycleRecord;
                    }).collect(Collectors.toList());
                    
                    batteryRecycleRecordMapper.batchInsert(batteryRecycleRecords);
                });
            } catch (Exception e) {
                log.error("recycle battery check Battery batch insert error", e);
            }
            
        });
        
        BatteryRecycleSaveResultVO recycleSaveResultVO = BatteryRecycleSaveResultVO.builder().successCount(existsBatterySnList.size()).failCount(notExistsBatterySnList.size())
                .notExistBatterySnList(notExistsBatterySnList).build();
        
        return Triple.of(true, "", recycleSaveResultVO);
    }
    
    @Override
    @Slave
    public List<BatteryRecycleVO> listByPage(BatteryRecyclePageRequest request) {
        BatteryRecycleQueryModel queryModel = new BatteryRecycleQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        
        List<BatteryRecycleRecord> batteryRecycleRecordList = batteryRecycleRecordMapper.selectListByPage(queryModel);
        if (ObjectUtils.isEmpty(batteryRecycleRecordList)) {
            return Collections.emptyList();
        }
    
        return batteryRecycleRecordList.parallelStream().map(batteryRecycleRecord -> {
            BatteryRecycleVO batteryRecycleRecordVO = new BatteryRecycleVO();
            BeanUtils.copyProperties(batteryRecycleRecord, batteryRecycleRecordVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(batteryRecycleRecord.getFranchiseeId());
            if (Objects.nonNull(franchisee)) {
                batteryRecycleRecordVO.setFranchiseeName(franchisee.getName());
            }
    
            ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(batteryRecycleRecord.getElectricityCabinetId());
            if (Objects.nonNull(cabinet)) {
                batteryRecycleRecordVO.setElectricityCabinetName(cabinet.getName());
            }
    
            User user = userService.queryByUidFromCache(batteryRecycleRecord.getOperatorId());
            if (Objects.nonNull(user)) {
                batteryRecycleRecordVO.setOperatorName(user.getName());
            }
            
            return batteryRecycleRecordVO;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Slave
    public Integer countTotal(BatteryRecyclePageRequest request) {
        BatteryRecycleQueryModel queryModel = new BatteryRecycleQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        return batteryRecycleRecordMapper.countTotal(queryModel);
    }
    
    @Override
    @Slave
    public BatteryRecycleRecord listFirstNotLockedRecord(Integer tenantId) {
        return batteryRecycleRecordMapper.selectListFirstNotLockedRecord(tenantId);
    }
    
    @Override
    @Slave
    public List<BatteryRecycleRecord> listNotLockedRecord(Integer tenantId, Long maxId, Long size) {
        return batteryRecycleRecordMapper.selectListNotLockedRecord(tenantId, maxId, size);
    }
    
    @Override
    public Integer updateById(BatteryRecycleRecord batteryRecycleRecord) {
        return batteryRecycleRecordMapper.updateById(batteryRecycleRecord);
    }

    /**
     * 取消回收电池
     * @param request
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> cancel(BatteryRecycleCancelRequest request, List<BatteryRecycleRecord> batteryRecycleRecords) {
        BatteryRecycleCancelResultVO batteryRecycleCancelResultVO = new BatteryRecycleCancelResultVO();

        // 根据电池编号查询是否存在已录入的电池记录
        if (ObjectUtils.isEmpty(batteryRecycleRecords)) {
            batteryRecycleCancelResultVO.setFailBatterySnList(request.getBatterySnList());
            batteryRecycleCancelResultVO.setFailCount(request.getBatterySnList().size());
            batteryRecycleCancelResultVO.setSuccessCount(NumberConstant.ZERO);

            log.info("battery recycle cancel info! battery is not exists batter req:{}", request);
            return Triple.of(true, "", batteryRecycleCancelResultVO);
        }

        Set<String> existsSnList = new HashSet<>();
        List<Long> idList = new ArrayList<>();
        batteryRecycleRecords.stream().forEach(item -> {
            existsSnList.add(item.getSn());
            idList.add(item.getId());
        });

        Set<String> notExistsSnList = new HashSet<>();
        // 判断数量是否一致
        if (existsSnList.size() != request.getBatterySnList().size()) {
            // 对比出不同的电池sn
            notExistsSnList = request.getBatterySnList().stream().filter(sn -> !existsSnList.contains(sn)).collect(Collectors.toSet());
        }

        // 修改记录为取消
        batteryRecycleRecordMapper.batchUpdateStatusByIdList(idList, BatteryRecycleStatusEnum.CANCEL.getCode(), System.currentTimeMillis());

        batteryRecycleCancelResultVO.setFailCount(notExistsSnList.size());
        batteryRecycleCancelResultVO.setFailBatterySnList(notExistsSnList);
        batteryRecycleCancelResultVO.setSuccessCount(existsSnList.size());

        return Triple.of(true, "", batteryRecycleCancelResultVO);
    }

    @Slave
    @Override
    public List<BatteryRecycleRecord> listBySnList(BatteryRecycleCancelRequest request) {
        return batteryRecycleRecordMapper.listBySnList(request.getBatterySnList(), request.getTenantId(),
                request.getFranchiseeIdList(), BatteryRecycleStatusEnum.INIT.getCode());
    }

    private List<ElectricityBattery> checkBatterySnList(List<String> batterySnList, Integer tenantId, List<Long> bindFranchiseeIdList) {
        if (batterySnList.size() > 2000) {
            List<List<String>> partition = ListUtils.partition(batterySnList, 2000);
            List<ElectricityBattery> existsBatterySnList = Collections.synchronizedList(new ArrayList<>());
            
            List<CompletableFuture<List<ElectricityBattery>>> collect = partition.stream().map(item -> {
                CompletableFuture<List<ElectricityBattery>> exceptionally = CompletableFuture.supplyAsync(() -> {
                    List<ElectricityBattery> existSnBySnList = electricityBatteryService.listBySnList(item, tenantId, bindFranchiseeIdList);
                    return existSnBySnList;
                }, threadPool).whenComplete((result, throwable) -> {
                    if (result != null && ObjectUtils.isNotEmpty(result)) {
                        existsBatterySnList.addAll(result);
                    }
                    
                    if (throwable != null) {
                        log.error("recycle battery check Battery Sn error", throwable);
                    }
                    
                });
                return exceptionally;
            }).collect(Collectors.toList());
            
            CompletableFuture<Void> resultFuture = CompletableFuture.allOf(collect.toArray(new CompletableFuture[collect.size()]));
            
            try {
                resultFuture.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Data summary browsing error for battery recycle check", e);
            }
            
            return existsBatterySnList;
        }
        
        return electricityBatteryService.listBySnList(batterySnList, tenantId, bindFranchiseeIdList);
    }
}
