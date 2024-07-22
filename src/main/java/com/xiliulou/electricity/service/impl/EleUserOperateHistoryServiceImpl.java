package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleUserOperateHistory;
import com.xiliulou.electricity.entity.UserPhoneModifyRecord;
import com.xiliulou.electricity.mapper.EleUserOperateHistoryMapper;
import com.xiliulou.electricity.query.EleUserOperateHistoryQueryModel;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.CarLockCtrlHistoryService;
import com.xiliulou.electricity.service.CouponIssueOperateRecordService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.EleBindCarRecordService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.EleUserOperateHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.EnableMemberCardRecordService;
import com.xiliulou.electricity.service.FranchiseeMoveRecordService;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.LoginInfoService;
import com.xiliulou.electricity.service.MaintenanceRecordService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.UserActiveInfoService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserPhoneModifyRecordService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.VerificationCodeService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Description: EleUserOperateHistoryServiceImpl
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2023/12/28 14:28
 */
@Service
@Slf4j
public class EleUserOperateHistoryServiceImpl implements EleUserOperateHistoryService {
    
    
    TtlXllThreadPoolExecutorServiceWrapper eleUserOperateHistoryService = TtlXllThreadPoolExecutorsSupport
            .get(XllThreadPoolExecutors.newFixedThreadPool("ELE_USER_OPERATE_HISTORY_POOL", 2, "ele_user_operate_history_thread"));
    
    @Resource
    private EleUserOperateHistoryMapper historyMapper;
    
    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    CarLockCtrlHistoryService carLockCtrlHistoryService;
    
    @Autowired
    CouponIssueOperateRecordService couponIssueOperateRecordService;
    
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    
    @Autowired
    EleBindCarRecordService eleBindCarRecordService;
    
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    
    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    ElectricityCarService electricityCarService;
    
    @Autowired
    EnableMemberCardRecordService enableMemberCardRecordService;
    
    @Autowired
    EnterpriseInfoService enterpriseInfoService;
    
    @Autowired
    FranchiseeMoveRecordService franchiseeMoveRecordService;
    
    @Autowired
    FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @Autowired
    FreeDepositOrderService freeDepositOrderService;
    
    @Autowired
    InsuranceOrderService insuranceOrderService;
    
    @Autowired
    LoginInfoService loginInfoService;
    
    @Autowired
    MaintenanceRecordService maintenanceRecordService;
    
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;
    
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    UserActiveInfoService userActiveInfoService;
    
    @Autowired
    UserCouponService userCouponService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    VerificationCodeService verificationCodeService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    UserPhoneModifyRecordService userPhoneModifyRecordService;
    
    @Override
    public void insertOne(EleUserOperateHistory EleUserOperateHistory) {
        historyMapper.insertOne(EleUserOperateHistory);
    }
    
    @Override
    public void asyncHandleEleUserOperateHistory(EleUserOperateHistory eleUserOperateHistory) {
        eleUserOperateHistoryService.execute(() -> {
            insertOne(eleUserOperateHistory);
        });
    }
    
    @Override
    public void asyncHandleUpdateUserPhone(Integer tenantId, Long uid, String newPhone, String oldPhone) {
        eleUserOperateHistoryService.execute(() -> {
            UserPhoneModifyRecord userPhoneModifyRecord = new UserPhoneModifyRecord();
            userPhoneModifyRecord.setUid(uid);
            userPhoneModifyRecord.setTenantId(tenantId);
            userPhoneModifyRecord.setOldPhone(oldPhone);
            userPhoneModifyRecord.setNewPhone(newPhone);
            userPhoneModifyRecord.setCreateTime(System.currentTimeMillis());
            userPhoneModifyRecord.setUpdateTime(System.currentTimeMillis());
            userPhoneModifyRecordService.insertOne(userPhoneModifyRecord);
          /*  log.info("start update user phone,uid={}, newPhone={}",uid,newPhone);
            batteryMembercardRefundOrderService.updatePhoneByUid(tenantId, uid, newPhone);
            carDepositOrderService.updatePhoneByUid(tenantId, uid, newPhone);
            carLockCtrlHistoryService.updatePhoneByUid(tenantId, uid, newPhone);
            carRefundOrderService.updatePhoneByUid(tenantId, uid, newPhone);
            couponIssueOperateRecordService.updatePhoneByUid(tenantId, uid, newPhone);
            eleBatteryServiceFeeOrderService.updatePhoneByUid(tenantId, uid, newPhone);
            eleBindCarRecordService.updatePhoneByOldPhone(tenantId, oldPhone, newPhone);
            eleDepositOrderService.updatePhoneByUid(tenantId, uid, newPhone);
            eleDisableMemberCardRecordService.updatePhoneByUid(tenantId, uid, newPhone);
            electricityCabinetOrderService.updatePhoneByUid(tenantId, uid, newPhone);
            
            ElectricityCar electricityCar = electricityCarService.queryInfoByUid(uid);
            Integer carUpdate = electricityCarService.updatePhoneByUid(tenantId, uid, newPhone);
            DbUtils.dbOperateSuccessThenHandleCache(carUpdate, i -> {
                if (Objects.nonNull(electricityCar)) {
                    log.info("delete electricity car cache");
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId());
                }
            });
            
            enableMemberCardRecordService.updatePhoneByUid(tenantId, uid, newPhone);
            
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.selectByUid(uid);
            enterpriseInfoService.updatePhoneByUid(tenantId, uid, newPhone);
            DbUtils.dbOperateSuccessThenHandleCache(carUpdate, i -> {
                if (Objects.nonNull(enterpriseInfo)) {
                    log.info("delete enterpriseInfo cache");
                    redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + enterpriseInfo.getId());
                }
            });
            
            franchiseeMoveRecordService.updatePhoneByUid(tenantId, uid, newPhone);
            freeDepositAlipayHistoryService.updatePhoneByUid(tenantId, uid, newPhone);
            freeDepositOrderService.updatePhoneByUid(tenantId, uid, newPhone);
            insuranceOrderService.updatePhoneByUid(tenantId, uid, newPhone);
            maintenanceRecordService.updatePhoneByUid(tenantId, uid, newPhone);
            
            rentBatteryOrderService.updatePhoneByUid(tenantId, uid, newPhone);
            
            userActiveInfoService.updatePhoneByUid(tenantId, uid, newPhone);
            DbUtils.dbOperateSuccessThenHandleCache(carUpdate, i -> {
                log.info("delete userActiveInfoService cache");
                redisService.delete(CacheConstant.USER_ACTIVE_INFO_CACHE + uid);
            });
            
            userCouponService.updatePhoneByUid(tenantId, uid, newPhone);
          //  verificationCodeService.updatePhoneByUid(tenantId, uid, newPhone);
            log.info("end update user phone");*/
        });
    }
    
    @Override
    public R listEleUserOperateHistory(EleUserOperateHistoryQueryModel queryModel) {
        return R.ok(historyMapper.selectListUserOperateHistory(queryModel));
    }
    
}