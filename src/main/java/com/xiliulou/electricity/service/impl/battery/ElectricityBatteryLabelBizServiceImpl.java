package com.xiliulou.electricity.service.impl.battery;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.battery.BatteryLabelConstant;
import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDTO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.request.battery.BatteryLabelBatchUpdateRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelBizService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityBatteryDataVO;
import com.xiliulou.electricity.vo.battery.BatteryLabelBatchUpdateVO;
import com.xiliulou.electricity.vo.battery.ElectricityBatteryLabelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author SJP
 * @date 2025-02-20 17:54
 **/
@Slf4j
@Service
public class ElectricityBatteryLabelBizServiceImpl implements ElectricityBatteryLabelBizService {
    
    @Resource
    private ElectricityBatteryLabelService electricityBatteryLabelService;
    
    @Resource
    private ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    @Resource
    private StoreService service;
    
    @Resource
    private ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private RentBatteryOrderService rentBatteryOrderService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private MerchantService merchantService;
    
    private final static ExecutorService checkRentStatusForLabelExecutor = XllThreadPoolExecutors.newFixedThreadPool("checkRentStatusForLabel", 1, "CHECK_RENT_STATUS_FOR_LABEL_THREAD");
    
    
    @Override
    public R updateRemark(String sn, String remark) {
        Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityBattery battery = electricityBatteryService.queryBySnFromDb(sn, tenantId);
        if (Objects.isNull(battery)) {
            log.warn("UPDATE REMARK WARN! battery is null, sn={}, tenantId={}", sn, tenantId);
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }
        
        ElectricityBatteryLabel batteryLabel = ElectricityBatteryLabel.builder().remark(remark).build();
        updateOrInsertBatteryLabel(battery, batteryLabel);
        return R.ok();
    }
    
    @Override
    public void updateOrInsertBatteryLabel(ElectricityBattery battery, ElectricityBatteryLabel batteryLabel) {
        Integer tenantId = battery.getTenantId();
        String sn = battery.getSn();
        
        ElectricityBatteryLabel batteryLabelFromDb = electricityBatteryLabelService.queryBySnAndTenantId(sn, tenantId);
        Long now = System.currentTimeMillis();
        
        if (Objects.isNull(batteryLabelFromDb)) {
            ElectricityBatteryLabel newBatteryLabel = new ElectricityBatteryLabel();
            BeanUtils.copyProperties(batteryLabel, newBatteryLabel);
            newBatteryLabel.setSn(sn);
            newBatteryLabel.setTenantId(tenantId);
            newBatteryLabel.setFranchiseeId(battery.getFranchiseeId());
            newBatteryLabel.setCreateTime(now);
            newBatteryLabel.setUpdateTime(now);
            
            electricityBatteryLabelService.insert(newBatteryLabel);
        } else {
            ElectricityBatteryLabel batteryLabelUpdate = new ElectricityBatteryLabel();
            BeanUtils.copyProperties(batteryLabel, batteryLabelUpdate);
            
            // 出于健壮性考虑，清除一下不能通过此处逻辑自动修改的属性
            batteryLabel.setSn(null);
            batteryLabel.setTenantId(null);
            batteryLabel.setCreateTime(null);
            
            batteryLabelUpdate.setId(batteryLabelFromDb.getId());
            batteryLabelUpdate.setUpdateTime(now);
            electricityBatteryLabelService.updateById(batteryLabelUpdate);
        }
    }
    
