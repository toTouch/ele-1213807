package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.CarLockCtrlHistory;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageCarBatteryRelPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePo;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.RenalPackageConfineEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import com.xiliulou.electricity.enums.SlippageTypeEnum;
import com.xiliulou.electricity.enums.UseStateEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermExpiredQryModel;
import com.xiliulou.electricity.reqparam.opt.carpackage.ExpirePackageOrderReq;
import com.xiliulou.electricity.service.CarLockCtrlHistoryService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageCarBatteryRelService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderFreezeService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.biz.CarRentalMemberTermExpireBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import com.xiliulou.electricity.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/11/26 17:44
 */

@Slf4j
@Service
@AllArgsConstructor
public class CarRentalMemberTermExpireBizServiceImpl implements CarRentalMemberTermExpireBizService {
    
    private final CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
    private final CarRentalPackageOrderService carRentalPackageOrderService;
    
    private final CarRentalPackageCarBatteryRelService carRentalPackageCarBatteryRelService;
    
    private final UserBatteryTypeService userBatteryTypeService;
    
    private final ElectricityCarService carService;
    
    private final UserInfoService userInfoService;
    
    private final CarRentalOrderBizService carRentalOrderBizService;
    
    private final ElectricityConfigService electricityConfigService;
    
    private final ElectricityBatteryService batteryService;
    
    private final CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;
    
    private final CarLockCtrlHistoryService carLockCtrlHistoryService;
    
    private final CarRentalPackageOrderFreezeService carRentalPackageOrderFreezeService;
    
