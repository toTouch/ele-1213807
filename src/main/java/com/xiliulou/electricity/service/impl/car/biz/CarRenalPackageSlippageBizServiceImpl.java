package com.xiliulou.electricity.service.impl.car.biz;

import cn.hutool.core.util.NumberUtil;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.UserOperateRecordConstant;
import com.xiliulou.electricity.entity.CarLockCtrlHistory;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePo;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.OverdueType;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.RentalPackageOrderFreezeStatusEnum;
import com.xiliulou.electricity.enums.SlippageTypeEnum;
import com.xiliulou.electricity.event.publish.OverdueUserRemarkPublish;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.CarLockCtrlHistoryService;
import com.xiliulou.electricity.service.EleUserOperateRecordService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderFreezeService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.FreezeTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 逾期业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRenalPackageSlippageBizServiceImpl implements CarRenalPackageSlippageBizService {
    
    @Resource
    private CarLockCtrlHistoryService carLockCtrlHistoryService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private CarRentalOrderBizService carRentalOrderBizService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private ElectricityCarService carService;
    
    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;
    
    @Resource
    private CarRentalPackageOrderFreezeService carRentalPackageOrderFreezeService;
    
    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;
    
    @Resource
    private EleUserOperateRecordService eleUserOperateRecordService;
    
    @Autowired
    private OverdueUserRemarkPublish overdueUserRemarkPublish;
    
    /**
     * 清除滞纳金
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param optUid   操作用户ID
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean clearSlippage(Integer tenantId, Long uid, Long optUid, String userName) {
        if (!ObjectUtils.allNotNull(tenantId, uid, optUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 判定用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        if (Objects.isNull(userInfo)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            throw new BizException("ELECTRICITY.0024", "用户已被禁用");
        }
        
        // 查询名下当前所有类型的未支付、支付失败的逾期订单
        List<CarRentalPackageOrderSlippagePo> slippageEntityList = carRentalPackageOrderSlippageService.selectUnPayByByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(slippageEntityList)) {
            log.warn("clearSlippage, not found t_car_rental_package_order_slippage. uid is {}", uid);
            return true;
        }
        
        //获取滞纳金金额
        BigDecimal lateFeeAmount = queryCarPackageUnpaidAmountByUid(tenantId, uid);
        
        // 查询会员详情
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.warn("clearSlippage, not found t_car_rental_package_member_term or status is wrong. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }
        
        if (slippageEntityList.size() == 1 && SlippageTypeEnum.EXPIRE.getCode().equals(slippageEntityList.get(0).getType())) {
            saveClearSlippageTx(slippageEntityList, null, optUid, memberTermEntity);
        } else {
            // 查询冻结订单
            CarRentalPackageOrderFreezePo freezeEntity = carRentalPackageOrderFreezeService.selectLastFreeByUid(uid);
            
            if (ObjectUtils.isEmpty(freezeEntity)) {
                log.warn("clearSlippage, not found t_car_rental_package_order_freeze. uid is {}", uid);
                throw new BizException("300000", "数据有误");
            }
            saveClearSlippageTx(slippageEntityList, freezeEntity, optUid, memberTermEntity);
        }
        
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.CAR_MEMBER_CARD_MODEL)
                .operateContent(UserOperateRecordConstant.CLEAN_CAR_SERVICE_FEE).operateType(UserOperateRecordConstant.OPERATE_TYPE_CAR).operateUid(optUid).uid(uid).name(userName)
                .carServiceFee(lateFeeAmount).tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.asyncHandleUserOperateRecord(eleUserOperateRecord);
        //清除逾期用户备注
        overdueUserRemarkPublish.publish(uid, OverdueType.CAR.getCode(), tenantId);
        return true;
    }
    
    /**
     * 清除滞纳金事务处理
     *
     * @param slippageEntityList 逾期订单
     * @param freezeEntity       冻结订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveClearSlippageTx(List<CarRentalPackageOrderSlippagePo> slippageEntityList, CarRentalPackageOrderFreezePo freezeEntity, Long optUid,
            CarRentalPackageMemberTermPo memberTermEntity) {
        long now = System.currentTimeMillis();
        AtomicBoolean jt808Flag = new AtomicBoolean(false);
        // 处理逾期
        slippageEntityList.forEach(slippageEntity -> {
            CarRentalPackageOrderSlippagePo slippageUpdateEntity = new CarRentalPackageOrderSlippagePo();
            slippageUpdateEntity.setId(slippageEntity.getId());
            slippageUpdateEntity.setUpdateUid(optUid);
            slippageUpdateEntity.setUpdateTime(now);
            slippageUpdateEntity.setPayState(PayStateEnum.CLEAN_UP.getCode());
            slippageUpdateEntity.setPayTime(now);
            
            // 过期
            if (SlippageTypeEnum.EXPIRE.getCode().equals(slippageEntity.getType())) {
                slippageUpdateEntity.setLateFeeEndTime(now);
                // 转换天
                long diffDay = DateUtils.diffDay(slippageEntity.getLateFeeStartTime(), now);
                // 计算滞纳金金额
                slippageUpdateEntity.setLateFeePay(NumberUtil.mul(diffDay, slippageEntity.getLateFee()));
                
                // 更改会员期限表数据
                CarRentalPackageMemberTermPo memberTermEntityUpdate = new CarRentalPackageMemberTermPo();
                memberTermEntityUpdate.setDueTime(now);
                memberTermEntityUpdate.setDueTimeTotal(now);
                memberTermEntityUpdate.setId(memberTermEntity.getId());
                carRentalPackageMemberTermService.updateById(memberTermEntityUpdate);
            }
            
            // 冻结
            if (SlippageTypeEnum.FREEZE.getCode().equals(slippageEntity.getType())) {
                Long endTime = slippageEntity.getLateFeeEndTime();
                // 没有结束
                if (ObjectUtils.isEmpty(slippageEntity.getLateFeeEndTime())) {
                    slippageUpdateEntity.setLateFeeEndTime(now);
                    endTime = now;
                }
                // 转换天
                long diffDay = DateUtils.diffDay(slippageEntity.getLateFeeStartTime(), endTime);
                // 计算滞纳金金额
                slippageUpdateEntity.setLateFeePay(NumberUtil.mul(diffDay, slippageEntity.getLateFee()));
                
                CarRentalPackageOrderFreezePo orderFreezePo = carRentalPackageOrderFreezeService.selectLastFreeByUid(slippageEntity.getUid());
                if (ObjectUtils.isNotEmpty(orderFreezePo) && RentalPackageOrderFreezeStatusEnum.AUDIT_PASS.getCode().equals(orderFreezePo.getStatus())) {
                    // 1. 更改订单冻结表数据
                    carRentalPackageOrderFreezeService
                            .enableFreezeRentOrderByUidAndPackageOrderNo(slippageEntity.getRentalPackageOrderNo(), slippageEntity.getUid(), false, optUid);
                }
                
                // 赋值会员更新
                CarRentalPackageMemberTermPo memberTermUpdateEntity = new CarRentalPackageMemberTermPo();
                memberTermUpdateEntity.setStatus(MemberTermStatusEnum.NORMAL.getCode());
                memberTermUpdateEntity.setId(memberTermEntity.getId());
                memberTermUpdateEntity.setUpdateUid(optUid);
                memberTermUpdateEntity.setUpdateTime(now);
                
                // 计算差额
                Pair<Boolean, Long> diffTimePair = FreezeTimeUtils.calculateRemainingFrozenDays(freezeEntity.getApplyTerm(), freezeEntity.getAuditTime(), now);
                // 如果当前滞纳金缴纳时间已经超过了冻结启用时间，则非提前启用，无需减去剩余未使用的冻结天数
                if (diffTimePair.getLeft()) {
                    memberTermUpdateEntity.setDueTime(memberTermEntity.getDueTime() - diffTimePair.getRight());
                    memberTermUpdateEntity.setDueTimeTotal(memberTermEntity.getDueTimeTotal() - diffTimePair.getRight());
                }
                
                carRentalPackageMemberTermService.updateById(memberTermUpdateEntity);
                
                jt808Flag.set(true);
            }
            carRentalPackageOrderSlippageService.updateById(slippageUpdateEntity);
        });
        
        // JT808
        if (jt808Flag.get()) {
            // 查询车辆
            ElectricityCar electricityCar = carService.selectByUid(freezeEntity.getTenantId(), freezeEntity.getUid());
            if (ObjectUtils.isNotEmpty(electricityCar)) {
                // JT808解锁
                UserInfo userInfo = userInfoService.queryByUidFromCache(freezeEntity.getUid());
                CarLockCtrlHistory carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo);
                // 生成日志
                if (ObjectUtils.isNotEmpty(carLockCtrlHistory)) {
                    carLockCtrlHistoryService.insert(carLockCtrlHistory);
                }
            }
        }
        
    }
    
    
    /**
     * 构建JT808
     *
     * @param electricityCar
     * @param userInfo
     * @return
     */
    private CarLockCtrlHistory buildCarLockCtrlHistory(ElectricityCar electricityCar, UserInfo userInfo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {
            
            boolean result = carRentalOrderBizService.retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);
            
            CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
            carLockCtrlHistory.setUid(userInfo.getUid());
            carLockCtrlHistory.setName(userInfo.getName());
            carLockCtrlHistory.setPhone(userInfo.getPhone());
            carLockCtrlHistory.setStatus(result ? CarLockCtrlHistory.STATUS_UN_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_UN_LOCK_FAIL);
            carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
            carLockCtrlHistory.setCarModel(electricityCar.getModel());
            carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
            carLockCtrlHistory.setCarSn(electricityCar.getSn());
            carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
            carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
            carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
            carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_SLIPPAGE_UN_LOCK);
            
            return carLockCtrlHistory;
        }
        return null;
    }
    
    /**
     * 根据用户ID查询车辆租赁套餐订单未支付的滞纳金金额
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 滞纳金金额，保留两位小数，四舍五入
     */
    @Override
    public BigDecimal queryCarPackageUnpaidAmountByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        List<CarRentalPackageOrderSlippagePo> slippageEntityList = carRentalPackageOrderSlippageService.selectUnPayByByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(slippageEntityList)) {
            return null;
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CarRentalPackageOrderSlippagePo slippageEntity : slippageEntityList) {
            long now = System.currentTimeMillis();
            // 结束时间，不为空
            if (ObjectUtils.isNotEmpty(slippageEntity.getLateFeeEndTime())) {
                now = slippageEntity.getLateFeeEndTime();
            }
            
            // 时间比对
            long lateFeeStartTime = slippageEntity.getLateFeeStartTime();
            
            // 转换天
            long diffDay = DateUtils.diffDay(lateFeeStartTime, now);
            // 计算滞纳金金额
            BigDecimal amount = NumberUtil.mul(diffDay, slippageEntity.getLateFee());
            totalAmount = totalAmount.add(amount);
        }
        
        if (BigDecimal.ZERO.compareTo(totalAmount) == 0) {
            return null;
        }
        
        return totalAmount.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 是否存在未支付的滞纳金<br /> 租车(单车、车电一体)
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return true(存在)、false(不存在)
     */
    @Override
    public boolean isExitUnpaid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderSlippageService.isExitUnpaid(tenantId, uid);
    }
    
    @Override
    public Map<Long, BigDecimal> listCarPackageUnpaidAmountByUidList(Integer tenantId, List<Long> uidList) {
        if (Objects.isNull(tenantId) || !CollectionUtils.isEmpty(uidList)) {
            return null;
        }
    
        List<CarRentalPackageOrderSlippagePo> slippageEntityList = carRentalPackageOrderSlippageService.listUnPayByByUidList(tenantId, uidList);
        if (CollectionUtils.isEmpty(slippageEntityList)) {
            return null;
        }
    
        Map<Long, List<CarRentalPackageOrderSlippagePo>> slippageEntityMap = slippageEntityList.stream().collect(Collectors.groupingBy(CarRentalPackageOrderSlippagePo::getUid));
        if (MapUtils.isEmpty(slippageEntityMap)) {
            return null;
        }
    
        Map<Long, BigDecimal> resultMap = new HashMap<>();
    
        slippageEntityMap.forEach((uid, slippageList) -> {
            if (CollectionUtils.isEmpty(slippageList)) {
                resultMap.put(uid, null);
                return;
            }
    
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CarRentalPackageOrderSlippagePo slippageEntity : slippageList) {
                long now = System.currentTimeMillis();
                // 结束时间，不为空
                if (ObjectUtils.isNotEmpty(slippageEntity.getLateFeeEndTime())) {
                    now = slippageEntity.getLateFeeEndTime();
                }
            
                // 时间比对
                long lateFeeStartTime = slippageEntity.getLateFeeStartTime();
            
                // 转换天
                long diffDay = DateUtils.diffDay(lateFeeStartTime, now);
                // 计算滞纳金金额
                BigDecimal amount = NumberUtil.mul(diffDay, slippageEntity.getLateFee());
                totalAmount = totalAmount.add(amount);
            }
    
            resultMap.put(uid, BigDecimal.ZERO.compareTo(totalAmount) == 0 ? null : totalAmount.setScale(2, RoundingMode.HALF_UP));
        });
    
        return resultMap;
    }
}
