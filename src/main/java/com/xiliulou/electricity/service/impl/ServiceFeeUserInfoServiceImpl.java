package com.xiliulou.electricity.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ServiceFeeUserInfoMapper;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import com.xiliulou.electricity.vo.UserServiceFeeDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    
    private final ScheduledThreadPoolExecutor scheduledExecutor = ThreadUtil.createScheduledExecutor(1);
    
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
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoMapper.selectOne(
                new LambdaQueryWrapper<ServiceFeeUserInfo>().eq(ServiceFeeUserInfo::getUid, uid).eq(ServiceFeeUserInfo::getDelFlag, ServiceFeeUserInfo.DEL_NORMAL));
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
            clearCache(serviceFeeUserInfo.getUid());
            return null;
        });
        return;
    }
    
    @Override
    public Integer deleteByUid(Long uid) {
        int delete = serviceFeeUserInfoMapper.deleteByUid(uid);
        
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + uid);
            clearCache(uid);
            return null;
        });
        return delete;
    }
    
    private void clearCache(Long uid) {
        scheduledExecutor.schedule(() -> {
            if (redisService.hasKey(CacheConstant.SERVICE_FEE_USER_INFO + uid)) {
                redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + uid);
            }
        }, 1, TimeUnit.SECONDS);
    }
    
    @Override
    public void unbindServiceFeeInfoByUid(Long uid) {
        
        ServiceFeeUserInfo serviceFeeUserInfo = new ServiceFeeUserInfo();
        serviceFeeUserInfo.setUid(uid);
        serviceFeeUserInfo.setDisableMemberCardNo("");
        serviceFeeUserInfo.setPauseOrderNo("");
        serviceFeeUserInfo.setExpireOrderNo("");
        serviceFeeUserInfo.setServiceFeeGenerateTime(System.currentTimeMillis());
        serviceFeeUserInfo.setUpdateTime(System.currentTimeMillis());
        this.updateByUid(serviceFeeUserInfo);
    }
    
    @Slave
    @Override
    public List<ServiceFeeUserInfo> selectDisableMembercardList(int offset, int size) {
        return serviceFeeUserInfoMapper.selectDisableMembercardList(offset, size);
    }
    
    @Override
    public EleBatteryServiceFeeVO queryUserBatteryServiceFee(Long uid) {
        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = new EleBatteryServiceFeeVO();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY SERVICE FEE WARN! not found userInfo,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("BATTERY SERVICE FEE WARN! not found userBatteryMemberCard,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("BATTERY SERVICE FEE WARN! not found serviceFeeUserInfo,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("BATTERY SERVICE FEE WARN! not found batteryMemberCard,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }
        
        eleBatteryServiceFeeVO.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
        eleBatteryServiceFeeVO.setUserBatteryServiceFee(BigDecimal.ZERO);
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = this.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard,
                serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            eleBatteryServiceFeeVO.setUserBatteryServiceFee(acquireUserBatteryServiceFeeResult.getRight());
        }
        
        return eleBatteryServiceFeeVO;
    }
    
    @Override
    @Deprecated
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
            userChangeServiceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(userInfo, null, cardDays);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        
        Integer memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE;
        
        //用户产生的停卡电池服务费
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.nonNull(
                    userBatteryMemberCard.getDisableMemberCardTime())) {
                cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
                //不足一天按一天计算
                double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
                if (time < 24) {
                    cardDays = 1;
                }
                userChangeServiceFee = electricityMemberCardOrderService.checkUserDisableCardBatteryService(userInfo, userInfo.getUid(), cardDays, null, serviceFeeUserInfo);
                memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_DISABLE;
            }
        }
        
        return userChangeServiceFee;
    }
    
    @Override
    public Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFee(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard,
            ServiceFeeUserInfo serviceFeeUserInfo) {
        BatteryMemberCard userBindBatteryMemberCard = Objects.isNull(userBatteryMemberCard) ? null : batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        
        if (Objects.isNull(userInfo) || Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBindBatteryMemberCard) || Objects.isNull(serviceFeeUserInfo)) {
            return Triple.of(false, null, null);
        }
        
        // 防止上线重启过程中还未删除缓存的情况下报错，暂时先取过期滞纳金，上线之后不可能为null了
        BigDecimal freezeServiceCharge = userBindBatteryMemberCard.getFreezeServiceCharge();
        if (Objects.isNull(freezeServiceCharge)) {
            log.info("BATTERY SERVICE FEE INFO!freezeServiceCharge user the value of ServiceCharge,uid={}", userInfo.getUid());
            freezeServiceCharge = userBindBatteryMemberCard.getServiceCharge();
        }
        
        // 此时若仍然为null，则说明两个计算标准都为null，不再执行
        if (Objects.isNull(freezeServiceCharge)) {
            log.info("BATTERY SERVICE FEE INFO!freezeServiceCharge and ServiceCharge both are null,uid={}", userInfo.getUid());
            return Triple.of(false, null, null);
        }
        
        if (BigDecimal.valueOf(0).compareTo(userBindBatteryMemberCard.getServiceCharge()) == 0 && BigDecimal.valueOf(0).compareTo(freezeServiceCharge) == 0) {
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
            pauseBatteryServiceFee = freezeServiceCharge.multiply(BigDecimal.valueOf(batteryMembercardDisableDays));
            type = EleBatteryServiceFeeOrder.DISABLE_MEMBER_CARD;
            log.info("BATTERY SERVICE FEE INFO!user exist pause fee,uid={},fee={}", userInfo.getUid(), pauseBatteryServiceFee.doubleValue());
        }
        
        EleBatteryServiceFeeOrder expireEleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getExpireOrderNo());
        Integer expiredProtectionTime = eleBatteryServiceFeeOrderService.getExpiredProtectionTime(expireEleBatteryServiceFeeOrder, userInfo.getTenantId());
        //是否存在套餐过期电池服务费
        if (!Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) && (
                System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000L) > 0)) {
            int batteryMemebercardExpireDays = (int) Math.ceil(
                    (System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);
            expireBatteryServiceFee = userBindBatteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMemebercardExpireDays));
            type = EleBatteryServiceFeeOrder.MEMBER_CARD_OVERDUE;
            log.info("BATTERY SERVICE FEE INFO!user exist expire fee,uid={},fee={}", userInfo.getUid(), expireBatteryServiceFee.doubleValue());
        }
        
        //是否存在停卡系统启用电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) && StringUtils.isNotBlank(
                serviceFeeUserInfo.getPauseOrderNo())) {
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
    
    /**
     * 批量查询滞纳金，参照上面方法：acquireUserBatteryServiceFee
     */
    @Override
    public Map<Long, BigDecimal> acquireUserBatteryServiceFeeByUserList(List<UserInfo> userInfoList, List<UserBatteryMemberCard> userBatteryMemberCardList,
            List<BatteryMemberCard> batteryMemberCardList, List<ServiceFeeUserInfo> serviceFeeUserInfoList) {
        if (CollectionUtils.isEmpty(userInfoList) || CollectionUtils.isEmpty(userBatteryMemberCardList) || CollectionUtils.isEmpty(serviceFeeUserInfoList)) {
            return null;
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        Map<Long, UserBatteryMemberCard> userBatteryMemberCardMap = userBatteryMemberCardList.stream()
                .collect(Collectors.toMap(UserBatteryMemberCard::getUid, v -> v, (k1, k2) -> k1));
        Map<Long, ServiceFeeUserInfo> userServiceFeeUserInfoMap = serviceFeeUserInfoList.stream().collect(Collectors.toMap(ServiceFeeUserInfo::getUid, v -> v, (k1, k2) -> k1));
        List<String> expireOrderNoList = serviceFeeUserInfoList.stream().map(ServiceFeeUserInfo::getExpireOrderNo).collect(Collectors.toList());
        List<String> pauseOrderNoList = serviceFeeUserInfoList.stream().map(ServiceFeeUserInfo::getPauseOrderNo).collect(Collectors.toList());
        Map<Long, BatteryMemberCard> batteryMemberCardMap = null;
        if (CollectionUtils.isNotEmpty(batteryMemberCardList)) {
            batteryMemberCardMap = batteryMemberCardList.stream().collect(Collectors.toMap(BatteryMemberCard::getId, v -> v, (k1, k2) -> k1));
        }
        
        // 查询套餐过期电池服务费订单
        Map<String, EleBatteryServiceFeeOrder> expireServiceFeeOrderMap = null;
        if (CollectionUtils.isNotEmpty(expireOrderNoList)) {
            List<EleBatteryServiceFeeOrder> expireServiceFeeOrderList = eleBatteryServiceFeeOrderService.listByOrderNoList(expireOrderNoList, tenantId);
            if (CollectionUtils.isNotEmpty(expireServiceFeeOrderList)) {
                expireServiceFeeOrderMap = expireServiceFeeOrderList.stream().collect(Collectors.toMap(EleBatteryServiceFeeOrder::getOrderId, v -> v, (k1, k2) -> k1));
            }
        }
        
        // 查询停卡系统启用电池服务费订单
        Map<String, EleBatteryServiceFeeOrder> pauseServiceFeeOrderMap = null;
        if (CollectionUtils.isNotEmpty(pauseOrderNoList)) {
            List<EleBatteryServiceFeeOrder> pauseServiceFeeOrderList = eleBatteryServiceFeeOrderService.listByOrderNoList(pauseOrderNoList, tenantId);
            if (CollectionUtils.isNotEmpty(pauseServiceFeeOrderList)) {
                pauseServiceFeeOrderMap = pauseServiceFeeOrderList.stream().collect(Collectors.toMap(EleBatteryServiceFeeOrder::getOrderId, v -> v, (k1, k2) -> k1));
            }
        }
        
        return acquireUserBatteryServiceFeeByUsers(userInfoList, userBatteryMemberCardMap, batteryMemberCardMap, userServiceFeeUserInfoMap, expireServiceFeeOrderMap,
                pauseServiceFeeOrderMap);
    }
    
    private Map<Long, BigDecimal> acquireUserBatteryServiceFeeByUsers(List<UserInfo> userInfoList, Map<Long, UserBatteryMemberCard> userBatteryMemberCardMap,
            Map<Long, BatteryMemberCard> batteryMemberCardMap, Map<Long, ServiceFeeUserInfo> userServiceFeeUserInfoMap,
            Map<String, EleBatteryServiceFeeOrder> expireServiceFeeOrderMap, Map<String, EleBatteryServiceFeeOrder> pauseServiceFeeOrderMap) {
        Map<Long, BigDecimal> resultMap = new HashMap<>(userInfoList.size());
        
        for (UserInfo userInfo : userInfoList) {
            if (Objects.isNull(userInfo)) {
                continue;
            }
            Long uid = userInfo.getUid();
            UserBatteryMemberCard userBatteryMemberCard = null;
            if (MapUtils.isNotEmpty(userBatteryMemberCardMap) && userBatteryMemberCardMap.containsKey(uid)) {
                userBatteryMemberCard = userBatteryMemberCardMap.get(uid);
            }
            
            BatteryMemberCard userBindBatteryMemberCard = null;
            if (Objects.nonNull(userBatteryMemberCard) && MapUtils.isNotEmpty(batteryMemberCardMap) && batteryMemberCardMap.containsKey(userBatteryMemberCard.getMemberCardId())) {
                userBindBatteryMemberCard = batteryMemberCardMap.get(userBatteryMemberCard.getMemberCardId());
            }
            
            ServiceFeeUserInfo serviceFeeUserInfo = null;
            if (MapUtils.isNotEmpty(userServiceFeeUserInfoMap) && userServiceFeeUserInfoMap.containsKey(uid)) {
                serviceFeeUserInfo = userServiceFeeUserInfoMap.get(uid);
            }
            
            if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBindBatteryMemberCard) || Objects.isNull(serviceFeeUserInfo)) {
                continue;
            }
            
            // 防止上线重启过程中还未删除缓存的情况下报错，暂时先取过期滞纳金，上线之后不可能为null了
            BigDecimal freezeServiceCharge = userBindBatteryMemberCard.getFreezeServiceCharge();
            if (Objects.isNull(freezeServiceCharge)) {
                log.info("BATTERY SERVICE FEE INFO BY USERINFO LIST!freezeServiceCharge user the value of ServiceCharge,uid={}", uid);
                freezeServiceCharge = userBindBatteryMemberCard.getServiceCharge();
            }
            
            // 此时若仍然为null，则说明两个计算标准都为null，不再执行
            if (Objects.isNull(freezeServiceCharge)) {
                log.info("BATTERY SERVICE FEE INFO BY USERINFO LIST!freezeServiceCharge and ServiceCharge both are null,uid={}", uid);
                continue;
            }
            
            if (BigDecimal.valueOf(0).compareTo(userBindBatteryMemberCard.getServiceCharge()) == 0 && BigDecimal.valueOf(0).compareTo(freezeServiceCharge) == 0) {
                continue;
            }
            
            if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                continue;
            }
            
            //停卡电池服务费
            BigDecimal pauseBatteryServiceFee = BigDecimal.ZERO;
            //套餐过期电池服务费
            BigDecimal expireBatteryServiceFee = BigDecimal.ZERO;
            //停卡系统启用电池服务费
            BigDecimal systemEnableBatteryServiceFee = BigDecimal.ZERO;
            
            //是否存在停卡电池服务费
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                int batteryMembercardDisableDays = (int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);
                pauseBatteryServiceFee = freezeServiceCharge.multiply(BigDecimal.valueOf(batteryMembercardDisableDays));
                log.info("BATTERY SERVICE FEE INFO BY USERINFO LIST!user exist pause fee,uid={},fee={}", uid, pauseBatteryServiceFee.doubleValue());
            }
            
            EleBatteryServiceFeeOrder expireEleBatteryServiceFeeOrder = null;
            if (MapUtils.isNotEmpty(expireServiceFeeOrderMap) && expireServiceFeeOrderMap.containsKey(serviceFeeUserInfo.getExpireOrderNo())) {
                expireEleBatteryServiceFeeOrder = expireServiceFeeOrderMap.get(serviceFeeUserInfo.getExpireOrderNo());
            }
            
            Integer expiredProtectionTime = eleBatteryServiceFeeOrderService.getExpiredProtectionTime(expireEleBatteryServiceFeeOrder, userInfo.getTenantId());
            //是否存在套餐过期电池服务费
            if (!Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) && (
                    System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000L) > 0)) {
                int batteryMemebercardExpireDays = (int) Math.ceil(
                        (System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);
                expireBatteryServiceFee = userBindBatteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMemebercardExpireDays));
                log.info("BATTERY SERVICE FEE INFO BY USERINFO LIST!user exist expire fee,uid={},fee={}", uid, expireBatteryServiceFee.doubleValue());
            }
            
            //是否存在停卡系统启用电池服务费
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) && StringUtils.isNotBlank(
                    serviceFeeUserInfo.getPauseOrderNo())) {
                EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = null;
                if (MapUtils.isNotEmpty(pauseServiceFeeOrderMap) && pauseServiceFeeOrderMap.containsKey(serviceFeeUserInfo.getPauseOrderNo())) {
                    eleBatteryServiceFeeOrder = pauseServiceFeeOrderMap.get(serviceFeeUserInfo.getPauseOrderNo());
                }
                
                if (Objects.nonNull(eleBatteryServiceFeeOrder)) {
                    systemEnableBatteryServiceFee = eleBatteryServiceFeeOrder.getPayAmount();
                    log.info("BATTERY SERVICE FEE INFO BY USERINFO LIST!user exist system enable expire fee,uid={},fee={}", uid, systemEnableBatteryServiceFee.doubleValue());
                }
            }
            
            BigDecimal totalBatteryServiceFee = pauseBatteryServiceFee.add(expireBatteryServiceFee).add(systemEnableBatteryServiceFee);
            if (totalBatteryServiceFee.doubleValue() > 0) {
                resultMap.put(uid, totalBatteryServiceFee);
            }
        }
        return resultMap;
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
        
        // 需求上线时避免重启中未删除缓存导致报错，以后无用
        BigDecimal freezeServiceCharge = batteryMemberCard.getFreezeServiceCharge();
        if (Objects.isNull(freezeServiceCharge)) {
            freezeServiceCharge = batteryMemberCard.getServiceCharge();
        }
        
        if (Objects.isNull(freezeServiceCharge)) {
            return serviceFee;
        }
        
        if (BigDecimal.valueOf(0).compareTo(batteryMemberCard.getServiceCharge()) == 0 && BigDecimal.valueOf(0).compareTo(freezeServiceCharge) == 0) {
            return serviceFee;
        }
        
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return serviceFee;
        }
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = this.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard,
                serviceFeeUserInfo);
        if (Boolean.FALSE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            return serviceFee;
        }
        
        return acquireUserBatteryServiceFeeResult.getRight();
    }
    
    @Override
    public Integer deleteById(Long id) {
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoMapper.selectById(id);
        int delete = 0;
        if (Objects.nonNull(serviceFeeUserInfo)) {
            delete = serviceFeeUserInfoMapper.deleteById(id);
            
            DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
                //更新缓存
                redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + serviceFeeUserInfo.getUid());
                clearCache(serviceFeeUserInfo.getUid());
            });
        }
        
        return delete;
    }
    
    @Slave
    @Override
    public List<ServiceFeeUserInfo> listByUidList(List<Long> uidList) {
        return serviceFeeUserInfoMapper.selectListByUidList(uidList);
    }
    
    @Override
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
        
        // 需求上线时避免重启中未删除缓存导致报错，以后无用
        BigDecimal freezeServiceCharge = batteryMemberCard.getFreezeServiceCharge();
        if (Objects.isNull(freezeServiceCharge)) {
            freezeServiceCharge = batteryMemberCard.getServiceCharge();
        }
        
        if (Objects.isNull(freezeServiceCharge)) {
            return list;
        }
        
        if (BigDecimal.valueOf(0).compareTo(batteryMemberCard.getServiceCharge()) == 0 && BigDecimal.valueOf(0).compareTo(freezeServiceCharge) == 0) {
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
        
        //是否存在停卡电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            int batteryMembercardDisableDays = (int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);
            pauseBatteryServiceFee = freezeServiceCharge.multiply(BigDecimal.valueOf(batteryMembercardDisableDays));
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
        
        EleBatteryServiceFeeOrder expireEleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getExpireOrderNo());
        Integer expiredProtectionTime = eleBatteryServiceFeeOrderService.getExpiredProtectionTime(expireEleBatteryServiceFeeOrder, userInfo.getTenantId());
        
        //是否存在套餐过期电池服务费
        if (!Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) && (
                System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000L) > 0)) {
            int batteryMemebercardExpireDays = (int) Math.ceil(
                    (System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);
            expireBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMemebercardExpireDays));
            log.info("BATTERY SERVICE FEE INFO!user exist expire fee,uid={},fee={}", userInfo.getUid(), expireBatteryServiceFee.doubleValue());
            
            
            if (Objects.nonNull(expireEleBatteryServiceFeeOrder)) {
                UserServiceFeeDetail userServiceFeeDetail = new UserServiceFeeDetail();
                userServiceFeeDetail.setBatteryMembercardName(batteryMemberCard.getName());
                userServiceFeeDetail.setSource(expireEleBatteryServiceFeeOrder.getSource());
                userServiceFeeDetail.setBatteryServiceFee(expireBatteryServiceFee);
                userServiceFeeDetail.setBatteryServiceFeeGenerateTime(expireEleBatteryServiceFeeOrder.getBatteryServiceFeeGenerateTime());
                list.add(userServiceFeeDetail);
            }
        }
        
        //是否存在停卡系统启用电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) && StringUtils.isNotBlank(
                serviceFeeUserInfo.getPauseOrderNo())) {
            
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