    @Override
    public void expirePackageOrder(ExpirePackageOrderReq req) {
        long nowTime = System.currentTimeMillis();
        
        // 查询参数转换
        CarRentalPackageMemberTermExpiredQryModel qryModel = this.converterQueryModel(req, nowTime);
        
        // 租户对应的设置 当前请求的缓存，减少查询 租户id->配置
        Map<Integer, ElectricityConfig> electricityConfigCache = new HashMap<>();
        
        while (true) {
            
            // 查询已经过期的会员期限
            List<CarRentalPackageMemberTermPo> expireMemberTermPos = carRentalPackageMemberTermService.queryListExpireByParam(
                    qryModel);
            if (CollectionUtils.isEmpty(expireMemberTermPos)) {
                break;
            }
            qryModel.setStartId(expireMemberTermPos.get(expireMemberTermPos.size() - 1).getId());
            
            // 根据租户分组处理，可以进行批量查询
            Map<Integer, List<CarRentalPackageMemberTermPo>> tenantMap = expireMemberTermPos.stream()
                    .collect(Collectors.groupingBy(CarRentalPackageMemberTermPo::getTenantId));
            
            //根据租户分组处理过期套餐
            tenantMap.forEach(
                    (tenantId, memberTermPos) -> this.processingExpiredByTenant(nowTime, tenantId, memberTermPos,
                            electricityConfigCache));
        }
    }
    
    
    /**
     * 请求参数转换查询model
     *
     * @param req     任务参数
     * @param nowTime 当前时间
     * @author caobotao.cbt
     * @date 2024/11/27 09:04
     */
    private CarRentalPackageMemberTermExpiredQryModel converterQueryModel(ExpirePackageOrderReq req, long nowTime) {
        CarRentalPackageMemberTermExpiredQryModel qryModel = new CarRentalPackageMemberTermExpiredQryModel();
        qryModel.setSize(req.getSize());
        qryModel.setNowTime(nowTime);
        qryModel.setStartId(0L);
        if (ObjectUtils.isNotEmpty(req.getTenantIds())) {
            qryModel.setTenantIds(req.getTenantIds());
        }
        return qryModel;
    }
    
    
    /**
     * 根据租户分组处理
     *
     * @param nowTime                当前时间
     * @param tenantId               租户id
     * @param memberTermPos          租车会员集合
     * @param electricityConfigCache 租户配置
     * @author caobotao.cbt
     * @date 2024/11/26 11:32
     */
    private void processingExpiredByTenant(long nowTime, Integer tenantId,
            List<CarRentalPackageMemberTermPo> memberTermPos, Map<Integer, ElectricityConfig> electricityConfigCache) {
        
        // 处理过期滞纳金的
        List<CarRentalPackageMemberTermPo> processingLateFeeList = new ArrayList<>();
        
        // 处理套餐替换的
        Map<CarRentalPackageMemberTermPo, CarRentalPackageOrderPo> packageReplacementMap = new HashMap<>();
        
        for (CarRentalPackageMemberTermPo expireMemberTermPo : memberTermPos) {
            
            // 查询用户是否存在支付成功未使用的订单
            CarRentalPackageOrderPo carRentalPackageOrderPo = carRentalPackageOrderService.selectFirstUnUsedAndPaySuccessByUid(
                    expireMemberTermPo.getTenantId(), expireMemberTermPo.getUid());
            
            if (ObjectUtils.isNotEmpty(carRentalPackageOrderPo)) {
                // 存在未使用的套餐,替换套餐
                packageReplacementMap.put(expireMemberTermPo, carRentalPackageOrderPo);
            } else {
                processingLateFeeList.add(expireMemberTermPo);
            }
        }
        
        // 处理套餐替换的
        this.processingPackageReplacement(nowTime, packageReplacementMap);
        
        // 处理过期滞纳金
        this.processingExpiredLateFee(tenantId, nowTime, processingLateFeeList, electricityConfigCache);
        
    }
    
    
    /**
     * 处理套餐过期以及滞纳金
     *
     * @param tenantId               租户id
     * @param nowTime                当前时间
     * @param processingLateFeeList  需处理过期滞纳金的集合
     * @param electricityConfigCache 租户配置
     * @author caobotao.cbt
     * @date 2024/11/26 14:50
     */
    private void processingExpiredLateFee(Integer tenantId, long nowTime,
            List<CarRentalPackageMemberTermPo> processingLateFeeList,
            Map<Integer, ElectricityConfig> electricityConfigCache) {
        
        if (processingLateFeeList.isEmpty()) {
            return;
        }
        
        List<Long> uidList = new ArrayList<>(processingLateFeeList.size());
        List<String> rentalPackageOrderNoList = new ArrayList<>(processingLateFeeList.size());
        
        processingLateFeeList.forEach(carRentalPackageMemberTermPo -> {
            uidList.add(carRentalPackageMemberTermPo.getUid());
            rentalPackageOrderNoList.add(carRentalPackageMemberTermPo.getRentalPackageOrderNo());
        });
        
        // 过期滞纳金查询
        Map<String, CarRentalPackageOrderSlippagePo> expireOrderSlippageMap = this.getExpiredOrderSlippageMap(
                rentalPackageOrderNoList);
        
        // 批量查询车辆
        Map<Long, ElectricityCar> uidCarMap = this.getElectricityCarMap(tenantId, uidList);
        
        // 批量查询用户
        Map<Long, UserInfo> uidUserMap = this.getUserInfoMap(uidList, tenantId);
        
        // 查询租户配置
        ElectricityConfig electricityConfig = this.getTenantElectricityConfig(tenantId, electricityConfigCache);
        
        // 已经生成过过期滞纳金的记录
        List<CarRentalPackageMemberTermPo> existExpirOrderSlippageList = new ArrayList<>();
        
        // 未生成过过期滞纳金的记录
        List<CarRentalPackageMemberTermPo> noExpirOrderSlippageList = new ArrayList<>();
        
        List<String> noExpirRentalPackageOrderNoList = new ArrayList<>();
        
        processingLateFeeList.forEach(processLateFee -> {
            if (expireOrderSlippageMap.containsKey(processLateFee.getRentalPackageOrderNo())) {
                existExpirOrderSlippageList.add(processLateFee);
            } else {
                noExpirOrderSlippageList.add(processLateFee);
                noExpirRentalPackageOrderNoList.add(processLateFee.getRentalPackageOrderNo());
            }
        });
        
        // 处理已经处理过的过期订单
        this.processingExistOrderSlippage(nowTime, existExpirOrderSlippageList, uidCarMap, uidUserMap,
                electricityConfig);
        
        if (noExpirOrderSlippageList.isEmpty()) {
            return;
        }
        
        // 处理未生成过期滞纳金的数据
        
        // 批量查询套餐
        Map<String, CarRentalPackageOrderPo> orderNoCarRentalPackageOrderMap = this.getCarRentalPackageOrderMap(
                tenantId, noExpirRentalPackageOrderNoList);
        
        // 批量查询冻结订单（兜底）
        Map<String, CarRentalPackageOrderSlippagePo> freezeOrderSlippageMap = this.getFreezeCarRentalPackageOrderSlippage(
                noExpirRentalPackageOrderNoList);
        
        // 循环执行
        noExpirOrderSlippageList.forEach(packageMemberTermPo -> {
            ElectricityCar electricityCar = uidCarMap.get(packageMemberTermPo.getUid());
            UserInfo userInfo = uidUserMap.get(packageMemberTermPo.getUid());
            CarRentalPackageOrderPo carRentalPackageOrderPo = orderNoCarRentalPackageOrderMap.get(
                    packageMemberTermPo.getRentalPackageOrderNo());
            
            // 冻结滞纳金订单
            CarRentalPackageOrderSlippagePo freezeOrderSlippage = freezeOrderSlippageMap.get(
                    packageMemberTermPo.getRentalPackageOrderNo());
            
            // 执行
            this.processLateFee(nowTime, packageMemberTermPo, electricityCar, userInfo, carRentalPackageOrderPo,
                    freezeOrderSlippage, electricityConfig);
            
        });
        
    }
    
    
    /**
     * 处理已处理过的过期订单
     *
     * @author caobotao.cbt
     * @date 2024/12/19 17:49
     */
    private void processingExistOrderSlippage(Long nowTime, List<CarRentalPackageMemberTermPo> processingLateFeeList,
            Map<Long, ElectricityCar> uidCarMap, Map<Long, UserInfo> uidUserMap, ElectricityConfig electricityConfig) {
        
        if (processingLateFeeList.isEmpty()) {
            return;
        }
        
        // 如果已经生成过期滞纳金，则无需处理 只需加锁（保持原逻辑，也不需要记录）
        processingLateFeeList.forEach(processingLateFee -> {
            ElectricityCar electricityCar = uidCarMap.get(processingLateFee.getUid());
            UserInfo userInfo = uidUserMap.get(processingLateFee.getUid());
            
            // 处理车辆加锁
            this.processCarLockCtrl(nowTime, electricityCar, userInfo, electricityConfig,
                    CarLockCtrlHistory.TYPE_MEMBER_CARD_LOCK);
        });
    }
    
    
    /**
     * 查询过期滞纳金
     *
     * @param rentalPackageOrderNoList 购买订单编号
     * @author caobotao.cbt
     * @date 2024/12/19 17:22
     */
    private Map<String, CarRentalPackageOrderSlippagePo> getExpiredOrderSlippageMap(
            List<String> rentalPackageOrderNoList) {
        
        List<CarRentalPackageOrderSlippagePo> existOrderSlippagePo = carRentalPackageOrderSlippageService.queryListByPackageOrderNoAndType(
                rentalPackageOrderNoList, SlippageTypeEnum.EXPIRE.getCode());
        
        return Optional.ofNullable(existOrderSlippagePo).orElse(Collections.emptyList()).stream().collect(
                Collectors.toMap(CarRentalPackageOrderSlippagePo::getRentalPackageOrderNo, Function.identity(),
                        (k1, k2) -> k1));
    }
    
    
    /**
     * 查询冻结滞纳金订单
     */
    private Map<String, CarRentalPackageOrderSlippagePo> getFreezeCarRentalPackageOrderSlippage(
            List<String> rentalPackageOrderNoList) {
        List<CarRentalPackageOrderSlippagePo> carRentalPackageOrderSlippagePos = carRentalPackageOrderSlippageService.queryListByPackageOrderNoAndType(
                rentalPackageOrderNoList, SlippageTypeEnum.FREEZE.getCode());
        
        return Optional.ofNullable(carRentalPackageOrderSlippagePos).orElse(Collections.emptyList()).stream().collect(
                Collectors.toMap(CarRentalPackageOrderSlippagePo::getRentalPackageOrderNo, Function.identity(),
                        (k1, k2) -> k1));
        
    }
    
    
    /**
     * 执行滞纳金处理
     *
     * @param nowTime                 当前时间
     * @param packageMemberTermPo     会员
     * @param electricityCar          车辆
     * @param userInfo                用户
     * @param carRentalPackageOrderPo 套餐
     * @param freezeOrderSlippage     冻结滞纳金订单
     * @param electricityConfig       租户配置
     * @author caobotao.cbt
     * @date 2024/11/27 09:19
     */
    private void processLateFee(long nowTime, CarRentalPackageMemberTermPo packageMemberTermPo,
            ElectricityCar electricityCar, UserInfo userInfo, CarRentalPackageOrderPo carRentalPackageOrderPo,
            CarRentalPackageOrderSlippagePo freezeOrderSlippage, ElectricityConfig electricityConfig) {
        
        try {
            // 处理车辆加锁
            CarLockCtrlHistory carLockCtrlHistory = this.processCarLockCtrl(nowTime, electricityCar, userInfo,
                    electricityConfig, CarLockCtrlHistory.TYPE_MEMBER_CARD_LOCK);
            
            CarRentalPackageOrderSlippagePo insertOrderExpiredSlippagePo = this.buildInsertExpiredFreezeOrderSlippagePo(
                    nowTime, electricityConfig, packageMemberTermPo, carRentalPackageOrderPo, electricityCar);
            
            if (Objects.nonNull(insertOrderExpiredSlippagePo)) {
                
                // 处理冻结滞纳金订单
                this.processFreezeOrderSlippage(freezeOrderSlippage, nowTime);
                
                // 生成过期滞纳金 则需要给车辆加过期滞纳金锁
                carLockCtrlHistory = this.processCarLockCtrl(nowTime, electricityCar, userInfo, electricityConfig,
                        CarLockCtrlHistory.TYPE_SLIPPAGE_LOCK);
                
                carRentalPackageOrderSlippageService.insert(insertOrderExpiredSlippagePo);
            } else if (carRentalPackageOrderPo.getUseState().equals(UseStateEnum.EXPIRED.getCode())) {
               
                // 如果不生过期滞纳金并且套餐使用状态是已过期，则不处理状态变更已经锁车记录
                return;
            }
            
            
            
            if (Objects.nonNull(carLockCtrlHistory)) {
                carLockCtrlHistoryService.insert(carLockCtrlHistory);
            }
            
            carRentalPackageOrderService.updateUseStateByOrderNo(packageMemberTermPo.getRentalPackageOrderNo(),
                    UseStateEnum.EXPIRED.getCode(), null, null);
            
        } catch (Exception e) {
            log.warn("Exception in late fee processing:", e);
        }
    }
    
