package com.xiliulou.electricity.service.impl.battery;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.battery.BatteryLabelConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.request.battery.BatteryLabelBatchUpdateRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelBizService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.battery.BatteryLabelBatchUpdateVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author SJP
 * @date 2025-02-20 17:54
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ElectricityBatteryLabelBizServiceImpl implements ElectricityBatteryLabelBizService {
    
    private final ElectricityBatteryLabelService electricityBatteryLabelService;
    
    private final ElectricityBatteryService electricityBatteryService;
    
    private final UserDataScopeService userDataScopeService;
    
    private final StoreService service;
    
    private final InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
    
    @Override
    public R updateRemark(String sn, String remark) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        ElectricityBatteryLabel batteryLabel = electricityBatteryLabelService.queryBySnAndTenantId(sn, tenantId);
        Long now = System.currentTimeMillis();
        
        if (Objects.isNull(batteryLabel)) {
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(sn, tenantId);
            if (Objects.isNull(electricityBattery)) {
                log.warn("UPDATE REMARK WARN! electricityBattery is null, sn={}, tenantId={}", sn, tenantId);
                return R.fail("ELECTRICITY.0020", "未找到电池");
            }
            
            ElectricityBatteryLabel newBatteryLabel = ElectricityBatteryLabel.builder().sn(sn).remark(remark).tenantId(tenantId).franchiseeId(electricityBattery.getFranchiseeId())
                    .createTime(now).updateTime(now).build();
            electricityBatteryLabelService.insert(newBatteryLabel);
        } else {
            ElectricityBatteryLabel batteryLabelUpdate = new ElectricityBatteryLabel();
            batteryLabelUpdate.setId(batteryLabel.getId());
            batteryLabelUpdate.setRemark(remark);
            batteryLabelUpdate.setUpdateTime(now);
            electricityBatteryLabelService.updateById(batteryLabelUpdate);
        }
        
        return R.ok();
    }
    
    @Override
    public R batchUpdate(BatteryLabelBatchUpdateRequest request) {
        try {
            TokenUser user = SecurityUtils.getUserInfo();
            if (Objects.isNull(user)) {
                log.error("ELECTRICITY  ERROR! not found user ");
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            
            if (Objects.isNull(request)) {
                log.warn("BATCH UPDATE LABEL WARN! request is null");
                return R.fail("300150", "数据不合规，请联系管理员");
            }
            
            List<String> snList = request.getSnList();
            if (CollectionUtils.isEmpty(snList)) {
                log.warn("BATCH UPDATE LABEL WARN! snList is empty, request={}", request);
                return R.fail("300151", "电池sn不可为空");
            }
            
            Integer newLabel = request.getLabel();
            if (Objects.equals(newLabel, BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode()) && Objects.isNull(request.getAdministratorId())) {
                log.warn("BATCH UPDATE LABEL WARN! administratorId is empty, request={}", request);
                return R.fail("300152", "领用管理员不可为空");
            }
            
            if (Objects.equals(newLabel, BatteryLabelEnum.RECEIVED_MERCHANT.getCode()) && Objects.isNull(request.getMerchantId())) {
                log.warn("BATCH UPDATE LABEL WARN! merchantId is empty, request={}", request);
                return R.fail("300153", "领用商户不可为空");
            }
            
            // 准备电池数据用于校验
            List<ElectricityBattery> electricityBatteries = electricityBatteryService.listBatteryBySnList(snList);
            Map<String, ElectricityBattery> batteryMap = electricityBatteries.stream()
                    .collect(Collectors.toMap(ElectricityBattery::getSn, Function.identity(), (item1, item2) -> item2));
            // 准备标签数据用于校验
            List<ElectricityBatteryLabel> batteryLabels = electricityBatteryLabelService.listBySns(snList);
            Map<String, ElectricityBatteryLabel> labelMap = batteryLabels.stream()
                    .collect(Collectors.toMap(ElectricityBatteryLabel::getSn, Function.identity(), (item1, item2) -> item2));
            
            // 收集要修改的电池
            List<ElectricityBattery> batteriesNeedUpdate = new ArrayList<>();
            
            List<Map<String, String>> failReasons = new ArrayList<>();
            int failureCount = 0;
            for (String sn : snList) {
                // 先校验组装失败原因
                Map<String, String> failReason = new HashMap<>();
                failReason.put("sn", sn);
                
                if (MapUtils.isEmpty(batteryMap) || !batteryMap.containsKey(sn)) {
                    failReason.put("reason", "电池不存在");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                ElectricityBattery electricityBattery = batteryMap.get(sn);
                if (Objects.equals(electricityBattery.getLabel(), newLabel)) {
                    failReason.put("reason", "新旧标签相同");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                if (Objects.equals(electricityBattery.getLabel(), BatteryLabelEnum.IN_THE_CABIN.getCode())) {
                    failReason.put("reason", "不支持此操作，当前标签为在仓");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                if (BatteryLabelConstant.RENT_LABEL_SET.contains(electricityBattery.getLabel())) {
                    failReason.put("reason", "不支持此操作，当前标签为租借");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                if (Objects.equals(electricityBattery.getLabel(), BatteryLabelEnum.LOCKED_IN_THE_CABIN.getCode())) {
                    failReason.put("reason", "不支持此操作，当前标签为锁定在仓");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                if (permissionVerification(electricityBattery, user, newLabel)) {
                    failReason.put("reason", "无操作权限");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                // 收集待修改的电池
                batteriesNeedUpdate.add(electricityBattery);
            }
            
            if (CollectionUtils.isEmpty(batteriesNeedUpdate)) {
                return R.ok(BatteryLabelBatchUpdateVO.builder().successCount(0).failureCount(failureCount).failReasons(failReasons).build());
            }
            
            
            inheritableThreadLocal.set(MDC.get("traceId"));
            batteriesNeedUpdate.parallelStream().forEach(battery -> {
                MDC.put("traceId", inheritableThreadLocal.get());
                
                if (BatteryLabelConstant.RECEIVED_LABEL_SET.contains(newLabel)) {
                    if (labelMap.containsKey(battery.getSn())) {
                        ElectricityBatteryLabel batteryLabel = labelMap.get(battery.getSn());
                        ElectricityBatteryLabel labelUpdate = createLabelUpdate(batteryLabel, newLabel, request);
                        electricityBatteryLabelService.updateById(labelUpdate);
                    }
                }
                
                if (BatteryLabelConstant.RECEIVED_LABEL_SET.contains(battery.getLabel()) && !BatteryLabelConstant.RECEIVED_LABEL_SET.contains(newLabel)) {
                    electricityBatteryLabelService.deleteReceivedData(battery.getSn());
                }
                
                // TODO 修改电池标签，此处可能存在并发问题，在本接口执行时，电池不是在仓、租借、锁定在仓，当修改时是这三种状态了，此时会出现错乱，若要避免需要在modifyLabel方法中加锁，并实时查询数据
                electricityBatteryService.modifyLabel(battery, null, user.getUid(), newLabel);
            });
        } finally {
            inheritableThreadLocal.remove();
            MDC.clear();
        }
        
        return R.ok();
    }
    
    /**
     * 权限校验
     * @return true-有操作权限；false-无权限
     */
    private boolean permissionVerification(ElectricityBattery battery, TokenUser user, Integer label) {
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            return !CollectionUtils.isEmpty(franchiseeIds) && franchiseeIds.contains(battery.getFranchiseeId());
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            // 门店用户，如果操作不属于管理员领用，就无权限修改
            if (!Objects.equals(label, BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode())) {
                return false;
            }
            
            // 操作为管理员领用的，校验门店所属加盟商与电池的是否一致
            List<Long> storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            List<Store> stores = service.selectByStoreIds(storeIds);
            List<Long> franchiseeIdsOfStoreUser = stores.stream().map(Store::getFranchiseeId).collect(Collectors.toList());
            return !CollectionUtils.isEmpty(franchiseeIdsOfStoreUser) && franchiseeIdsOfStoreUser.contains(battery.getFranchiseeId());
        }
        
        // 不是加盟商或门店的，校验租户即可
        return Objects.equals(user.getTenantId(), battery.getTenantId());
    }
    
    // 创建标签更新对象
    private ElectricityBatteryLabel createLabelUpdate(ElectricityBatteryLabel batteryLabel, Integer newLabel, BatteryLabelBatchUpdateRequest request) {
        ElectricityBatteryLabel labelUpdate = new ElectricityBatteryLabel();
        labelUpdate.setId(batteryLabel.getId());
        
        if (Objects.equals(newLabel, BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode())) {
            labelUpdate.setAdministratorId(request.getAdministratorId());
        } else {
            labelUpdate.setMerchantId(request.getMerchantId());
        }
        labelUpdate.setUpdateTime(System.currentTimeMillis());
        
        return labelUpdate;
    }
}
