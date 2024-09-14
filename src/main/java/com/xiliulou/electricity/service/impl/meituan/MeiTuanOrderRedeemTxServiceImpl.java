package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.bo.meituan.MeiTuanOrderRedeemRollBackBO;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.UserOperateRecordConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleUserOperateRecordService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.meituan.MeiTuanOrderRedeemTxService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/13 08:59:32
 */
@Slf4j
@Service
public class MeiTuanOrderRedeemTxServiceImpl implements MeiTuanOrderRedeemTxService {
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private EleDepositOrderService depositOrderService;
    
    @Resource
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    @Resource
    private UserBatteryTypeService userBatteryTypeService;
    
    @Resource
    private UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    private ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Resource
    private EleUserOperateRecordService eleUserOperateRecordService;
    
    @Resource
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> saveUserInfoAndOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            UserBatteryMemberCard userBatteryMemberCard, MeiTuanRiderMallOrder meiTuanRiderMallOrder) {
        EleDepositOrder eleDepositOrder = null;
        ElectricityMemberCardOrder electricityMemberCardOrder = null;
        UserInfo rollBackUserInfo = null;
        List<UserBatteryType> userBatteryTypes = null;
        UserBatteryDeposit userBatteryDeposit = null;
        UserBatteryDeposit rollBackUserBatteryDeposit = null;
        UserBatteryMemberCard userBatteryMemberCardUpdate = null;
        UserBatteryMemberCard rollBackUserBatteryMemberCard = null;
        ServiceFeeUserInfo serviceFeeUserInfo = null;
        ServiceFeeUserInfo rollBackServiceFeeUserInfo = null;
        EleUserOperateRecord eleUserDepositOperateRecord = null;
        EleUserOperateRecord eleUserMemberCardOperateRecord = null;
        MeiTuanOrderRedeemRollBackBO rollBackBO = null;
        Long eleDepositOrderById = null;
        Long electricityMemberCardOrderById = null;
        Long userBatteryDepositById = null;
        Long userBatteryMemberCardUpdateById = null;
        Long serviceFeeUserInfoById = null;
        Long eleUserDepositOperateRecordById = null;
        Long eleUserMemberCardOperateRecordById = null;
        
        try {
            BigDecimal deposit = batteryMemberCard.getDeposit();
            eleDepositOrder = EleDepositOrder.builder().orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid())).uid(userInfo.getUid())
                    .phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(deposit).status(EleDepositOrder.STATUS_SUCCESS).createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId())
                    .payType(EleDepositOrder.MEITUAN_DEPOSIT_PAYMENT).storeId(null).mid(batteryMemberCard.getId()).modelType(0).build();
            depositOrderService.insert(eleDepositOrder);
            
            // 封装eleDepositOrder回滚数据
            if (Objects.nonNull(eleDepositOrder.getId())) {
                eleDepositOrderById = eleDepositOrder.getId();
            }
            
            electricityMemberCardOrder = ElectricityMemberCardOrder.builder().orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()))
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).status(ElectricityMemberCardOrder.STATUS_SUCCESS)
                    .memberCardId(batteryMemberCard.getId()).uid(userInfo.getUid()).maxUseCount(batteryMemberCard.getUseCount()).cardName(batteryMemberCard.getName())
                    .payAmount(meiTuanRiderMallOrder.getMeiTuanActuallyPayPrice()).userName(userInfo.getName()).validDays(batteryMemberCard.getValidDays())
                    .tenantId(batteryMemberCard.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId())
                    .payCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1).payType(ElectricityMemberCardOrder.MEITUAN_PAYMENT).refId(null)
                    .sendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null)
                    .useStatus(ElectricityMemberCardOrder.USE_STATUS_USING).source(ElectricityMemberCardOrder.SOURCE_NOT_SCAN).storeId(null)
                    .couponIds(batteryMemberCard.getCouponIds()).build();
            electricityMemberCardOrderService.insert(electricityMemberCardOrder);
            
            // 封装electricityMemberCardOrder回滚数据
            if (Objects.nonNull(electricityMemberCardOrder.getId())) {
                electricityMemberCardOrderById = electricityMemberCardOrder.getId();
            }
            
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(userInfo.getUid());
            userInfoUpdate.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            if (Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L)) {
                userInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            }
            if (Objects.equals(userInfo.getStoreId(), NumberConstant.ZERO_L)) {
                userInfoUpdate.setStoreId(eleDepositOrder.getStoreId());
            }
            userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);
            
            //封装UserInfo回滚
            rollBackUserInfo = UserInfo.builder().uid(userInfo.getUid()).batteryDepositStatus(userInfo.getBatteryDepositStatus()).franchiseeId(userInfo.getFranchiseeId())
                    .storeId(userInfo.getStoreId()).payCount(userInfo.getPayCount()).updateTime(userInfo.getUpdateTime()).build();
            
            List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
            if (CollectionUtils.isNotEmpty(batteryTypeList)) {
                List<String> userBatteryTypeList = userBatteryTypeService.selectByUid(userInfo.getUid());
                if (CollectionUtils.isNotEmpty(userBatteryTypeList)) {
                    batteryTypeList = batteryTypeList.stream().filter(t -> !userBatteryTypeList.contains(t)).collect(Collectors.toList());
                }
                
                userBatteryTypes = userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo);
                userBatteryTypeService.batchInsert(userBatteryTypes);
            }
            
            userBatteryDeposit = new UserBatteryDeposit();
            userBatteryDeposit.setUid(userInfo.getUid());
            userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
            userBatteryDeposit.setDid(eleDepositOrder.getMid());
            userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
            userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
            userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
            userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
            if (!Objects.equals(batteryMemberCard.getDeposit(), deposit)) {
                userBatteryDeposit.setDepositModifyFlag(UserBatteryDeposit.DEPOSIT_MODIFY_YES);
                userBatteryDeposit.setBeforeModifyDeposit(batteryMemberCard.getDeposit());
            }
            
            UserBatteryDeposit existUserBatteryDeposit = userBatteryDepositService.queryByUid(userInfo.getUid());
            if (Objects.nonNull(existUserBatteryDeposit)) {
                userBatteryDepositService.update(userBatteryDeposit);
                
                // 封装UserBatteryDeposit回滚
                rollBackUserBatteryDeposit = UserBatteryDeposit.builder().uid(userBatteryDeposit.getUid()).orderId(userBatteryDeposit.getOrderId()).did(userBatteryDeposit.getDid())
                        .batteryDeposit(userBatteryDeposit.getBatteryDeposit()).applyDepositTime(userBatteryDeposit.getApplyDepositTime())
                        .depositType(userBatteryDeposit.getDepositType()).delFlag(userBatteryDeposit.getDelFlag()).updateTime(userBatteryDeposit.getUpdateTime())
                        .depositModifyFlag(userBatteryDeposit.getDepositModifyFlag()).beforeModifyDeposit(userBatteryDeposit.getBeforeModifyDeposit()).build();
            } else {
                userBatteryDeposit.setCreateTime(System.currentTimeMillis());
                userBatteryDepositService.insert(userBatteryDeposit);
                
                // 封装UserBatteryDeposit回滚
                if (Objects.nonNull(userBatteryDeposit.getId())) {
                    userBatteryDepositById = userBatteryDeposit.getId();
                }
            }
            
            userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(electricityMemberCardOrder.getUid());
            userBatteryMemberCardUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
            userBatteryMemberCardUpdate.setOrderExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
            userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
            userBatteryMemberCardUpdate.setOrderRemainingNumber(batteryMemberCard.getUseCount());
            userBatteryMemberCardUpdate.setRemainingNumber(batteryMemberCard.getUseCount());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setMemberCardId(electricityMemberCardOrder.getMemberCardId());
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
            userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
            
            if (Objects.isNull(userBatteryMemberCard)) {
                userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
                userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
                
                // 封装UserBatteryMemberCard的回滚
                if (Objects.nonNull(userBatteryMemberCardUpdate.getId())) {
                    userBatteryMemberCardUpdateById = userBatteryMemberCardUpdate.getId();
                }
            } else {
                userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
                
                // 封装UserBatteryMemberCard的回滚
                rollBackUserBatteryMemberCard = UserBatteryMemberCard.builder().uid(userBatteryMemberCard.getUid()).orderId(userBatteryMemberCard.getOrderId())
                        .orderExpireTime(userBatteryMemberCard.getOrderExpireTime()).orderEffectiveTime(userBatteryMemberCard.getOrderEffectiveTime())
                        .memberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime()).orderRemainingNumber(userBatteryMemberCard.getRemainingNumber())
                        .remainingNumber(userBatteryMemberCard.getRemainingNumber()).memberCardStatus(userBatteryMemberCard.getMemberCardStatus())
                        .memberCardId(userBatteryMemberCard.getMemberCardId()).delFlag(userBatteryMemberCard.getDelFlag()).updateTime(userBatteryMemberCard.getUpdateTime())
                        .tenantId(userBatteryMemberCard.getTenantId()).cardPayCount(userBatteryMemberCard.getCardPayCount()).build();
            }
            
            serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
            ServiceFeeUserInfo serviceFeeUserInfoInsert = new ServiceFeeUserInfo();
            serviceFeeUserInfoInsert.setServiceFeeGenerateTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
            serviceFeeUserInfoInsert.setUid(userBatteryMemberCardUpdate.getUid());
            serviceFeeUserInfoInsert.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            serviceFeeUserInfoInsert.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoInsert.setTenantId(electricityMemberCardOrder.getTenantId());
            serviceFeeUserInfoInsert.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
            serviceFeeUserInfoInsert.setDisableMemberCardNo("");
            
            if (Objects.nonNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsert);
                
                // 封装ServiceFeeUserInfo的回滚
                rollBackServiceFeeUserInfo = ServiceFeeUserInfo.builder().uid(serviceFeeUserInfo.getUid()).serviceFeeGenerateTime(serviceFeeUserInfo.getServiceFeeGenerateTime())
                        .franchiseeId(serviceFeeUserInfo.getFranchiseeId()).updateTime(serviceFeeUserInfo.getUpdateTime()).tenantId(serviceFeeUserInfo.getTenantId())
                        .delFlag(serviceFeeUserInfo.getDelFlag()).disableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo()).build();
            } else {
                serviceFeeUserInfoInsert.setCreateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.insert(serviceFeeUserInfoInsert);
                
                // 封装ServiceFeeUserInfo的回滚
                if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getId())) {
                    serviceFeeUserInfoById = serviceFeeUserInfo.getId();
                }
            }
            
            eleUserDepositOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.DEPOSIT_MODEL).operateContent(EleUserOperateRecord.DEPOSIT_MODEL)
                    .operateUid(SecurityUtils.getUid()).uid(eleDepositOrder.getUid())
                    .name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername()).oldBatteryDeposit(null)
                    .newBatteryDeposit(eleDepositOrder.getPayAmount()).tenantId(TenantContextHolder.getTenantId()).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY)
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserDepositOperateRecord);
            
            // 封装eleUserDepositOperateRecord回滚数据
            if (Objects.nonNull(eleUserDepositOperateRecord) && Objects.nonNull(eleUserDepositOperateRecord.getId())) {
                eleUserDepositOperateRecordById = eleUserDepositOperateRecord.getId().longValue();
            }
            
            double oldValidDays = 0.0;
            double newValidDays = 0.0;
            Long oldMaxUseCount = null;
            Long newMaxUseCount = null;
            if (Objects.nonNull(userBatteryMemberCard)) {
                if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && !Objects.equals(userBatteryMemberCard.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
                    // oldValidDays = Math.toIntExact(((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
                    oldValidDays = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
                }
                
                // 设置限次 不限次
                if (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
                    oldMaxUseCount = userBatteryMemberCard.getRemainingNumber();
                } else {
                    oldMaxUseCount = UserOperateRecordConstant.UN_LIMIT_COUNT_REMAINING_NUMBER;
                }
            }
            
            // newValidDays = Math.toIntExact(((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
            newValidDays = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
            
            // 设置限次 不限次
            if (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
                newMaxUseCount = userBatteryMemberCardUpdate.getRemainingNumber();
            } else {
                newMaxUseCount = UserOperateRecordConstant.UN_LIMIT_COUNT_REMAINING_NUMBER;
            }
            
            eleUserMemberCardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                    .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                    .uid(electricityMemberCardOrder.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername())
                    .oldValidDays((int) oldValidDays).newValidDays((int) newValidDays).oldMaxUseCount(oldMaxUseCount).newMaxUseCount(newMaxUseCount)
                    .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserMemberCardOperateRecord);
            
            // 封装eleUserMemberCardOperateRecord回滚数据
            if (Objects.nonNull(eleUserMemberCardOperateRecord) && Objects.nonNull(eleUserDepositOperateRecord.getId())) {
                eleUserMemberCardOperateRecordById = eleUserMemberCardOperateRecord.getId().longValue();
            }
        } catch (Exception e) {
            log.error("MeiTuan order redeem fail! saveUserInfoAndOrder uid={}, meiTuanOrderId={}", userInfo.getUid(), meiTuanRiderMallOrder.getMeiTuanOrderId(), e);
            throw new CustomBusinessException(e.getMessage());
        }
        
        rollBackBO = buildRollBackData(eleDepositOrderById, electricityMemberCardOrderById, null, rollBackUserInfo, userBatteryTypes, userBatteryDepositById,
                rollBackUserBatteryDeposit, userBatteryMemberCardUpdateById, rollBackUserBatteryMemberCard, serviceFeeUserInfoById, rollBackServiceFeeUserInfo,
                eleUserDepositOperateRecordById, eleUserMemberCardOperateRecordById, null, null);
        
        return Pair.of(electricityMemberCardOrder, rollBackBO);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> bindUserMemberCard(UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            MeiTuanRiderMallOrder meiTuanRiderMallOrder) {
        ElectricityMemberCardOrder memberCardOrder = null;
        UserBatteryMemberCard userBatteryMemberCardUpdate = null;
        List<UserBatteryType> userBatteryTypes = null;
        ServiceFeeUserInfo serviceFeeUserInfo = null;
        ServiceFeeUserInfo rollBackServiceFeeUserInfo = null;
        UserInfo rollBackUserInfo = null;
        EleUserOperateRecord eleUserMemberCardOperateRecord = null;
        MeiTuanOrderRedeemRollBackBO rollBackBO = null;
        Long electricityMemberCardOrderById = null;
        Long userBatteryMemberCardUpdateById = null;
        Long serviceFeeUserInfoById = null;
        Long eleUserMemberCardOperateRecordId = null;
        
        try {
            memberCardOrder = new ElectricityMemberCardOrder();
            memberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
            memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            memberCardOrder.setMemberCardId(batteryMemberCard.getId());
            memberCardOrder.setUid(userInfo.getUid());
            memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
            memberCardOrder.setCardName(batteryMemberCard.getName());
            memberCardOrder.setPayAmount(meiTuanRiderMallOrder.getMeiTuanActuallyPayPrice());
            memberCardOrder.setPayType(ElectricityMemberCardOrder.MEITUAN_PAYMENT);
            memberCardOrder.setPayCount(1);
            memberCardOrder.setUserName(userInfo.getName());
            memberCardOrder.setValidDays(batteryMemberCard.getValidDays());
            memberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
            memberCardOrder.setStoreId(userInfo.getStoreId());
            memberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
            memberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
            memberCardOrder.setTenantId(userInfo.getTenantId());
            memberCardOrder.setCreateTime(System.currentTimeMillis());
            memberCardOrder.setUpdateTime(System.currentTimeMillis());
            memberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
            memberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
            electricityMemberCardOrderService.insert(memberCardOrder);
            
            // 封装memberCardOrder回滚数据
            if (Objects.nonNull(memberCardOrder.getId())) {
                electricityMemberCardOrderById = memberCardOrder.getId();
            }
            
            userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(memberCardOrder.getUid());
            userBatteryMemberCardUpdate.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardUpdate.setOrderExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setOrderRemainingNumber(batteryMemberCard.getUseCount());
            userBatteryMemberCardUpdate.setRemainingNumber(batteryMemberCard.getUseCount());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setMemberCardId(memberCardOrder.getMemberCardId());
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setTenantId(memberCardOrder.getTenantId());
            userBatteryMemberCardUpdate.setCardPayCount(1);
            userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
            
            // 封装userBatteryMemberCardUpdate回滚数据
            if (Objects.nonNull(userBatteryMemberCardUpdate.getId())) {
                userBatteryMemberCardUpdateById = userBatteryMemberCardUpdate.getId();
            }
            
            List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
            if (CollectionUtils.isNotEmpty(batteryTypeList)) {
                List<String> userBatteryTypeList = userBatteryTypeService.selectByUid(userInfo.getUid());
                if (CollectionUtils.isNotEmpty(userBatteryTypeList)) {
                    batteryTypeList = batteryTypeList.stream().filter(t -> !userBatteryTypeList.contains(t)).collect(Collectors.toList());
                }
                
                if (CollectionUtils.isNotEmpty(batteryTypeList)) {
                    userBatteryTypes = userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo);
                    userBatteryTypeService.batchInsert(userBatteryTypes);
                }
            }
            
            serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
            ServiceFeeUserInfo serviceFeeUserInfoInsert = new ServiceFeeUserInfo();
            serviceFeeUserInfoInsert.setServiceFeeGenerateTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            serviceFeeUserInfoInsert.setUid(userBatteryMemberCardUpdate.getUid());
            serviceFeeUserInfoInsert.setFranchiseeId(memberCardOrder.getFranchiseeId());
            serviceFeeUserInfoInsert.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoInsert.setTenantId(memberCardOrder.getTenantId());
            serviceFeeUserInfoInsert.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
            serviceFeeUserInfoInsert.setDisableMemberCardNo("");
            
            if (Objects.nonNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsert);
                
                // 封装serviceFeeUserInfo回滚数据
                rollBackServiceFeeUserInfo = ServiceFeeUserInfo.builder().uid(serviceFeeUserInfo.getUid()).serviceFeeGenerateTime(serviceFeeUserInfo.getServiceFeeGenerateTime())
                        .franchiseeId(serviceFeeUserInfo.getFranchiseeId()).updateTime(serviceFeeUserInfo.getUpdateTime()).tenantId(serviceFeeUserInfo.getTenantId())
                        .delFlag(serviceFeeUserInfo.getDelFlag()).disableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo()).build();
            } else {
                serviceFeeUserInfoInsert.setCreateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.insert(serviceFeeUserInfoInsert);
                
                // 封装serviceFeeUserInfo回滚数据
                if (Objects.nonNull(serviceFeeUserInfoInsert.getId())) {
                    serviceFeeUserInfoById = serviceFeeUserInfoInsert.getId();
                }
            }
            
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(userInfo.getUid());
            userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);
            
            //封装UserInfo回滚
            rollBackUserInfo = UserInfo.builder().uid(userInfo.getUid()).payCount(userInfo.getPayCount()).updateTime(userInfo.getUpdateTime()).build();
            
            double newValidDays = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
            
            eleUserMemberCardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                    .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                    .uid(userInfo.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername()).oldValidDays(0)
                    .newValidDays((int) newValidDays).oldMaxUseCount(0L).newMaxUseCount(userBatteryMemberCardUpdate.getRemainingNumber())
                    .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserMemberCardOperateRecord);
            
            // 封装eleUserMemberCardOperateRecord回滚数据
            if (Objects.nonNull(eleUserMemberCardOperateRecord) && Objects.nonNull(eleUserMemberCardOperateRecord.getId())) {
                eleUserMemberCardOperateRecordId = eleUserMemberCardOperateRecord.getId().longValue();
            }
            
        } catch (Exception e) {
            log.error("MeiTuan order redeem fail! bindUserMemberCard uid={}, meiTuanOrderId={}", userInfo.getUid(), meiTuanRiderMallOrder.getMeiTuanOrderId(), e);
            throw new CustomBusinessException(e.getMessage());
        }
        rollBackBO = buildRollBackData(null, electricityMemberCardOrderById, null, rollBackUserInfo, userBatteryTypes, null, null, userBatteryMemberCardUpdateById, null,
                serviceFeeUserInfoById, rollBackServiceFeeUserInfo, null, eleUserMemberCardOperateRecordId, null, null);
        
        return Pair.of(memberCardOrder, rollBackBO);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> saveRenewalUserBatteryMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard userBindBatteryMemberCard, MeiTuanRiderMallOrder meiTuanRiderMallOrder,
            List<String> userBindBatteryTypes, List<String> memberCardBatteryTypes) {
        ElectricityMemberCardOrder memberCardOrder = null;
        UserBatteryMemberCard rollBackUserBatteryMemberCard = new UserBatteryMemberCard();
        ElectricityMemberCardOrder rollBackElectricityMemberCardOrder = null;
        List<UserBatteryType> insertUserBatteryTypeListForRollBack = null;
        List<UserBatteryType> userBatteryTypes = null;
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = null;
        ServiceFeeUserInfo rollBackServiceFeeUserInfo = null;
        UserInfo rollBackUserInfo = null;
        EleUserOperateRecord eleUserMembercardOperateRecord = null;
        MeiTuanOrderRedeemRollBackBO rollBackBO = null;
        Long electricityMemberCardOrderById = null;
        Long serviceFeeUserInfoById = null;
        Long eleUserMemberCardOperateRecordById = null;
        Long userBatteryMemberCardPackageId = null;
        
        try {
            memberCardOrder = new ElectricityMemberCardOrder();
            memberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
            memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            memberCardOrder.setMemberCardId(batteryMemberCard.getId());
            memberCardOrder.setUid(userInfo.getUid());
            memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
            memberCardOrder.setCardName(batteryMemberCard.getName());
            memberCardOrder.setPayAmount(meiTuanRiderMallOrder.getMeiTuanActuallyPayPrice());
            memberCardOrder.setPayType(ElectricityMemberCardOrder.MEITUAN_PAYMENT);
            memberCardOrder.setPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
            memberCardOrder.setUserName(userInfo.getName());
            memberCardOrder.setValidDays(batteryMemberCard.getValidDays());
            memberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
            memberCardOrder.setStoreId(userInfo.getStoreId());
            memberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
            memberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
            memberCardOrder.setTenantId(userInfo.getTenantId());
            memberCardOrder.setCreateTime(System.currentTimeMillis());
            memberCardOrder.setUpdateTime(System.currentTimeMillis());
            memberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_NOT_USE);
            memberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
            
            UserBatteryMemberCard existUserBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)
                    || userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || Objects.isNull(userBindBatteryMemberCard) || (
                    Objects.equals(userBindBatteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0)) {
                memberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
                
                userBatteryMemberCardUpdate.setUid(userInfo.getUid());
                userBatteryMemberCardUpdate.setMemberCardId(batteryMemberCard.getId());
                userBatteryMemberCardUpdate.setOrderId(memberCardOrder.getOrderId());
                userBatteryMemberCardUpdate.setMemberCardExpireTime(
                        System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
                userBatteryMemberCardUpdate.setOrderExpireTime(
                        System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
                userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setOrderRemainingNumber(memberCardOrder.getMaxUseCount());
                userBatteryMemberCardUpdate.setRemainingNumber(memberCardOrder.getMaxUseCount());
                userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
                userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
                userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setTenantId(userInfo.getTenantId());
                userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
                
                // 封装UserBatteryMemberCard回滚数据
                rollBackUserBatteryMemberCard.setUid(userInfo.getUid());
                rollBackUserBatteryMemberCard.setMemberCardId(existUserBatteryMemberCard.getMemberCardId());
                rollBackUserBatteryMemberCard.setOrderId(existUserBatteryMemberCard.getOrderId());
                rollBackUserBatteryMemberCard.setMemberCardExpireTime(existUserBatteryMemberCard.getMemberCardExpireTime());
                rollBackUserBatteryMemberCard.setOrderExpireTime(existUserBatteryMemberCard.getOrderExpireTime());
                rollBackUserBatteryMemberCard.setOrderEffectiveTime(existUserBatteryMemberCard.getOrderEffectiveTime());
                rollBackUserBatteryMemberCard.setOrderRemainingNumber(existUserBatteryMemberCard.getOrderRemainingNumber());
                rollBackUserBatteryMemberCard.setRemainingNumber(existUserBatteryMemberCard.getRemainingNumber());
                rollBackUserBatteryMemberCard.setMemberCardStatus(existUserBatteryMemberCard.getMemberCardStatus());
                rollBackUserBatteryMemberCard.setDisableMemberCardTime(existUserBatteryMemberCard.getDisableMemberCardTime());
                rollBackUserBatteryMemberCard.setDelFlag(existUserBatteryMemberCard.getDelFlag());
                rollBackUserBatteryMemberCard.setCreateTime(existUserBatteryMemberCard.getCreateTime());
                rollBackUserBatteryMemberCard.setUpdateTime(existUserBatteryMemberCard.getUpdateTime());
                rollBackUserBatteryMemberCard.setTenantId(existUserBatteryMemberCard.getTenantId());
                rollBackUserBatteryMemberCard.setCardPayCount(existUserBatteryMemberCard.getCardPayCount());
                
                // 如果用户原来绑定的有套餐 套餐过期了，需要把原来绑定的套餐订单状态更新为已过期
                if (StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
                    ElectricityMemberCardOrder existElectricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
                    
                    ElectricityMemberCardOrder electricityMemberCardOrderUpdateUseStatus = new ElectricityMemberCardOrder();
                    electricityMemberCardOrderUpdateUseStatus.setOrderId(userBatteryMemberCard.getOrderId());
                    electricityMemberCardOrderUpdateUseStatus.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                    electricityMemberCardOrderUpdateUseStatus.setUpdateTime(System.currentTimeMillis());
                    electricityMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdateUseStatus);
                    
                    // 封装ElectricityMemberCardOrder回滚数据
                    if (Objects.nonNull(existElectricityMemberCardOrder)) {
                        rollBackElectricityMemberCardOrder = new ElectricityMemberCardOrder();
                        rollBackElectricityMemberCardOrder.setId(existElectricityMemberCardOrder.getId());
                        rollBackElectricityMemberCardOrder.setOrderId(userBatteryMemberCard.getOrderId());
                        rollBackElectricityMemberCardOrder.setUseStatus(existElectricityMemberCardOrder.getUseStatus());
                        rollBackElectricityMemberCardOrder.setUpdateTime(existElectricityMemberCardOrder.getUpdateTime());
                    }
                }
                
                // 更新用户电池型号
                Set<String> totalBatteryTypes = new HashSet<>();
                if (CollectionUtils.isNotEmpty(userBindBatteryTypes)) {
                    totalBatteryTypes.addAll(userBindBatteryTypes);
                }
                if (CollectionUtils.isNotEmpty(memberCardBatteryTypes)) {
                    totalBatteryTypes.addAll(memberCardBatteryTypes);
                }
                if (CollectionUtils.isNotEmpty(totalBatteryTypes)) {
                    // 封装UserBatteryType回滚数据
                    insertUserBatteryTypeListForRollBack = userBatteryTypeService.listByUid(memberCardOrder.getUid());
                    
                    userBatteryTypeService.deleteByUid(memberCardOrder.getUid());
                    
                    userBatteryTypes = userBatteryTypeService.buildUserBatteryType(new ArrayList<>(totalBatteryTypes), userInfo);
                    userBatteryTypeService.batchInsert(userBatteryTypes);
                }
            } else {
                UserBatteryMemberCardPackage userBatteryMemberCardPackage = new UserBatteryMemberCardPackage();
                userBatteryMemberCardPackage.setUid(userInfo.getUid());
                userBatteryMemberCardPackage.setMemberCardId(memberCardOrder.getMemberCardId());
                userBatteryMemberCardPackage.setOrderId(memberCardOrder.getOrderId());
                userBatteryMemberCardPackage.setRemainingNumber(batteryMemberCard.getUseCount());
                userBatteryMemberCardPackage.setMemberCardExpireTime(batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
                userBatteryMemberCardPackage.setTenantId(userInfo.getTenantId());
                userBatteryMemberCardPackage.setCreateTime(System.currentTimeMillis());
                userBatteryMemberCardPackage.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardPackageService.insert(userBatteryMemberCardPackage);
                
                // 封装UserBatteryMemberCardPackage回滚数据
                userBatteryMemberCardPackageId = userBatteryMemberCardPackage.getId();
                
                userBatteryMemberCardUpdate.setUid(userInfo.getUid());
                userBatteryMemberCardUpdate.setMemberCardExpireTime(
                        userBatteryMemberCard.getMemberCardExpireTime() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
                userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() + memberCardOrder.getMaxUseCount());
                userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                
                // 封装UserBatteryMemberCard回滚数据
                rollBackUserBatteryMemberCard.setUid(userInfo.getUid());
                rollBackUserBatteryMemberCard.setMemberCardExpireTime(existUserBatteryMemberCard.getMemberCardExpireTime());
                rollBackUserBatteryMemberCard.setRemainingNumber(existUserBatteryMemberCard.getRemainingNumber());
                rollBackUserBatteryMemberCard.setCardPayCount(existUserBatteryMemberCard.getCardPayCount());
                rollBackUserBatteryMemberCard.setUpdateTime(existUserBatteryMemberCard.getUpdateTime());
            }
            
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            
            ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
            
            serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
            serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
            serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
            serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            
            if (Objects.nonNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
                
                // 封装ServiceFeeUserInfo回滚数据
                rollBackServiceFeeUserInfo = new ServiceFeeUserInfo();
                rollBackServiceFeeUserInfo.setUid(userInfo.getUid());
                rollBackServiceFeeUserInfo.setTenantId(serviceFeeUserInfo.getTenantId());
                rollBackServiceFeeUserInfo.setServiceFeeGenerateTime(serviceFeeUserInfo.getServiceFeeGenerateTime());
                rollBackServiceFeeUserInfo.setUpdateTime(serviceFeeUserInfo.getUpdateTime());
            } else {
                serviceFeeUserInfoUpdate.setFranchiseeId(memberCardOrder.getFranchiseeId());
                serviceFeeUserInfoUpdate.setCreateTime(System.currentTimeMillis());
                serviceFeeUserInfoUpdate.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
                serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
                serviceFeeUserInfoService.insert(serviceFeeUserInfoUpdate);
                
                // 封装serviceFeeUserInfo回滚数据
                if (Objects.nonNull(serviceFeeUserInfoUpdate.getId())) {
                    serviceFeeUserInfoById = serviceFeeUserInfoUpdate.getId();
                }
            }
            
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(userInfo.getUid());
            userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);
            
            //封装UserInfo回滚数据
            rollBackUserInfo = UserInfo.builder().uid(userInfo.getUid()).payCount(userInfo.getPayCount()).updateTime(userInfo.getUpdateTime()).build();
            
            electricityMemberCardOrderService.insert(memberCardOrder);
            
            // 封装memberCardOrder回滚数据
            if (Objects.nonNull(memberCardOrder.getId())) {
                electricityMemberCardOrderById = memberCardOrder.getId();
            }
            
            double oldValidDays = 0.0;
            double newValidDays = 0.0;
            Long oldMaxUseCount = 0L;
            Long newMaxUseCount = 0L;
            if (Objects.nonNull(userBatteryMemberCard)) {
                if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && !Objects.equals(userBatteryMemberCard.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
                    // oldValidDays = Math.toIntExact(((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
                    // newValidDays = Math.toIntExact(((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
                    oldValidDays = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
                    newValidDays = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
                }
                oldMaxUseCount = userBatteryMemberCard.getRemainingNumber();
                newMaxUseCount = userBatteryMemberCardUpdate.getRemainingNumber();
            }
            
            eleUserMembercardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                    .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                    .uid(userInfo.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername()).oldValidDays((int) oldValidDays)
                    .newValidDays((int) newValidDays).oldMaxUseCount(oldMaxUseCount).newMaxUseCount(newMaxUseCount).tenantId(TenantContextHolder.getTenantId())
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserMembercardOperateRecord);
            
            // 封装eleUserMembercardOperateRecord回滚数据
            if (Objects.nonNull(eleUserMembercardOperateRecord) && Objects.nonNull(eleUserMembercardOperateRecord.getId())) {
                eleUserMemberCardOperateRecordById = eleUserMembercardOperateRecord.getId().longValue();
            }
        } catch (Exception e) {
            log.error("MeiTuan order redeem fail! saveRenewalUserBatteryMemberCardOrder uid={}, meiTuanOrderId={}", userInfo.getUid(), meiTuanRiderMallOrder.getMeiTuanOrderId(),
                    e);
            throw new CustomBusinessException(e.getMessage());
        }
        rollBackBO = buildRollBackData(null, electricityMemberCardOrderById, rollBackElectricityMemberCardOrder, rollBackUserInfo, userBatteryTypes, null, null, null,
                rollBackUserBatteryMemberCard, serviceFeeUserInfoById, rollBackServiceFeeUserInfo, null, eleUserMemberCardOperateRecordById, insertUserBatteryTypeListForRollBack,
                userBatteryMemberCardPackageId);
        
        return Pair.of(memberCardOrder, rollBackBO);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void rollback(MeiTuanOrderRedeemRollBackBO rollBackBO) {
        if (Objects.isNull(rollBackBO)) {
            return;
        }
        
        Long depositOrderId = rollBackBO.getDeleteDepositOrderById();
        if (Objects.nonNull(depositOrderId)) {
            depositOrderService.deleteById(depositOrderId);
        }
        
        Long memberCardOrderId = rollBackBO.getDeleteMemberCardOrderById();
        if (Objects.nonNull(memberCardOrderId)) {
            electricityMemberCardOrderService.deleteById(memberCardOrderId);
        }
        
        List<UserBatteryType> userBatteryTypeList = rollBackBO.getDeleteUserBatteryTypeList();
        if (CollectionUtils.isNotEmpty(userBatteryTypeList)) {
            Long uid = userBatteryTypeList.get(0).getUid();
            List<String> batteryTypes = userBatteryTypeList.stream().map(UserBatteryType::getBatteryType).collect(Collectors.toList());
            userBatteryTypeService.deleteByUidAndBatteryTypes(uid, batteryTypes);
        }
        
        Long userBatteryDepositId = rollBackBO.getDeleteUserBatteryDepositById();
        if (Objects.nonNull(userBatteryDepositId)) {
            userBatteryDepositService.deleteById(userBatteryDepositId);
        }
        
        Long userBatteryMemberCardId = rollBackBO.getDeleteUserBatteryMemberCardById();
        if (Objects.nonNull(userBatteryMemberCardId)) {
            userBatteryMemberCardService.deleteById(userBatteryMemberCardId);
        }
        
        Long serviceFeeUserInfoId = rollBackBO.getDeleteServiceFeeUserInfoById();
        if (Objects.nonNull(serviceFeeUserInfoId)) {
            serviceFeeUserInfoService.deleteById(serviceFeeUserInfoId);
        }
        
        Long eleUserOperateRecordDepositId = rollBackBO.getDeleteEleUserOperateRecordDepositById();
        if (Objects.nonNull(eleUserOperateRecordDepositId)) {
            eleUserOperateRecordService.deleteById(eleUserOperateRecordDepositId);
        }
        
        Long eleUserOperateRecordMemberCardId = rollBackBO.getDeleteEleUserOperateRecordMemberCardById();
        if (Objects.nonNull(eleUserOperateRecordMemberCardId)) {
            eleUserOperateRecordService.deleteById(eleUserOperateRecordMemberCardId);
        }
        
        List<UserBatteryType> insertUserBatteryTypeList = rollBackBO.getInsertUserBatteryTypeList();
        if (CollectionUtils.isNotEmpty(insertUserBatteryTypeList)) {
            userBatteryTypeService.batchInsert(insertUserBatteryTypeList);
        }
        
        ElectricityMemberCardOrder updateMemberCardOrder = rollBackBO.getRollBackElectricityMemberCardOrder();
        if (Objects.nonNull(updateMemberCardOrder)) {
            electricityMemberCardOrderService.updateByID(updateMemberCardOrder);
        }
        
        UserInfo updateUserInfo = rollBackBO.getRollBackUserInfo();
        if (Objects.nonNull(updateUserInfo)) {
            userInfoService.updateByUid(updateUserInfo);
        }
        
        UserBatteryDeposit updateUserBatteryDeposit = rollBackBO.getRollBackUserBatteryDeposit();
        if (Objects.nonNull(updateUserBatteryDeposit)) {
            userBatteryDepositService.update(updateUserBatteryDeposit);
        }
        
        UserBatteryMemberCard updateUserBatteryMemberCard = rollBackBO.getRollBackUserBatteryMemberCard();
        if (Objects.nonNull(updateUserBatteryMemberCard)) {
            userBatteryMemberCardService.updateByUid(updateUserBatteryMemberCard);
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = rollBackBO.getRollBackServiceFeeUserInfo();
        if (Objects.nonNull(serviceFeeUserInfo)) {
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfo);
        }
        
        Long userBatteryMemberCardPackageId = rollBackBO.getDeleteUserBatteryMemberCardPackageId();
        if (Objects.nonNull(userBatteryMemberCardPackageId)) {
            userBatteryMemberCardPackageService.deleteById(userBatteryMemberCardPackageId);
        }
    }
    
    private MeiTuanOrderRedeemRollBackBO buildRollBackData(Long eleDepositOrderById, Long electricityMemberCardOrderById,
            ElectricityMemberCardOrder rollBackElectricityMemberCardOrder, UserInfo rollBackUserInfo, List<UserBatteryType> userBatteryTypes, Long userBatteryDepositById,
            UserBatteryDeposit rollBackUserBatteryDeposit, Long userBatteryMemberCardUpdateById, UserBatteryMemberCard rollBackUserBatteryMemberCard, Long serviceFeeUserInfoById,
            ServiceFeeUserInfo rollBackServiceFeeUserInfo, Long eleUserDepositOperateRecordById, Long eleUserMemberCardOperateRecordById,
            List<UserBatteryType> insertUserBatteryTypeList, Long userBatteryMemberCardPackageId) {
        // 封装用户电池型号回滚
        List<UserBatteryType> deleteUserBatteryTypeList = null;
        if (CollectionUtils.isNotEmpty(userBatteryTypes)) {
            deleteUserBatteryTypeList = new ArrayList<>(userBatteryTypes.size());
            
            for (UserBatteryType userBatteryType : userBatteryTypes) {
                UserBatteryType rollBackUserBatteryType = UserBatteryType.builder().uid(userBatteryType.getUid()).batteryType(userBatteryType.getBatteryType())
                        .tenantId(userBatteryType.getTenantId()).delFlag(userBatteryType.getDelFlag()).createTime(userBatteryType.getCreateTime())
                        .updateTime(userBatteryType.getUpdateTime()).build();
                
                deleteUserBatteryTypeList.add(rollBackUserBatteryType);
            }
        }
        
        MeiTuanOrderRedeemRollBackBO rollBackBO = new MeiTuanOrderRedeemRollBackBO();
        if (Objects.nonNull(eleDepositOrderById)) {
            rollBackBO.setDeleteDepositOrderById(eleDepositOrderById);
        }
        if (Objects.nonNull(electricityMemberCardOrderById)) {
            rollBackBO.setDeleteMemberCardOrderById(electricityMemberCardOrderById);
        }
        if (Objects.nonNull(rollBackElectricityMemberCardOrder)) {
            rollBackBO.setRollBackElectricityMemberCardOrder(rollBackElectricityMemberCardOrder);
        }
        if (Objects.nonNull(rollBackUserInfo)) {
            rollBackBO.setRollBackUserInfo(rollBackUserInfo);
        }
        if (CollectionUtils.isNotEmpty(deleteUserBatteryTypeList)) {
            rollBackBO.setDeleteUserBatteryTypeList(deleteUserBatteryTypeList);
        }
        if (Objects.nonNull(userBatteryDepositById)) {
            rollBackBO.setDeleteUserBatteryDepositById(userBatteryDepositById);
        }
        if (Objects.nonNull(rollBackUserBatteryDeposit)) {
            rollBackBO.setRollBackUserBatteryDeposit(rollBackUserBatteryDeposit);
        }
        if (Objects.nonNull(userBatteryMemberCardUpdateById)) {
            rollBackBO.setDeleteUserBatteryMemberCardById(userBatteryMemberCardUpdateById);
        }
        if (Objects.nonNull(rollBackUserBatteryMemberCard)) {
            rollBackBO.setRollBackUserBatteryMemberCard(rollBackUserBatteryMemberCard);
        }
        if (Objects.nonNull(serviceFeeUserInfoById)) {
            rollBackBO.setDeleteServiceFeeUserInfoById(serviceFeeUserInfoById);
        }
        if (Objects.nonNull(rollBackServiceFeeUserInfo)) {
            rollBackBO.setRollBackServiceFeeUserInfo(rollBackServiceFeeUserInfo);
        }
        if (Objects.nonNull(eleUserDepositOperateRecordById)) {
            rollBackBO.setDeleteEleUserOperateRecordDepositById(eleUserDepositOperateRecordById);
        }
        if (Objects.nonNull(eleUserMemberCardOperateRecordById)) {
            rollBackBO.setDeleteEleUserOperateRecordMemberCardById(eleUserMemberCardOperateRecordById);
        }
        if (CollectionUtils.isNotEmpty(insertUserBatteryTypeList)) {
            rollBackBO.setInsertUserBatteryTypeList(insertUserBatteryTypeList);
        }
        if (Objects.nonNull(userBatteryMemberCardPackageId)) {
            rollBackBO.setDeleteUserBatteryMemberCardPackageId(userBatteryMemberCardPackageId);
        }
        
        return rollBackBO;
    }
}