    @Override
    public R batchUpdate(BatteryLabelBatchUpdateRequest request) {
        try {
            User user = userService.queryByUidFromCache(SecurityUtils.getUid());
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
            if (Objects.isNull(newLabel)) {
                log.warn("BATCH UPDATE LABEL WARN! newLabel is null");
                return R.fail("300153", "新电池标签不可为空");
            }
            
            if (Objects.isNull(request.getReceiverId()) && BatteryLabelConstant.RECEIVED_LABEL_SET.contains(newLabel)) {
                log.warn("BATCH UPDATE LABEL WARN! administratorId is empty, request={}", request);
                return R.fail("300152", "领用人不可为空");
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
                    failReason.put("reason", "电池编码不存在");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                ElectricityBattery electricityBattery = batteryMap.get(sn);
                Integer oldLabel = electricityBattery.getLabel();
                if (Objects.equals(oldLabel, newLabel)) {
                    // 不是领用的不用修改
                    if (!BatteryLabelConstant.RECEIVED_LABEL_SET.contains(newLabel)) {
                        failReason.put("reason", "新旧标签相同");
                        failReasons.add(failReason);
                        failureCount = failureCount + 1;
                        continue;
                    }
                    
                    // 领用的，校验其领用人是否相同
                    ElectricityBatteryLabel batteryLabel = labelMap.get(sn);
                    if (Objects.nonNull(batteryLabel) && Objects.equals(batteryLabel.getReceiverId(), request.getReceiverId())) {
                        failReason.put("reason", "新旧标签相同");
                        failReasons.add(failReason);
                        failureCount = failureCount + 1;
                        continue;
                    }
                }
                
                if (Objects.equals(oldLabel, BatteryLabelEnum.IN_THE_CABIN.getCode())) {
                    failReason.put("reason", "电池在仓不支持此操作");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                if (Objects.nonNull(oldLabel) && BatteryLabelConstant.RENT_LABEL_SET.contains(oldLabel)) {
                    failReason.put("reason", "电池租借中不支持此操作");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                if (Objects.equals(oldLabel, BatteryLabelEnum.LOCKED_IN_THE_CABIN.getCode())) {
                    failReason.put("reason", "电池锁定在仓不支持此操作");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                // 校验操作人权限
                if (!permissionVerificationForUser(electricityBattery, user, newLabel, labelMap.get(sn))) {
                    failReason.put("reason", "无操作权限");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                // 校验领用人权限
                if (!permissionVerificationForReceiver(electricityBattery, request.getReceiverId(), newLabel, labelMap.get(sn))) {
                    failReason.put("reason", "领用人无权领用");
                    failReasons.add(failReason);
                    failureCount = failureCount + 1;
                    continue;
                }
                
                // 校验电池是否出库，即是否绑定加盟商
                if (Objects.equals(electricityBattery.getLabel(), BatteryLabelEnum.INVENTORY.getCode()) && Objects.equals(electricityBattery.getStockStatus(),
                        StockStatusEnum.STOCK.getCode())) {
                    failReason.put("reason", "该电池未出库，不支持更新");
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
            
            BatteryLabelModifyDTO modifyDto = BatteryLabelModifyDTO.builder().newLabel(newLabel).operatorUid(user.getUid()).newReceiverId(request.getReceiverId()).build();
            String traceId = MDC.get(CommonConstant.TRACE_ID);
            batteriesNeedUpdate.parallelStream().forEach(battery -> {
                MDC.put(CommonConstant.TRACE_ID, traceId);
                
                // TODO 修改电池标签，此处可能存在并发问题，在本接口执行时，电池不是在仓、租借、锁定在仓，当修改时是这三种状态了，此时会出现错乱，若要避免需要在modifyLabel方法中加锁，并实时查询数据
                electricityBatteryService.syncModifyLabel(battery, null, modifyDto);
            });
            
            return R.ok(BatteryLabelBatchUpdateVO.builder().successCount(snList.size() - failureCount).failureCount(failureCount).failReasons(failReasons).build());
        } catch (Exception e) {
            log.error("BATCH UPDATE LABEL ERROR! request={}", request, e);
            return R.fail("100227", "操作失败");
        }
    }
    
    /**
     * 操作人权限校验
     *
     * @return true-有操作权限；false-无权限
     */
    private boolean permissionVerificationForUser(ElectricityBattery battery, User user, Integer label, ElectricityBatteryLabel batteryLabel) {
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            return !CollectionUtils.isEmpty(franchiseeIds) && franchiseeIds.contains(battery.getFranchiseeId());
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            // 门店用户，如果操作不属于管理员领用，校验电池是否是其领用的
            if (!Objects.equals(label, BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode())) {
                return Objects.nonNull(batteryLabel) && Objects.equals(batteryLabel.getReceiverId(), user.getUid());
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
    
    /**
     * 领用人权限校验
     *
     * @return true-有操作权限；false-无权限
     */
    private boolean permissionVerificationForReceiver(ElectricityBattery battery, Long receiverId, Integer label, ElectricityBatteryLabel batteryLabel) {
        if (Objects.equals(label, BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode())) {
            User user = userService.queryByUidFromCache(receiverId);
            if (Objects.isNull(user)) {
                log.warn("PERMISSION VERIFICATION FOR RECEIVER WARN! user is null, receiverId={}", receiverId);
                return false;
            }
            
            return permissionVerificationForUser(battery, user, label, batteryLabel);
        }
        
        if (Objects.equals(label, BatteryLabelEnum.RECEIVED_MERCHANT.getCode())) {
            Merchant merchant = merchantService.queryByIdFromCache(receiverId);
            if (Objects.isNull(merchant)) {
                log.warn("PERMISSION VERIFICATION FOR RECEIVER WARN! merchant is null, receiverId={}", receiverId);
                return false;
            }
            
            return Objects.equals(merchant.getFranchiseeId(), battery.getFranchiseeId());
        }
        
        return true;
    }
    
    @Override
    public List<ElectricityBatteryLabelVO> listLabelVOByBatteries(List<String> sns, List<ElectricityBattery> electricityBatteryList) {
        Map<String, Integer> snAndLabel = electricityBatteryList.parallelStream().collect(Collectors.toMap(ElectricityBattery::getSn, ElectricityBattery::getLabel, (a, b) -> b));
        return electricityBatteryLabelService.listLabelVOBySns(sns, snAndLabel);
    }
    
    @Override
    public List<ElectricityBatteryLabelVO> listLabelVOByDataVOs(List<String> sns, List<ElectricityBatteryDataVO> electricityBatteries) {
        Map<String, Integer> snAndLabel = electricityBatteries.parallelStream()
                .collect(Collectors.toMap(ElectricityBatteryDataVO::getSn, ElectricityBatteryDataVO::getLabel, (a, b) -> b));
        return electricityBatteryLabelService.listLabelVOBySns(sns, snAndLabel);
    }
    
    @Override
    public void checkRentStatusForLabel(UserBatteryMemberCard userBatteryMemberCard, CarRentalPackageMemberTermPo memberTermPo) {
        try {
            if (Objects.isNull(userBatteryMemberCard) && Objects.isNull(memberTermPo)) {
                log.warn("CHECK RENT STATUS FOR LABEL WARN! userBatteryMemberCard and memberTermPo is null");
                return;
            }
            
            String traceId = MDC.get(CommonConstant.TRACE_ID);
            Integer tenantId = TenantContextHolder.getTenantId();
            checkRentStatusForLabelExecutor.execute(() -> {
                MDC.put(CommonConstant.TRACE_ID, traceId);
                Long uid = Objects.isNull(memberTermPo) ? userBatteryMemberCard.getUid() : memberTermPo.getUid();
                if (Objects.isNull(uid)) {
                    log.warn("CHECK RENT STATUS FOR LABEL WARN! uid is null");
                    return;
                }
                
                List<ElectricityBattery> batteries = electricityBatteryService.listByUid(uid, tenantId);
                
                if (CollectionUtils.isEmpty(batteries)) {
                    log.warn("CHECK RENT STATUS FOR LABEL WARN! batteries is null");
                    return;
                }
                
                for (ElectricityBattery battery : batteries) {
                    // 没有标签或者当前标签是在仓的，不处理
                    if (Objects.isNull(battery.getLabel()) || Objects.equals(battery.getLabel(), BatteryLabelEnum.IN_THE_CABIN.getCode())) {
                        log.info("CHECK RENT STATUS FOR LABEL INFO! sn={}, label={}", battery.getSn(), battery.getLabel());
                        return;
                    }
                    
                    // 判断租借逾期状态
                    Long dueTimeTotal = Objects.isNull(memberTermPo) ? userBatteryMemberCard.getMemberCardExpireTime() : memberTermPo.getDueTimeTotal();
                    if (Objects.nonNull(dueTimeTotal) && dueTimeTotal <= System.currentTimeMillis()) {
                        electricityBatteryService.syncModifyLabel(battery, null, new BatteryLabelModifyDTO(BatteryLabelEnum.RENT_OVERDUE.getCode()));
                        return;
                    }
                    
                    // 处理长时间未换电
                    ElectricityConfig config = electricityConfigService.queryFromCacheByTenantId(tenantId);
                    if (Objects.nonNull(config) && Objects.nonNull(config.getNotExchangeProtectionTime())) {
                        // 长时间未换电保护时间
                        long protectTime = config.getNotExchangeProtectionTime() * 24 * 60 * 60 * 1000;
                        ElectricityCabinetOrder latestOrderBySn = electricityCabinetOrderService.selectLatestBySn(battery.getSn());
                        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.selectLatestBySn(tenantId, battery.getSn());
                        Long lastExchangeTime = null;
                        if (Objects.nonNull(latestOrderBySn)) {
                            lastExchangeTime = latestOrderBySn.getUpdateTime();
                        }
                        
                        // 换电订单中没获取到，直接从租电订单中取，换电订单中获取到了比较获取最大的
                        if (Objects.nonNull(rentBatteryOrder) && Objects.nonNull(rentBatteryOrder.getUpdateTime())) {
                            lastExchangeTime = Objects.isNull(lastExchangeTime) ? rentBatteryOrder.getUpdateTime() : Math.max(lastExchangeTime, rentBatteryOrder.getUpdateTime());
                        }
                        
                        if (Objects.isNull(lastExchangeTime)) {
                            log.warn("CHECK RENT STATUS FOR LABEL WARN! lastExchangeTime is null, sn={}", battery.getSn());
                            return;
                        }
                        
                        if (System.currentTimeMillis() - lastExchangeTime >= protectTime) {
                            electricityBatteryService.syncModifyLabel(battery, null, new BatteryLabelModifyDTO(BatteryLabelEnum.RENT_LONG_TERM_UNUSED.getCode()));
                            return;
                        }
                    }
                    
                    // 已经是租借正常的不处理，不是的，改为租借正常
                    if (Objects.equals(battery.getLabel(), BatteryLabelEnum.RENT_NORMAL.getCode())) {
                        return;
                    }
                    electricityBatteryService.syncModifyLabel(battery, null, new BatteryLabelModifyDTO(BatteryLabelEnum.RENT_NORMAL.getCode()));
                }
                
            });
            
        } catch (Exception e) {
            log.error("CHECK RENT STATUS FOR LABEL error! userBatteryMemberCard={}, memberTermPo={}", userBatteryMemberCard, memberTermPo, e);
        }
    }
}
