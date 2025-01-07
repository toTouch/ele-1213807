package com.xiliulou.electricity.service.impl.warn;

import cn.hutool.core.date.DateUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.entity.warn.EleHardwareWarnMsg;
import com.xiliulou.electricity.entity.warn.EleWarnHandleRecord;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.failureAlarm.WarnHandleStatusEnum;
import com.xiliulou.electricity.mapper.warn.EleHardwareWarnMsgMapper;
import com.xiliulou.electricity.request.failureAlarm.WarnHandleRequest;
import com.xiliulou.electricity.service.warn.EleHardwareWarnMsgBusinessService;
import com.xiliulou.electricity.service.warn.EleHardwareWarnMsgService;
import com.xiliulou.electricity.service.warn.EleWarnHandleRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/11/7 14:59
 * @desc
 */
@Service
@Slf4j
public class EleHardwareWarnMsgBusinessServiceImpl implements EleHardwareWarnMsgBusinessService {
    
    @Resource
    private EleHardwareWarnMsgService eleHardwareWarnMsgService;
    
    @Resource
    private EleWarnHandleRecordService eleWarnHandleRecordService;
    
    @Resource
    private EleHardwareWarnMsgMapper eleHardwareWarnMsgMapper;
    
    @Resource
    private RedisService redisService;
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("WARN-HANDLE-THREAD-POOL", 6, "warnHandleThread:");
    
    
    @Override
    public Triple<Boolean, String, Object> handle(WarnHandleRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (!redisService.setNx(CacheConstant.WARN_HANDLE_LOCK_KEY + SecurityUtils.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "000000", "操作频繁，请稍后再试！");
        }
        
        // 检测状态是否合法
        if (!BasicEnum.isExist(request.getHandleStatus(), WarnHandleStatusEnum.class)) {
            return Triple.of(false, "300833", "请选择正确的处理方式");
        }
        
        // 检测状态是否合法
        if (!BasicEnum.isExist(request.getHandleType(), YesNoEnum.class)) {
            return Triple.of(false, "300834", "请选择正确的是否同时处理相同告警");
        }
        
        Long uid = SecurityUtils.getUid();
        String batchNo = DateUtil.format(new Date(), DateFormatConstant.YEAR_TIME_FORMAT);
        
        // 单独处理告警
        List<EleWarnHandleRecord> warnHandleRecordList = request.getWarnIdList().stream().map(warnId -> {
            EleWarnHandleRecord eleWarnHandleRecord = new EleWarnHandleRecord();
            eleWarnHandleRecord.setWarnId(warnId);
            eleWarnHandleRecord.setBatchNo(batchNo);
            eleWarnHandleRecord.setStatus(request.getHandleStatus());
            eleWarnHandleRecord.setRemark(request.getRemark());
            eleWarnHandleRecord.setTenantId(tenantId);
            eleWarnHandleRecord.setCreateTime(System.currentTimeMillis());
            eleWarnHandleRecord.setUpdateTime(System.currentTimeMillis());
            return eleWarnHandleRecord;
        }).collect(Collectors.toList());
        
        // 检测告警是否存在
        Boolean exists = eleHardwareWarnMsgService.existsByIdList(request.getWarnIdList());
        if (!exists) {
            return Triple.of(false, "300835", "告警数据不存在");
        }
        
        // 修改处理状态，批次号
        eleHardwareWarnMsgService.batchUpdateHandleStatus(request.getWarnIdList(), request.getHandleStatus(), batchNo);
        
        // 保存处理记录
        eleWarnHandleRecordService.batchInsert(warnHandleRecordList);
        
        // 不处理相同告警则直接返回batchNo进行后续的下载处理
        if (Objects.equals(request.getHandleType(), YesNoEnum.NO.getCode())) {
            // 处理完成之后将批次号写入redis
            return Triple.of(true, "", batchNo);
        }
        
        // 同时处理相同告警
        // 查询符合当前处理条件告警signalId
        List<String> signalIdList = eleHardwareWarnMsgService.listSignalIdByIdList(request.getWarnIdList(), request.getHandleStatus());
        if (ObjectUtils.isEmpty(signalIdList)) {
            redisService.set(String.format(CacheConstant.WARN_HANDLE_RESULT, uid, batchNo), batchNo, 60L, TimeUnit.SECONDS);
            return Triple.of(true, "", batchNo);
        }
    
        threadPool.execute(() -> {
            Long maxId = 0L;
            Long size = 2000L;
        
            while (true) {
                // 只处理 ：处理中，未处理的数据
                List<EleHardwareWarnMsg> warnMsgList = eleHardwareWarnMsgService.listBySignalIdList(signalIdList, maxId, tenantId, size);
                if (ObjectUtils.isEmpty(warnMsgList)) {
                    break;
                }
            
                maxId = warnMsgList.get(warnMsgList.size() - 1).getId();
                // 分批处理
                List<List<EleHardwareWarnMsg>> partition = ListUtils.partition(warnMsgList, 500);
            
                List<CompletableFuture<Void>> collect = partition.stream().map(item -> {
                    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                        List<Long> warnIdList = new ArrayList<>();
                        List<EleWarnHandleRecord> warnHandleRecordListTemp = new ArrayList<>();
    
                        item.stream().filter(warnMsg -> !request.getWarnIdList().contains(warnMsg.getId())).forEach(warnMsg -> {
                            EleWarnHandleRecord eleWarnHandleRecord = new EleWarnHandleRecord();
                            eleWarnHandleRecord.setWarnId(warnMsg.getId());
                            eleWarnHandleRecord.setBatchNo(batchNo);
                            eleWarnHandleRecord.setStatus(request.getHandleStatus());
                            eleWarnHandleRecord.setRemark(request.getRemark());
                            eleWarnHandleRecord.setTenantId(tenantId);
                            eleWarnHandleRecord.setCreateTime(System.currentTimeMillis());
                            eleWarnHandleRecord.setUpdateTime(System.currentTimeMillis());
                            warnHandleRecordListTemp.add(eleWarnHandleRecord);
                        
                            warnIdList.add(warnMsg.getId());
                        });
                    
                        // 修改处理状态，批次号
                        eleHardwareWarnMsgService.batchUpdateHandleStatus(warnIdList, request.getHandleStatus(), batchNo);
                    
                        // 保存处理记录
                        eleWarnHandleRecordService.batchInsert(warnHandleRecordListTemp);
                    }, threadPool).exceptionally(e -> {
                        log.error("WARN HANDLE ERROR! query weekMemberCard Order Count error!", e);
                        return null;
                    });
                    
                    return completableFuture;
                }).collect(Collectors.toList());
            
                CompletableFuture<Void> resultFuture = CompletableFuture.allOf(collect.toArray(new CompletableFuture[collect.size()]));
            
                try {
                    resultFuture.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("Data summary browsing error for WARN HANDLE", e);
                }
            }
        
            // 处理完成之后将批次号写入redis
            redisService.set(String.format(CacheConstant.WARN_HANDLE_RESULT, uid, batchNo), batchNo, 60L, TimeUnit.SECONDS);
        });
        
        return Triple.of(true, "", batchNo);
    }
}
