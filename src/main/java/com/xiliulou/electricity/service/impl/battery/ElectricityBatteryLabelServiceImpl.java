package com.xiliulou.electricity.service.impl.battery;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.battery.BatteryLabelConstant;
import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDTO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.mapper.battery.ElectricityBatteryLabelMapper;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.battery.BatteryLabelRecordService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelBizService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.battery.ElectricityBatteryLabelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author SJP
 * @date 2025-02-14 15:43
 **/
@Slf4j
@Service
public class ElectricityBatteryLabelServiceImpl implements ElectricityBatteryLabelService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private ElectricityBatteryLabelMapper electricityBatteryLabelMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private BatteryLabelRecordService batteryLabelRecordService;
    
    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Resource
    private ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private ElectricityBatteryLabelBizService electricityBatteryLabelBizService;
    
    private final ExecutorService dealBatteryLabelWhenSendCommandExecutor = XllThreadPoolExecutors.newFixedThreadPool("dealBatteryLabelWhenSendCommand", 3, "DEAL_BATTERY_LABEL_WHEN_SEND_COMMAND_THREAD");
    
    
    @Override
    public void insert(ElectricityBatteryLabel batteryLabel) {
        try {
            electricityBatteryLabelMapper.insert(batteryLabel);
        } catch (Exception e) {
            log.warn("INSERT BATTERY LABEL WARN!", e);
        }
    }
    
    @Override
    public void batchInsert(List<ElectricityBattery> batteries, Long operatorUid) {
        if (CollectionUtils.isEmpty(batteries)) {
            return;
        }
        
        List<ElectricityBatteryLabel> batteryLabels = new ArrayList<>();
        Long now = System.currentTimeMillis();
        for (ElectricityBattery battery : batteries) {
            batteryLabels.add(
                    ElectricityBatteryLabel.builder().sn(battery.getSn()).tenantId(battery.getTenantId()).franchiseeId(battery.getFranchiseeId()).createTime(now).updateTime(now)
                            .build());
            
            Integer newLabel = battery.getLabel();
            battery.setLabel(null);
            electricityBatteryLabelBizService.sendRecordAndGeneralHandling(battery, operatorUid, newLabel, null, now, null, null);
        }
        
        try {
            electricityBatteryLabelMapper.batchInsert(batteryLabels);
        } catch (Exception e) {
            log.warn("BATCH INSERT BATTERY LABEL WARN!", e);
        }
    }
    
    @Slave
    @Override
    public ElectricityBatteryLabel selectBySnAndTenantId(String sn, Integer tenantId) {
        return electricityBatteryLabelMapper.queryBySnAndTenantId(sn, tenantId);
    }
    
    @Override
    public int updateById(ElectricityBatteryLabel batteryLabel) {
        return electricityBatteryLabelMapper.updateById(batteryLabel);
    }
    
    @Override
    public void deleteBySnAndTenantId(String sn, Integer tenantId) {
        electricityBatteryLabelMapper.deleteBySnAndTenantId(sn, tenantId);
    }
    
    @Override
    public void setPreLabel(Integer eId, String cellNo, String sn, BatteryLabelModifyDTO labelModifyDto) {
        try {
            String key = String.format(CacheConstant.PRE_MODIFY_BATTERY_LABEL, eId, cellNo, sn);
            String dtoStr = redisService.get(key);
            // 缓存内没有，不用做优先级校验
            if (StringUtils.isEmpty(dtoStr) || StringUtils.isBlank(dtoStr)) {
                redisService.saveWithString(key, labelModifyDto, 30L, TimeUnit.MINUTES);
                return;
            }
            
            BatteryLabelModifyDTO oldDto = JsonUtil.fromJson(dtoStr, BatteryLabelModifyDTO.class);
            Integer oldPreLabel = oldDto.getNewLabel();
            Integer newPreLabel = labelModifyDto.getNewLabel();
            
            // 旧预修改标签是租借时，如果新预修改标签也属于租借则更新缓存，否则直接返回，租借的优先级更高
            if (Objects.nonNull(oldPreLabel) && BatteryLabelConstant.RENT_LABEL_SET.contains(oldPreLabel) && !BatteryLabelConstant.RENT_LABEL_SET.contains(newPreLabel)) {
                return;
            }
            
            // 新旧一样,判断是否刷新时间
            if (Objects.equals(oldPreLabel, newPreLabel)) {
                // 不是领用的直接刷新时间
                if (!BatteryLabelConstant.RECEIVED_LABEL_SET.contains(oldPreLabel)) {
                    redisService.expire(key, 30 * 60 * 1000L, false);
                    return;
                }
                
                // 领用类型的标签，新领用人一样时，再刷新时间
                if (Objects.equals(oldDto.getNewReceiverId(), labelModifyDto.getNewReceiverId())) {
                    redisService.expire(key, 30 * 60 * 1000L, false);
                    return;
                }
            }
            
            redisService.saveWithString(key, labelModifyDto, 30L, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("BATTERY LABEL SET PRE LABEL ERROR! sn={}", sn, e);
        }
    }
    
    @Slave
    @Override
    public List<ElectricityBatteryLabel> listBySns(List<String> sns) {
        return electricityBatteryLabelMapper.selectListBySns(sns);
    }
    
    @Override
    public int deleteReceivedData(String sn) {
        return electricityBatteryLabelMapper.updateReceivedData(sn, System.currentTimeMillis());
    }
    
    @Slave
    public List<ElectricityBatteryLabelVO> listLabelVOBySns(List<String> sns, Map<String, Integer> snAndLabel) {
        List<ElectricityBatteryLabel> batteryLabels = electricityBatteryLabelMapper.selectListBySns(sns);
        if (CollectionUtils.isEmpty(batteryLabels)) {
            return Collections.emptyList();
        }
        
        return batteryLabels.parallelStream().map(batteryLabel -> {
            String sn = batteryLabel.getSn();
            
            ElectricityBatteryLabelVO vo = new ElectricityBatteryLabelVO();
            vo.setSn(sn);
            vo.setRemark(batteryLabel.getRemark());
            vo.setReceiverId(batteryLabel.getReceiverId());
            
            if (MapUtils.isEmpty(snAndLabel) || !snAndLabel.containsKey(sn)) {
                return vo;
            }
            
            Integer label = snAndLabel.get(sn);
            if (Objects.equals(label, BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode())) {
                User user = userService.queryByUidFromCache(batteryLabel.getReceiverId());
                if (Objects.nonNull(user)) {
                    vo.setReceiverName(user.getName());
                }
            }
            
            if (Objects.equals(label, BatteryLabelEnum.RECEIVED_MERCHANT.getCode())) {
                Merchant merchant = merchantService.queryByIdFromCache(batteryLabel.getReceiverId());
                if (Objects.nonNull(merchant)) {
                    vo.setReceiverName(merchant.getName());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countReceived(Long receiverId) {
        return electricityBatteryLabelMapper.countReceived(receiverId, TenantContextHolder.getTenantId());
    }
    
    @Override
    public void updateLockSnAndBatteryLabel(EleOuterCommandQuery eleOuterCommandQuery, ElectricityCabinet electricityCabinet, Long operatorId) {
        String traceId = MDC.get(CommonConstant.TRACE_ID);
        dealBatteryLabelWhenSendCommandExecutor.execute(() -> {
            MDC.put(CommonConstant.TRACE_ID, traceId);
            
            try {
                if (Objects.isNull(eleOuterCommandQuery)) {
                    log.warn("UPDATE LOCK SN WARN! eleOuterCommandQuery is null");
                    return;
                }
                
                // 禁用启用key
                String isForbidden = "isForbidden";
                // 禁用启用仓门号key
                String cellNoKey = "cell_list";
                Map<String, Object> data = eleOuterCommandQuery.getData();
                
                if (!data.containsKey(isForbidden) || !data.containsKey(cellNoKey)) {
                    return;
                }
                
                // 获取cellNo
                String cellNo;
                Object value = data.get(cellNoKey);
                if (!(value instanceof List)) {
                    log.warn("UPDATE LOCK SN WARN! cell_list type is wrong, eleOuterCommandQuery={}", eleOuterCommandQuery);
                    return;
                }
                // 值是 List 类型，继续转换
                List<?> list = (List<?>) value;
                if (CollectionUtils.isEmpty(list) || Objects.isNull(list.get(0))) {
                    // 列表中的元素是 Integer 类型，安全转换
                    log.warn("UPDATE LOCK SN WARN! can not get cellNo, eleOuterCommandQuery={}, list={}", eleOuterCommandQuery, list);
                    return;
                }
                cellNo = list.get(0).toString();
                
                String lockSn = eleOuterCommandQuery.getLockSn();
                Integer eId = electricityCabinet.getId();
                ElectricityBattery battery = electricityBatteryService.queryBySnFromDb(lockSn, electricityCabinet.getTenantId());
                
                // 禁用格挡保存lockSn，修改电池标签
                if (data.containsKey(isForbidden) && Objects.equals(data.get(isForbidden), true) && StringUtils.isNotEmpty(lockSn) && StringUtils.isNotBlank(lockSn)) {
                    BatteryLabelModifyDTO dto = BatteryLabelModifyDTO.builder().newLabel(BatteryLabelEnum.LOCKED_IN_THE_CABIN.getCode()).operatorUid(operatorId).build();
                    boolean result = electricityBatteryService.syncModifyLabel(battery, null, dto, false);
                    if (!result){
                        return;
                    }
                    electricityCabinetBoxService.updateLockSnByEidAndCellNo(eId, cellNo, lockSn);
                }
            } catch (Exception e) {
                log.error("UPDATE LOCK SN ERROR! eleOuterCommandQuery={}", eleOuterCommandQuery, e);
            } finally {
                MDC.clear();
            }
        });
    }
    
    @Override
    public void updateOpenCellAndBatteryLabel(EleOuterCommandQuery eleOuterCommandQuery, ElectricityCabinet electricityCabinet, Long operatorId, List<ElectricityCabinetBox> electricityCabinetBoxList) {
        String traceId = MDC.get(CommonConstant.TRACE_ID);
        dealBatteryLabelWhenSendCommandExecutor.execute(() -> {
            MDC.put(CommonConstant.TRACE_ID, traceId);
            
            try {
                if (CollectionUtils.isNotEmpty(electricityCabinetBoxList)) {
                    // 开全部仓门的，数据都有了，直接保存预修改标签即可
                    for (ElectricityCabinetBox box : electricityCabinetBoxList) {
                        if (Objects.isNull(box.getSn())) {
                            continue;
                        }
                        
                        BatteryLabelModifyDTO modifyDTO = new BatteryLabelModifyDTO(BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode(), operatorId, operatorId);
                        setPreLabel(electricityCabinet.getId(), box.getCellNo(), box.getSn(), modifyDTO);
                    }
                } else {
                    // 指定仓门开启的，需要查询出sn
                    Map<String, Object> data = eleOuterCommandQuery.getData();
                    String cellNoKey = "cell_list";
                    
                    // 获取cellNo
                    String cellNo;
                    Object value = data.get(cellNoKey);
                    if (!(value instanceof List)) {
                        log.warn("UPDATE OPEN CELL WARN! cell_list type is wrong, eleOuterCommandQuery={}", eleOuterCommandQuery);
                        return;
                    }
                    // 值是 List 类型，继续转换
                    List<?> list = (List<?>) value;
                    if (CollectionUtils.isEmpty(list)) {
                        log.warn("UPDATE OPEN CELL WARN! can not get cellNo, eleOuterCommandQuery={}, list={}", eleOuterCommandQuery, list);
                        return;
                    }
                    
                    for (Object o : list) {
                        cellNo = o.toString();
                        ElectricityCabinetBox box = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), cellNo);
                        if (Objects.isNull(box) || Objects.isNull(box.getSn())) {
                            continue;
                        }
                        
                        BatteryLabelModifyDTO modifyDTO = new BatteryLabelModifyDTO(BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode(), operatorId, operatorId);
                        setPreLabel(electricityCabinet.getId(), box.getCellNo(), box.getSn(), modifyDTO);
                    }
                }
            } catch (Exception e) {
                log.error("UPDATE OPEN CELL ERROR! eleOuterCommandQuery={}", eleOuterCommandQuery, e);
            } finally {
                MDC.clear();
            }
        });
    }
}