    /**
     * 处理未处理的冻结滞纳金 （理论不存在，兜底）
     *
     * @param freezeOrderSlippage 冻结滞纳金订单
     * @param nowTime             当前时间
     * @author caobotao.cbt
     * @date 2024/12/19 11:08
     */
    private void processFreezeOrderSlippage(CarRentalPackageOrderSlippagePo freezeOrderSlippage, long nowTime) {
        
        if (Objects.isNull(freezeOrderSlippage) || Objects.nonNull(freezeOrderSlippage.getLateFeeEndTime())) {
            // 不存在冻结滞纳金或者冻结滞纳金已处理
            return;
        }
        
        // 生成过期滞纳金前要将冻结滞纳金截止,冻结滞纳金处理应该在冻结自动启用任务中，此逻辑只作为兜底方案，如果走到此逻辑，说明冻结滞纳金启用任务没启动，正常不会存在。
        log.warn("WARN! processFreezeOrderSlippage freezeOrderSlippage:{}", JsonUtil.toJson(freezeOrderSlippage));
        
        freezeOrderSlippage.setUpdateTime(nowTime);
        // 根据UID+套餐购买订单编号，获取冻结的订单
        CarRentalPackageOrderFreezePo orderFreezePo = carRentalPackageOrderFreezeService.selectFreezeByUidAndPackageOrderNo(
                freezeOrderSlippage.getUid(), freezeOrderSlippage.getRentalPackageOrderNo());
        if (ObjectUtils.isEmpty(orderFreezePo)) {
            return;
        }
        
        // 到期时间
        //                                    long expireTime = orderFreezePo.getCreateTime() + (TimeConstant.DAY_MILLISECOND * orderFreezePo.getApplyTerm());
        long expireTime = orderFreezePo.getAuditTime() + (TimeConstant.DAY_MILLISECOND * orderFreezePo.getApplyTerm());
        freezeOrderSlippage.setLateFeeEndTime(expireTime);
        // 计算滞纳金金额
        long diffDay = DateUtils.diffDay(freezeOrderSlippage.getLateFeeStartTime(),
                freezeOrderSlippage.getLateFeeEndTime());
        freezeOrderSlippage.setLateFeePay(
                freezeOrderSlippage.getLateFee().multiply(new BigDecimal(diffDay)).setScale(2, RoundingMode.HALF_UP));
        
        // 更新对应的因冻结的产生的逾期订单记录
        carRentalPackageOrderSlippageService.updateById(freezeOrderSlippage);
        
    }
    
    
    /**
     * 处理车辆加锁
     *
     * @param nowTime           当前时间
     * @param electricityCar    车辆
     * @param userInfo          用户
     * @param electricityConfig 租户配置
     * @author caobotao.cbt
     * @date 2024/11/27 09:22
     */
    private CarLockCtrlHistory processCarLockCtrl(Long nowTime, ElectricityCar electricityCar, UserInfo userInfo,
            ElectricityConfig electricityConfig, Integer type) {
        if (Objects.nonNull(electricityCar) && Objects.nonNull(electricityConfig) && Objects.equals(
                electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {
            return buildCarLockCtrlHistory(electricityCar, userInfo, nowTime, type);
        }
        return null;
    }
    
    /**
     * 获取车辆信息
     *
     * @param tenantId 租户id
     * @param uidList  用户id集合
     * @author caobotao.cbt
     * @date 2024/11/27 09:09
     */
    private Map<Long, ElectricityCar> getElectricityCarMap(Integer tenantId, List<Long> uidList) {
        List<ElectricityCar> electricityCars = carService.queryListByTenantIdAndUidList(tenantId, uidList);
        return Optional.ofNullable(electricityCars).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(ElectricityCar::getUid, Function.identity(), (k1, k2) -> k1));
    }
    
    
    /**
     * 获取用户信息
     *
     * @param tenantId 租户id
     * @param uidList  用户id集合
     * @author caobotao.cbt
     * @date 2024/11/27 09:09
     */
    private Map<Long, UserInfo> getUserInfoMap(List<Long> uidList, Integer tenantId) {
        List<UserInfo> userInfos = userInfoService.listByUids(uidList, tenantId);
        return Optional.ofNullable(userInfos).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(UserInfo::getUid, Function.identity(), (k1, k2) -> k1));
    }
    
