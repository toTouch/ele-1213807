package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ServiceFeeUserInfoMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import com.xiliulou.electricity.vo.UserServiceFeeDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户停卡绑定(TServiceFeeUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@Service("serviceFeeUserInfoService")
@Slf4j
public class ServiceFeeUserInfoServiceImpl implements ServiceFeeUserInfoService {

    @Resource
    ServiceFeeUserInfoMapper serviceFeeUserInfoMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    UserBatteryService userBatteryService;

    @Autowired
    BatteryModelService batteryModelService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;

    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Override
    public int insert(ServiceFeeUserInfo serviceFeeUserInfo) {
        return serviceFeeUserInfoMapper.insert(serviceFeeUserInfo);
    }

    @Override
    public ServiceFeeUserInfo queryByUidFromCache(Long uid) {
        ServiceFeeUserInfo cache = redisService.getWithHash(CacheConstant.SERVICE_FEE_USER_INFO + uid, ServiceFeeUserInfo.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoMapper.selectOne(new LambdaQueryWrapper<ServiceFeeUserInfo>().eq(ServiceFeeUserInfo::getUid, uid).eq(ServiceFeeUserInfo::getDelFlag, ServiceFeeUserInfo.DEL_NORMAL));
        if (Objects.isNull(serviceFeeUserInfo)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.SERVICE_FEE_USER_INFO + uid, serviceFeeUserInfo);
        return serviceFeeUserInfo;
    }

    @Override
    public void updateByUid(ServiceFeeUserInfo serviceFeeUserInfo) {

        int update = serviceFeeUserInfoMapper.updateByUid(serviceFeeUserInfo);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + serviceFeeUserInfo.getUid());
            return null;
        });
        return;
    }

    @Override
    public Integer deleteByUid(Long uid) {
        int delete = serviceFeeUserInfoMapper.deleteByUid(uid);

        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + uid);
            return null;
        });
        return delete;
    }

    @Override
    public void unbindServiceFeeInfoByUid(Long uid) {

        ServiceFeeUserInfo serviceFeeUserInfo=new ServiceFeeUserInfo();
        serviceFeeUserInfo.setUid(uid);
        serviceFeeUserInfo.setDisableMemberCardNo("");
        serviceFeeUserInfo.setPauseOrderNo("");
        serviceFeeUserInfo.setExpireOrderNo("");
        serviceFeeUserInfo.setServiceFeeGenerateTime(System.currentTimeMillis());
        serviceFeeUserInfo.setUpdateTime(System.currentTimeMillis());
        this.updateByUid(serviceFeeUserInfo);
    }

    @Override
    public List<ServiceFeeUserInfo> selectDisableMembercardList(int offset, int size) {
        return serviceFeeUserInfoMapper.selectDisableMembercardList(offset, size);
    }

    @Override
    public EleBatteryServiceFeeVO queryUserBatteryServiceFee(Long uid) {
/*

        //获取新用户所绑定的加盟商的电池服务费
        Franchisee franchisee = franchiseeService.queryByUserId(uid);
        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = new EleBatteryServiceFeeVO();
        //计算用户所产生的电池服务费
        if (Objects.isNull(franchisee)) {
            return eleBatteryServiceFeeVO;
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found user,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }

        Integer modelType = franchisee.getModelType();
        if (Objects.equals(modelType, Franchisee.OLD_MODEL_TYPE) && Objects.equals(franchisee.getBatteryServiceFee(), BigDecimal.valueOf(0))) {
            return eleBatteryServiceFeeVO;
        }

        eleBatteryServiceFeeVO.setBatteryServiceFee(franchisee.getBatteryServiceFee());

        eleBatteryServiceFeeVO.setModelType(franchisee.getModelType());

        ServiceFeeUserInfo serviceFeeUserInfo = queryByUidFromCache(uid);
    
        List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil
                .fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
        eleBatteryServiceFeeVO.setModelBatteryServiceFeeList(modelBatteryDepositList);
    
        eleBatteryServiceFeeVO.setBatteryServiceFee(franchisee.getBatteryServiceFee());
    
        if (Objects.equals(modelType, Franchisee.NEW_MODEL_TYPE)) {
            UserBattery userBattery = userBatteryService.selectByUidFromCache(uid);
            if (Objects.nonNull(userBattery)) {
                eleBatteryServiceFeeVO.setBatteryType(userBattery.getBatteryType());
                eleBatteryServiceFeeVO.setModel(batteryModelService.acquireBatteryModel(userBattery.getBatteryType(),userInfo.getTenantId()));
            }
        }

        BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);
        Long now = System.currentTimeMillis();
        long cardDays = 0;
        //用户产生的套餐过期电池服务费

        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            //查询用户是否存在套餐过期电池服务费
            userChangeServiceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(userInfo, franchisee, cardDays);
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);

        Integer memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE;

        //用户产生的停卡电池服务费
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {
                cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
                //不足一天按一天计算
                double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
                if (time < 24) {
                    cardDays = 1;
                }
                userChangeServiceFee = electricityMemberCardOrderService.checkUserDisableCardBatteryService(userInfo, uid, cardDays, null, serviceFeeUserInfo);
                memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_DISABLE;
            }
        }
        eleBatteryServiceFeeVO.setMemberCardStatus(memberCardStatus);
        eleBatteryServiceFeeVO.setUserBatteryServiceFee(userChangeServiceFee);
*/

        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = new EleBatteryServiceFeeVO();

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY SERVICE FEE WARN! not found userInfo,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if(Objects.isNull(userBatteryMemberCard)){
            log.warn("BATTERY SERVICE FEE WARN! not found userBatteryMemberCard,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }

        ServiceFeeUserInfo serviceFeeUserInfo = this.queryByUidFromCache(uid);
        if(Objects.isNull(serviceFeeUserInfo)){
            log.warn("BATTERY SERVICE FEE WARN! not found serviceFeeUserInfo,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if(Objects.isNull(batteryMemberCard)){
            log.warn("BATTERY SERVICE FEE WARN! not found batteryMemberCard,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }

        eleBatteryServiceFeeVO.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
        eleBatteryServiceFeeVO.setUserBatteryServiceFee(BigDecimal.ZERO);

        Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = this.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfo);
        if(Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())){
            eleBatteryServiceFeeVO.setUserBatteryServiceFee(acquireUserBatteryServiceFeeResult.getRight());
        }

        return eleBatteryServiceFeeVO;
    }
    
    @Override
    public BigDecimal queryUserBatteryServiceFee(UserInfo userInfo) {
        BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);
        if (Objects.isNull(userInfo)) {
            return userChangeServiceFee;
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = queryByUidFromCache(userInfo.getUid());
        
        Long now = System.currentTimeMillis();
        long cardDays = 0;
        //用户产生的套餐过期电池服务费
        
        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            //查询用户是否存在套餐过期电池服务费
            userChangeServiceFee = electricityMemberCardOrderService
                    .checkUserMemberCardExpireBatteryService(userInfo, null, cardDays);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService
                .selectByUidFromCache(userInfo.getUid());
        
        Integer memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE;
        
        //用户产生的停卡电池服务费
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)
                    || Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {
                cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
                //不足一天按一天计算
                double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
                if (time < 24) {
                    cardDays = 1;
                }
                userChangeServiceFee = electricityMemberCardOrderService
                        .checkUserDisableCardBatteryService(userInfo, userInfo.getUid(), cardDays, null,
                                serviceFeeUserInfo);
                memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_DISABLE;
            }
        }
        
        return userChangeServiceFee;
    }

    @Override
    public Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFee(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, ServiceFeeUserInfo serviceFeeUserInfo) {
        if (Objects.isNull(userInfo) || Objects.isNull(userBatteryMemberCard) || Objects.isNull(batteryMemberCard) || Objects.isNull(serviceFeeUserInfo)) {
            return Triple.of(false, null, null);
        }

        if (BigDecimal.valueOf(0).compareTo(batteryMemberCard.getServiceCharge()) == 0) {
            return Triple.of(false, null, null);
        }

        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return Triple.of(false, null, null);
        }

        //停卡电池服务费
        BigDecimal pauseBatteryServiceFee = BigDecimal.ZERO;
        //套餐过期电池服务费
        BigDecimal expireBatteryServiceFee = BigDecimal.ZERO;
        //停卡系统启用电池服务费
        BigDecimal systemEnableBatteryServiceFee = BigDecimal.ZERO;

        Integer type = null;

        //是否存在停卡电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            int batteryMembercardDisableDays = (int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);
            pauseBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMembercardDisableDays));
            type = EleBatteryServiceFeeOrder.DISABLE_MEMBER_CARD;
            log.info("BATTERY SERVICE FEE INFO!user exist pause fee,uid={},fee={}", userInfo.getUid(), pauseBatteryServiceFee.doubleValue());
        }

        //是否存在套餐过期电池服务费
        if (!Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) && (System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + 24 * 60 * 60 * 1000L) > 0)) {
            int batteryMemebercardExpireDays = (int) Math.ceil((System.currentTimeMillis() - (serviceFeeUserInfo.getServiceFeeGenerateTime() + 24 * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);
            expireBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMemebercardExpireDays));
            type = EleBatteryServiceFeeOrder.MEMBER_CARD_OVERDUE;
            log.info("BATTERY SERVICE FEE INFO!user exist expire fee,uid={},fee={}", userInfo.getUid(), expireBatteryServiceFee.doubleValue());
        }

        //是否存在停卡系统启用电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) && StringUtils.isNotBlank(serviceFeeUserInfo.getPauseOrderNo())) {
            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getPauseOrderNo());
            if (Objects.nonNull(eleBatteryServiceFeeOrder)) {
                systemEnableBatteryServiceFee = eleBatteryServiceFeeOrder.getPayAmount();
                type = EleBatteryServiceFeeOrder.DISABLE_MEMBER_CARD;
                log.info("BATTERY SERVICE FEE INFO!user exist system enable expire fee,uid={},fee={}", userInfo.getUid(), systemEnableBatteryServiceFee.doubleValue());
            }
        }

        BigDecimal totalBatteryServiceFee = pauseBatteryServiceFee.add(expireBatteryServiceFee).add(systemEnableBatteryServiceFee);
        if (totalBatteryServiceFee.doubleValue() > 0) {
            return Triple.of(true, type, totalBatteryServiceFee);
        }

        return Triple.of(false, null, null);
    }


    @Override
    public BigDecimal selectBatteryServiceFeeByUid(Long uid) {
        BigDecimal serviceFee = BigDecimal.ZERO;

        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return serviceFee;
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return serviceFee;
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            return serviceFee;
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            return serviceFee;
        }

        ServiceFeeUserInfo serviceFeeUserInfo = this.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            return serviceFee;
        }

        if (BigDecimal.valueOf(0).compareTo(batteryMemberCard.getServiceCharge()) == 0) {
            return serviceFee;
        }

        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return serviceFee;
        }

        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = this.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.FALSE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            return serviceFee;
        }

        return acquireUserBatteryServiceFeeResult.getRight();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserServiceFeeDetail> selectUserBatteryServiceFee() {
        List<UserServiceFeeDetail> list = new ArrayList<>();

        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return list;
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return list;
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            return list;
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            return list;
        }

        ServiceFeeUserInfo serviceFeeUserInfo = this.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            return list;
        }

        if (BigDecimal.valueOf(0).compareTo(batteryMemberCard.getServiceCharge()) == 0) {
            return list;
        }

        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return list;
        }

        //停卡电池服务费
        BigDecimal pauseBatteryServiceFee = BigDecimal.ZERO;
        //套餐过期电池服务费
        BigDecimal expireBatteryServiceFee = BigDecimal.ZERO;
        //停卡系统启用电池服务费
        BigDecimal systemEnableBatteryServiceFee = BigDecimal.ZERO;

        Integer type = null;

        //是否存在停卡电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            int batteryMembercardDisableDays = (int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);
            pauseBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMembercardDisableDays));
            type = EleBatteryServiceFeeOrder.DISABLE_MEMBER_CARD;
            log.info("BATTERY SERVICE FEE INFO!user exist pause fee,uid={},fee={}", userInfo.getUid(), pauseBatteryServiceFee.doubleValue());

            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getPauseOrderNo());
            if (Objects.nonNull(eleBatteryServiceFeeOrder)) {
                UserServiceFeeDetail userServiceFeeDetail = new UserServiceFeeDetail();
                userServiceFeeDetail.setBatteryMembercardName(batteryMemberCard.getName());
                userServiceFeeDetail.setSource(eleBatteryServiceFeeOrder.getSource());
                userServiceFeeDetail.setBatteryServiceFee(pauseBatteryServiceFee);
                userServiceFeeDetail.setBatteryServiceFeeGenerateTime(eleBatteryServiceFeeOrder.getBatteryServiceFeeGenerateTime());
                list.add(userServiceFeeDetail);
            }
        }

        //是否存在套餐过期电池服务费
        if (!Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) && (System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + 24 * 60 * 60 * 1000L) > 0)) {
            int batteryMemebercardExpireDays = (int) Math.ceil((System.currentTimeMillis() - (serviceFeeUserInfo.getServiceFeeGenerateTime() + 24 * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);
            expireBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMemebercardExpireDays));
            type = EleBatteryServiceFeeOrder.MEMBER_CARD_OVERDUE;
            log.info("BATTERY SERVICE FEE INFO!user exist expire fee,uid={},fee={}", userInfo.getUid(), expireBatteryServiceFee.doubleValue());

            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getExpireOrderNo());
            if (Objects.nonNull(eleBatteryServiceFeeOrder)) {
                UserServiceFeeDetail userServiceFeeDetail = new UserServiceFeeDetail();
                userServiceFeeDetail.setBatteryMembercardName(batteryMemberCard.getName());
                userServiceFeeDetail.setSource(eleBatteryServiceFeeOrder.getSource());
                userServiceFeeDetail.setBatteryServiceFee(expireBatteryServiceFee);
                userServiceFeeDetail.setBatteryServiceFeeGenerateTime(eleBatteryServiceFeeOrder.getBatteryServiceFeeGenerateTime());
                list.add(userServiceFeeDetail);
            }
        }

        //是否存在停卡系统启用电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) && StringUtils.isNotBlank(serviceFeeUserInfo.getPauseOrderNo())) {

            log.info("BATTERY SERVICE FEE INFO!user exist system enable expire fee,uid={},fee={}", userInfo.getUid(), systemEnableBatteryServiceFee.doubleValue());

            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getPauseOrderNo());
            if (Objects.nonNull(eleBatteryServiceFeeOrder)) {
                UserServiceFeeDetail userServiceFeeDetail = new UserServiceFeeDetail();
                userServiceFeeDetail.setBatteryMembercardName(batteryMemberCard.getName());
                userServiceFeeDetail.setSource(eleBatteryServiceFeeOrder.getSource());
                userServiceFeeDetail.setBatteryServiceFee(eleBatteryServiceFeeOrder.getPayAmount());
                userServiceFeeDetail.setBatteryServiceFeeGenerateTime(eleBatteryServiceFeeOrder.getBatteryServiceFeeGenerateTime());
                list.add(userServiceFeeDetail);
            }
        }

        return list;
    }
}
