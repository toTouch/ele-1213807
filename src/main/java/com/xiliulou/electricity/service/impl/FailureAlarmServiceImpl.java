package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.entity.FailureAlarmProtectMeasure;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmDeviceTypeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmGradeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmStatusEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmTenantVisibleEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmTypeEnum;
import com.xiliulou.electricity.enums.failureAlarm.ProtectMeasureEnum;
import com.xiliulou.electricity.mapper.FailureAlarmMapper;
import com.xiliulou.electricity.mapper.FailureAlarmProtectMeasureMapper;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureAlarmQueryModel;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmBatchSetRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmSaveRequest;
import com.xiliulou.electricity.service.FailureAlarmProtectMeasureService;
import com.xiliulou.electricity.service.FailureAlarmService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ChannelActivityHistoryExcelVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureAlarmExcelVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureAlarmVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 故障预警设置
 *
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 */
@Service
@Slf4j
public class FailureAlarmServiceImpl implements FailureAlarmService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private FailureAlarmMapper failureAlarmMapper;
    
    @Resource
    private FailureAlarmProtectMeasureMapper failureAlarmProtectMeasureMapper;
    
    @Resource
    private FailureAlarmProtectMeasureService measureService;
    
    /**
     * 故障告警设置保存
     *
     * @param failureAlarmSaveRequest
     * @param uid
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(FailureAlarmSaveRequest failureAlarmSaveRequest, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_FAILURE_ALARM_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        if (ObjectUtils.isEmpty(failureAlarmSaveRequest.getProtectMeasureList())) {
            return Triple.of(false, "300823", "保护措施不能为空");
        }
        
        // 检测错误码是否存在
        int errorCodeCount = this.checkErrorCode(failureAlarmSaveRequest.getSignalId(), null);
        if (errorCodeCount > 0) {
            return Triple.of(false, "300819", "信号量ID重复，请检查后操作");
        }
        
        // 检测保护措施是否存在
        List<Integer> noExistsList = checkProtectMeasureExists(failureAlarmSaveRequest.getProtectMeasureList());
        if (ObjectUtils.isNotEmpty(noExistsList)) {
            return Triple.of(false, "300821", "保护措施不存在");
        }
    
        // 故障告警设置保存
        FailureAlarm failureAlarm = new FailureAlarm();
        BeanUtils.copyProperties(failureAlarmSaveRequest, failureAlarm);
        failureAlarm.setCreateTime(System.currentTimeMillis());
        failureAlarm.setUpdateTime(System.currentTimeMillis());
        failureAlarm.setDelFlag(FailureAlarm.DEL_NORMAL);
        
        failureAlarmMapper.insertOne(failureAlarm);
        
        // 故障告警保护措施保存
        List<FailureAlarmProtectMeasure> protectMeasures = new ArrayList<>();
        List<Integer> protectMeasureList = failureAlarmSaveRequest.getProtectMeasureList();
        protectMeasureList.forEach(item -> {
            FailureAlarmProtectMeasure failureAlarmProtectMeasure = new FailureAlarmProtectMeasure();
            failureAlarmProtectMeasure.setFailureAlarmId(failureAlarm.getId());
            failureAlarmProtectMeasure.setProtectMeasure(item);
            protectMeasures.add(failureAlarmProtectMeasure);
        });
        
        failureAlarmProtectMeasureMapper.batchInsert(protectMeasures);
        
        return Triple.of(true, "", failureAlarm);
    }
    
    private List<Integer> checkProtectMeasureExists(List<Integer> protectMeasureList) {
         List<Integer> list = protectMeasureList.stream().filter(code -> !BasicEnum.isExist(code, ProtectMeasureEnum.class)).collect(Collectors.toList());
         return list;
    }
    
    @Slave
    @Override
    public Integer countTotal(FailureAlarmPageRequest allocateRecordPageRequest) {
        FailureAlarmQueryModel failureAlarmQueryModel = new FailureAlarmQueryModel();
        BeanUtils.copyProperties(allocateRecordPageRequest, failureAlarmQueryModel);
        
        return failureAlarmMapper.countTotal(failureAlarmQueryModel);
    }
    
    @Slave
    @Override
    public List<FailureAlarmVO> listByPage(FailureAlarmPageRequest allocateRecordPageRequest) {
        List<FailureAlarmVO> rspList = new ArrayList<>();
        
        FailureAlarmQueryModel failureAlarmQueryModel = new FailureAlarmQueryModel();
        BeanUtils.copyProperties(allocateRecordPageRequest, failureAlarmQueryModel);
        
        List<FailureAlarm> failureAlarmVOList = this.failureAlarmMapper.selectListByPage(failureAlarmQueryModel);
        if (CollectionUtils.isNotEmpty(failureAlarmVOList)) {
            List<Long> failureAlarmIdList = failureAlarmVOList.stream().map(FailureAlarm::getId).collect(Collectors.toList());
            // 查询保护措施
            List<FailureAlarmProtectMeasure> allocateDetailVOList = measureService.listByFailureAlarmIdList(failureAlarmIdList);
            Map<Long, List<Integer>> protectMeasureMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(allocateDetailVOList)) {
                protectMeasureMap = allocateDetailVOList.stream().collect(
                        Collectors.groupingBy(FailureAlarmProtectMeasure::getFailureAlarmId, Collectors.collectingAndThen(Collectors.toList(), e -> this.getProtectMeasure(e))));
            }
            
            for (FailureAlarm failureAlarm : failureAlarmVOList) {
                FailureAlarmVO failureAlarmVO = new FailureAlarmVO();
                BeanUtil.copyProperties(failureAlarm, failureAlarmVO);
                
                // 设置保护措施
                if (ObjectUtils.isNotEmpty(protectMeasureMap.get(failureAlarm.getId()))) {
                    List<Integer> protectList = protectMeasureMap.get(failureAlarm.getId());
                    String protectMeasure = ProtectMeasureEnum.getDescByCodeList(protectList);
                    failureAlarmVO.setProtectMeasure(protectMeasure);
                }
                
                rspList.add(failureAlarmVO);
            }
            
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    private List<Integer> getProtectMeasure(List<FailureAlarmProtectMeasure> list) {
        return list.stream().map(FailureAlarmProtectMeasure::getProtectMeasure).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple update(FailureAlarmSaveRequest failureAlarmSaveRequest, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_FAILURE_ALARM_UPDATE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
    
        if (ObjectUtils.isEmpty(failureAlarmSaveRequest.getProtectMeasureList())) {
            return Triple.of(false, "300823", "保护措施不能为空");
        }
    
        // 检测故障是否存在
        FailureAlarm failureAlarmOld = this.queryById(failureAlarmSaveRequest.getId());
        if (ObjectUtils.isEmpty(failureAlarmOld)) {
            return Triple.of(false, "300820", "故障告警不存在");
        }
    
        // 检测错误码是否存在
        int errorCodeCount = this.checkErrorCode(failureAlarmSaveRequest.getSignalId(), failureAlarmSaveRequest.getId());
        if (errorCodeCount > 0) {
            return Triple.of(false, "300819", "信号量ID重复，请检查后操作");
        }
    
        // 检测保护措施是否存在
        List<Integer> noExistsList = checkProtectMeasureExists(failureAlarmSaveRequest.getProtectMeasureList());
        if (ObjectUtils.isNotEmpty(noExistsList)) {
            return Triple.of(false, "300821", "保护措施不存在");
        }
        
        // 故障告警设置保存
        FailureAlarm failureAlarm = new FailureAlarm();
        BeanUtils.copyProperties(failureAlarmSaveRequest, failureAlarm);
        failureAlarm.setUpdateTime(System.currentTimeMillis());
        
        failureAlarmMapper.update(failureAlarm);
        
        // 删除保护措施
        failureAlarmMapper.batchDeleteByFailureAlarmId(failureAlarm.getId());
        
        // 故障告警保护措施保存
        List<FailureAlarmProtectMeasure> protectMeasures = new ArrayList<>();
        List<Integer> protectMeasureList = failureAlarmSaveRequest.getProtectMeasureList();
        protectMeasureList.forEach(item -> {
            FailureAlarmProtectMeasure failureAlarmProtectMeasure = new FailureAlarmProtectMeasure();
            failureAlarmProtectMeasure.setFailureAlarmId(failureAlarm.getId());
            failureAlarmProtectMeasure.setProtectMeasure(item);
            protectMeasures.add(failureAlarmProtectMeasure);
        });
        
        failureAlarmProtectMeasureMapper.batchInsert(protectMeasures);
        
        return Triple.of(true, "", failureAlarm);
    }
    
    @Slave
    public int checkErrorCode(String signalId, Long id) {
        return failureAlarmMapper.checkErrorCode(signalId, id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple delete(Long id, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_FAILURE_ALARM_DELETE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        // 检测故障是否存在
        FailureAlarm failureAlarm = this.queryById(id);
        if (ObjectUtils.isEmpty(failureAlarm)) {
            return Triple.of(false, "300820", "故障告警不存在");
        }
        
        failureAlarmProtectMeasureMapper.deleteByFailureAlarmId(id);
        
        // 删除故障
        int res = failureAlarmMapper.remove(id, System.currentTimeMillis(), FailureAlarm.DEL_DEL);
        
        return Triple.of(true, "", failureAlarm);
    }
    
    @Slave
    public FailureAlarm queryById(Long id) {
        return failureAlarmMapper.select(id);
    }
    
    @Override
    public R batchSet(FailureAlarmBatchSetRequest request, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_FAILURE_ALARM_BATCH_SET_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        if (ObjectUtils.isEmpty(request.getIdList())) {
            return R.fail("300822", "主键id不能为空");
        }
        // 判断id是否存在
        List<FailureAlarm> list = this.listByIdList(request.getIdList());
        if (ObjectUtils.isEmpty(list)) {
            return R.fail("300820", "故障告警不存在");
        }
        
        // 判断是否存在故障数据
        List<FailureAlarm> failureList = list.stream().filter(item -> Objects.equals(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getCode(), item.getType()))
                .collect(Collectors.toList());
        
        if (ObjectUtils.isNotEmpty(failureList)) {
            return R.fail("300820", "只可选择告警类型数据，请重新选择后操作");
        }
    
        List<Long> idList = list.stream().map(FailureAlarm::getId).collect(Collectors.toList());
        Set<Long> noExistIds = request.getIdList().stream().filter(item -> !idList.contains(item)).collect(Collectors.toSet());
        if (ObjectUtils.isNotEmpty(noExistIds)) {
            log.error("batch set not find failure alarm ,ids={}", noExistIds);
            return R.fail("300820", "故障告警不存在");
        }
        
        // 批量修改运营商的可见状态
        int res = failureAlarmMapper.batchUpdateTenantVisible(request.getIdList(), request.getTenantVisible(), System.currentTimeMillis());
        
        // 删除缓存
        DbUtils.dbOperateSuccessThenHandleCache(res, i -> {
            list.forEach(failureAlarm -> {
                this.deleteCache(failureAlarm);
            });
        });
        
        return R.ok();
    }
    
    @Slave
    public List<FailureAlarm> listByIdList(List<Long> idList) {
        return this.failureAlarmMapper.selectList(idList);
    }
    
    @Override
    public void exportExcel(FailureAlarmPageRequest failureAlarmPageRequest, HttpServletResponse response) {
        Long userId = SecurityUtils.getUid();
        if (Objects.isNull(userId)) {
            throw new CustomBusinessException("未查询到用户");
        }
        
        Long offset = 0L;
        Long size = 2000L;
        failureAlarmPageRequest.setOffset(offset);
        failureAlarmPageRequest.setSize(size);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
    
        List<FailureAlarmVO> failureAlarmVOList = this.listByPage(failureAlarmPageRequest);
        List<FailureAlarmExcelVo> voList = new ArrayList<>();
        
        if (ObjectUtils.isNotEmpty(failureAlarmVOList)) {
            // 查询保护措施
            Map<Long, List<Integer>> protectMeasureMap = new HashMap<>();
            List<Long> failureAlarmIdList = failureAlarmVOList.stream().map(FailureAlarmVO::getId).collect(Collectors.toList());
            List<List<Long>> failureAlarmIdPartList = ListUtils.partition(failureAlarmIdList, 500);
            
            failureAlarmIdPartList.forEach(item -> {
                List<FailureAlarmProtectMeasure> allocateDetailVOList = measureService.listByFailureAlarmIdList(item);
                if (CollectionUtils.isNotEmpty(allocateDetailVOList)) {
                     Map<Long, List<Integer>> map = allocateDetailVOList.stream().collect(Collectors.groupingBy(FailureAlarmProtectMeasure::getFailureAlarmId,
                            Collectors.collectingAndThen(Collectors.toList(), e -> this.getProtectMeasure(e))));
                    protectMeasureMap.putAll(map);
                }
            });
            
            for (FailureAlarmVO item : failureAlarmVOList) {
                FailureAlarmExcelVo vo = new FailureAlarmExcelVo();
                BeanUtils.copyProperties(item, vo);
                FailureAlarmTypeEnum typeEnum = BasicEnum.getEnum(item.getType(), FailureAlarmTypeEnum.class);
                if (ObjectUtils.isNotEmpty(typeEnum)) {
                    vo.setType(typeEnum.getDesc());
                }
    
                FailureAlarmGradeEnum gradeEnum = BasicEnum.getEnum(item.getType(), FailureAlarmGradeEnum.class);
                if (ObjectUtils.isNotEmpty(gradeEnum)) {
                    vo.setGrade(gradeEnum.getDesc());
                }
    
                FailureAlarmDeviceTypeEnum deviceTypeEnum = BasicEnum.getEnum(item.getType(), FailureAlarmDeviceTypeEnum.class);
                if (ObjectUtils.isNotEmpty(deviceTypeEnum)) {
                    vo.setDeviceType(deviceTypeEnum.getDesc());
                }
    
                FailureAlarmStatusEnum statusEnum = BasicEnum.getEnum(item.getType(), FailureAlarmStatusEnum.class);
                if (ObjectUtils.isNotEmpty(statusEnum)) {
                    vo.setStatus(statusEnum.getDesc());
                }
    
                FailureAlarmTenantVisibleEnum tenantVisibleEnum = BasicEnum.getEnum(item.getType(), FailureAlarmTenantVisibleEnum.class);
                if (ObjectUtils.isNotEmpty(tenantVisibleEnum)) {
                    vo.setTenantVisible(tenantVisibleEnum.getDesc());
                }
    
                // 设置保护措施
                if (ObjectUtils.isNotEmpty(protectMeasureMap.get(item.getId()))) {
                    List<Integer> protectList = protectMeasureMap.get(item.getId());
                    String protectMeasure = ProtectMeasureEnum.getDescByCodeList(protectList);
                    item.setProtectMeasure(protectMeasure);
                }
    
                date.setTime(item.getCreateTime());
                vo.setCreateTime(sdf.format(date));
                voList.add(vo);
            }
        }
        
        String fileName = "故障告警设置报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, FailureAlarmExcelVo.class).sheet("sheet").doWrite(voList);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }
    
    @Override
    public void refreshCache(FailureAlarm failureAlarm) {
        redisService.saveWithHash(CacheConstant.CACHE_FAILURE_ALARM + failureAlarm.getSignalId(), failureAlarm);
    }
    
    @Override
    public void deleteCache(FailureAlarm failureAlarm) {
        redisService.delete(CacheConstant.CACHE_FAILURE_ALARM + failureAlarm.getSignalId());
    }
}