    /**
     * 获取租车套餐
     *
     * @param tenantId                 租户id
     * @param rentalPackageOrderNoList 订单号集合
     * @author caobotao.cbt
     * @date 2024/11/27 09:09
     */
    private Map<String, CarRentalPackageOrderPo> getCarRentalPackageOrderMap(Integer tenantId,
            List<String> rentalPackageOrderNoList) {
        List<CarRentalPackageOrderPo> carRentalPackageOrderPos = carRentalPackageOrderService.queryListByOrderNo(
                tenantId, rentalPackageOrderNoList);
        return Optional.ofNullable(carRentalPackageOrderPos).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(CarRentalPackageOrderPo::getOrderNo, Function.identity(), (k1, k2) -> k1));
    }
    
    /**
     * 构建新增过期滞纳金
     *
     * @author caobotao.cbt
     * @date 2024/11/26 16:17
     */
    private CarRentalPackageOrderSlippagePo buildInsertExpiredFreezeOrderSlippagePo(Long nowTime,
            ElectricityConfig electricityConfig, CarRentalPackageMemberTermPo memberTermPo,
            CarRentalPackageOrderPo packageOrderEntity, ElectricityCar electricityCar) {
        
        // 获取过期保护期毫秒
        long expiredProtectionMillisecond = this.getExpiredProtectionMillisecond(electricityConfig);
        // 获取过期时间
        Long expireTime = memberTermPo.getDueTime() + expiredProtectionMillisecond;
        
        if (nowTime < expireTime) {
            return null;
        }
        
        // 查询当时购买的订单信息
        if (Objects.isNull(packageOrderEntity)) {
            log.warn("WARN not found car_rental_package_order. orderNo is {}", memberTermPo.getRentalPackageOrderNo());
            return null;
        }
        
        //车电一体
        ElectricityBattery battery = null;
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermPo.getRentalPackageType())) {
            // 车电一体
            battery = batteryService.queryByUid(memberTermPo.getUid());
        }
        
        if (Objects.isNull(electricityCar) && Objects.isNull(battery)) {
            return null;
        }
        
        // 生成实体记录
        CarRentalPackageOrderSlippagePo slippageEntity = new CarRentalPackageOrderSlippagePo();
        slippageEntity.setUid(memberTermPo.getUid());
        slippageEntity.setRentalPackageOrderNo(packageOrderEntity.getOrderNo());
        slippageEntity.setRentalPackageId(packageOrderEntity.getRentalPackageId());
        slippageEntity.setRentalPackageType(packageOrderEntity.getRentalPackageType());
        slippageEntity.setType(SlippageTypeEnum.EXPIRE.getCode());
        slippageEntity.setLateFee(packageOrderEntity.getLateFee());
        slippageEntity.setLateFeeStartTime(expireTime);
        slippageEntity.setPayState(PayStateEnum.UNPAID.getCode());
        slippageEntity.setTenantId(packageOrderEntity.getTenantId());
        slippageEntity.setFranchiseeId(packageOrderEntity.getFranchiseeId());
        slippageEntity.setStoreId(packageOrderEntity.getStoreId());
        slippageEntity.setCreateUid(memberTermPo.getUid());
        slippageEntity.setExpiredProtectionTime(electricityConfig.getExpiredProtectionTime());
        
        // 记录设备信息
        if (ObjectUtils.isNotEmpty(electricityCar)) {
            slippageEntity.setCarSn(electricityCar.getSn());
            slippageEntity.setCarModelId(electricityCar.getModelId());
        }
        if (ObjectUtils.isNotEmpty(battery)) {
            slippageEntity.setBatterySn(battery.getSn());
            slippageEntity.setBatteryModelType(battery.getModel());
        }
        
        return slippageEntity;
        
    }
    
    
    /**
     * 获取过期保护期毫秒
     *
     * @param electricityConfig 租户配置
     * @author caobotao.cbt
     * @date 2024/11/26 14:54
     */
    private long getExpiredProtectionMillisecond(ElectricityConfig electricityConfig) {
        if (Objects.isNull(electricityConfig)) {
            return TimeConstant.DAY_MILLISECOND;
        }
        // 套餐过期保护期 小时
        Integer expiredProtectionTime = Optional.ofNullable(electricityConfig.getExpiredProtectionTime())
                .orElse(TimeConstant.DAT_HOURS);
        // 转换毫秒
        return expiredProtectionTime * 60L * 60L * 1000L;
    }
    
    
    /**
     * 获取租户对应的套餐保护期
     *
     * @param tenantId               租户id
     * @param electricityConfigCache 租户配置
     * @author caobotao.cbt
     * @date 2024/11/26 11:25
     */
    private ElectricityConfig getTenantElectricityConfig(Integer tenantId,
            Map<Integer, ElectricityConfig> electricityConfigCache) {
        
        if (!electricityConfigCache.containsKey(tenantId)) {
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            electricityConfigCache.put(tenantId, electricityConfig);
        }
        
        return electricityConfigCache.get(tenantId);
    }
    
    
    /**
     * 处理套餐替换
     *
     * @param nowTime               当前时间
     * @param packageReplacementMap 替换套餐
     * @author caobotao.cbt
     * @date 2024/11/26 14:43
     */
    private void processingPackageReplacement(long nowTime,
            Map<CarRentalPackageMemberTermPo, CarRentalPackageOrderPo> packageReplacementMap) {
        if (packageReplacementMap.isEmpty()) {
            return;
        }
        
        packageReplacementMap.forEach(
                (packageMemberTermPo, carRentalPackageOrderPo) -> this.packageReplacement(nowTime, packageMemberTermPo,
                        carRentalPackageOrderPo));
    }
    
    /**
     * 套餐过期替换
     *
     * @param nowTime               当前时间
     * @param expireMemberTermPo    过期会员
     * @param packageOrderEntityNew 新套餐
     * @author caobotao.cbt
     * @date 2024/11/26 08:59
     */
    private void packageReplacement(Long nowTime, CarRentalPackageMemberTermPo expireMemberTermPo,
            CarRentalPackageOrderPo packageOrderEntityNew) {
        
        // 当前套餐已经在使用了 不处理
        if (expireMemberTermPo.getRentalPackageOrderNo().equals(packageOrderEntityNew.getOrderNo())) {
            log.info("t_car_rental_package_member_term processed. skip. id is {}", packageOrderEntityNew.getId());
            return;
        }
        
        // 更改原订单状态
        carRentalPackageOrderService.updateUseStateByOrderNo(expireMemberTermPo.getRentalPackageOrderNo(),
                UseStateEnum.EXPIRED.getCode(), null, null);
        
        // 构建会员期限更新信息
        CarRentalPackageMemberTermPo memberTermEntityUpdate = this.buildUpdateMemberTermEntity(expireMemberTermPo,
                packageOrderEntityNew, nowTime);
        
        // 更新会员期限
        carRentalPackageMemberTermService.updateById(memberTermEntityUpdate);
        
        // 更改原订单状态及新订单状态, 此处有一个小坑，正常逻辑来讲，需要传入使用时间，需要注意
        carRentalPackageOrderService.updateUseStateByOrderNo(packageOrderEntityNew.getOrderNo(),
                UseStateEnum.IN_USE.getCode(), null, null);
        
        // 同步电池数据
        synchronizeBatteryData(packageOrderEntityNew);
    }
    
    
    /**
     * 车电一体，同步电池那边的数据
     *
     * @param packageOrderEntityNew 新套餐
     * @author caobotao.cbt
     * @date 2024/11/26 08:57
     */
    private void synchronizeBatteryData(CarRentalPackageOrderPo packageOrderEntityNew) {
        if (!RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(packageOrderEntityNew.getRentalPackageType())) {
            return;
        }
        
        List<CarRentalPackageCarBatteryRelPo> carBatteryRelPos = carRentalPackageCarBatteryRelService.selectByRentalPackageId(
                packageOrderEntityNew.getRentalPackageId());
        if (CollectionUtils.isEmpty(carBatteryRelPos)) {
            return;
        }
        
        List<String> batteryTypes = carBatteryRelPos.stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType)
                .collect(Collectors.toList());
        log.info("synchronizedUserBatteryType, batteryTypes is {}", JsonUtil.toJson(batteryTypes));
        userBatteryTypeService.synchronizedUserBatteryType(packageOrderEntityNew.getUid(),
                packageOrderEntityNew.getTenantId(), batteryTypes);
    }
    
    
    /**
     * 构建更新会员期限数据
     *
     * @param expireMemberTermPo    过期会员
     * @param packageOrderEntityNew 新套餐
     * @param nowTime               当前时间
     * @author caobotao.cbt
     * @date 2024/11/26 08:46
     */
    private CarRentalPackageMemberTermPo buildUpdateMemberTermEntity(CarRentalPackageMemberTermPo expireMemberTermPo,
            CarRentalPackageOrderPo packageOrderEntityNew, Long nowTime) {
        CarRentalPackageMemberTermPo memberTermEntityUpdate = new CarRentalPackageMemberTermPo();
        memberTermEntityUpdate.setRentalPackageOrderNo(packageOrderEntityNew.getOrderNo());
        memberTermEntityUpdate.setRentalPackageId(packageOrderEntityNew.getRentalPackageId());
        memberTermEntityUpdate.setRentalPackageConfine(packageOrderEntityNew.getConfine());
        memberTermEntityUpdate.setId(expireMemberTermPo.getId());
        
        // 计算到期时间
        Long dueTime = Optional.ofNullable(expireMemberTermPo.getDueTime()).orElse(nowTime);
        dueTime = calculateDueTime(dueTime, packageOrderEntityNew.getTenancy(), packageOrderEntityNew.getTenancyUnit());
        memberTermEntityUpdate.setDueTime(dueTime);
        
        // 计算余量
        memberTermEntityUpdate.setResidue(calculateResidue(expireMemberTermPo, packageOrderEntityNew));
        
        return memberTermEntityUpdate;
    }
    
    
    /**
     * 计算到期时间
     *
     * @param dueTime     到期时间
     * @param tenancy     租期
     * @param tenancyUnit 租期单位
     * @author caobotao.cbt
     * @date 2024/11/26 08:36
     */
    private Long calculateDueTime(Long dueTime, Integer tenancy, Integer tenancyUnit) {
        long additionalTime = 0;
        if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
            additionalTime = tenancy * TimeConstant.DAY_MILLISECOND;
        } else if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
            additionalTime = tenancy * TimeConstant.MINUTE_MILLISECOND;
        }
        return dueTime + additionalTime;
    }
    
    /**
     * 计算余量
     *
     * @param expireMemberTermPo    过期会员
     * @param packageOrderEntityNew 新套餐
     * @author caobotao.cbt
     * @date 2024/11/26 08:37
     */
    private Long calculateResidue(CarRentalPackageMemberTermPo expireMemberTermPo,
            CarRentalPackageOrderPo packageOrderEntityNew) {
        if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntityNew.getConfine())) {
            // 按次
            long currentResidue = Optional.ofNullable(expireMemberTermPo.getResidue()).orElse(0L);
            // 老套餐有余量则清除，超过使用次数则新套餐次数-老余量
            return currentResidue >= 0L ? packageOrderEntityNew.getConfineNum()
                    : packageOrderEntityNew.getConfineNum() + currentResidue;
        }
        return 0L;
    }
    
    
    /**
     * 构建JT808
     *
     * @param electricityCar 车辆
     * @param userInfo       用户
     * @return CarLockCtrlHistory
     */
    private CarLockCtrlHistory buildCarLockCtrlHistory(ElectricityCar electricityCar, UserInfo userInfo, Long nowTime,
            Integer type) {
        Integer tenantId = userInfo.getTenantId();
        
        boolean result = carRentalOrderBizService.retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_LOCK, 3);
        log.info("buildCarLockCtrlHistory, carRentalOrderBizService.retryCarLockCtrl result is {}", result);
        
        CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
        carLockCtrlHistory.setUid(userInfo.getUid());
        carLockCtrlHistory.setName(userInfo.getName());
        carLockCtrlHistory.setPhone(userInfo.getPhone());
        carLockCtrlHistory.setStatus(
                result ? CarLockCtrlHistory.STATUS_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_LOCK_FAIL);
        carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
        carLockCtrlHistory.setCarModel(electricityCar.getModel());
        carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
        carLockCtrlHistory.setCarSn(electricityCar.getSn());
        carLockCtrlHistory.setCreateTime(nowTime);
        carLockCtrlHistory.setUpdateTime(nowTime);
        carLockCtrlHistory.setTenantId(tenantId);
        carLockCtrlHistory.setType(type);
        
        return carLockCtrlHistory;
    }
    
}
