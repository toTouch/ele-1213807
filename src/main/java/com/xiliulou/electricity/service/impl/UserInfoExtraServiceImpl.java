package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.RebateConfig;
import com.xiliulou.electricity.mapper.UserInfoExtraMapper;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (UserInfoExtra)表服务实现类
 *
 * @author zzlong
 * @since 2024-02-18 10:39:59
 */
@Service("userInfoExtraService")
@Slf4j
public class UserInfoExtraServiceImpl implements UserInfoExtraService {
    
    @Resource
    private UserInfoExtraMapper userInfoExtraMapper;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Autowired
    private MerchantAttrService merchantAttrService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    private MerchantLevelService merchantLevelService;
    
    @Autowired
    private RebateConfigService rebateConfigService;
    
    @Override
    public UserInfoExtra queryByUidFromDB(Long uid) {
        return this.userInfoExtraMapper.selectByUid(uid);
    }
    
    @Override
    public UserInfoExtra queryByUidFromCache(Long uid) {
        UserInfoExtra cacheUserInfoExtra = redisService.getWithHash(CacheConstant.CACHE_USER_INFO_EXTRA + uid, UserInfoExtra.class);
        if (Objects.nonNull(cacheUserInfoExtra)) {
            return cacheUserInfoExtra;
        }
        
        UserInfoExtra userInfoExtra = this.queryByUidFromDB(uid);
        if (Objects.isNull(userInfoExtra)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_USER_INFO_EXTRA + uid, userInfoExtra);
        
        return userInfoExtra;
    }
    
    @Override
    public UserInfoExtra insert(UserInfoExtra userInfoExtra) {
        this.userInfoExtraMapper.insert(userInfoExtra);
        return userInfoExtra;
    }
    
    @Override
    public Integer updateByUid(UserInfoExtra userInfoExtra) {
        int update = this.userInfoExtraMapper.updateByUid(userInfoExtra);
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_INFO_EXTRA + userInfoExtra.getUid());
        });
        
        return update;
    }
    
    @Override
    public Integer deleteByUid(Long uid) {
        //        int delete = this.userInfoExtraMapper.deleteByUid(uid);
        UserInfoExtra userInfoExtra = new UserInfoExtra();
        userInfoExtra.setUid(uid);
        userInfoExtra.setDelFlag(User.DEL_DEL);
        userInfoExtra.setUpdateTime(System.currentTimeMillis());
        return this.updateByUid(userInfoExtra);
    }
    
    @Override
    public void bindMerchant(Long uid, String orderId, Long memberCardId) {
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderId);
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("BIND MERCHANT WARN!electricityMemberCardOrder is null,uid={},orderId={}", uid, orderId);
            return;
        }
        
        if (Objects.isNull(electricityMemberCardOrder.getPayCount()) || electricityMemberCardOrder.getPayCount() > 1) {
            log.info("BIND MERCHANT WARN!payCount is illegal,uid={},orderId={}", uid, orderId);
            return;
        }
        
        UserInfoExtra userInfoExtra = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfoExtra)) {
            log.warn("BIND MERCHANT WARN!userInfoExtra is null,uid={},orderId={}", uid, orderId);
            return;
        }
        
        if (Objects.nonNull(userInfoExtra.getMerchantId())) {
            log.warn("BIND MERCHANT WARN!user already bind merchant,uid={},orderId={}", uid, orderId);
            return;
        }
        
        MerchantJoinRecord merchantJoinRecord = merchantJoinRecordService.queryByJoinUid(uid);
        if (Objects.isNull(merchantJoinRecord)) {
            log.warn("BIND MERCHANT WARN!merchantJoinRecord is null,uid={},orderId={}", uid, orderId);
            return;
        }
        
        Merchant merchant = merchantService.queryByIdFromCache(merchantJoinRecord.getMerchantId());
        if (Objects.isNull(merchant)) {
            log.warn("BIND MERCHANT WARN!merchant is null,merchantId={},uid={}", merchantJoinRecord.getMerchantId(), uid);
            return;
        }
//处理用户扫完码后商户被禁用
//        if (Objects.equals(MerchantConstant.DISABLE, merchant.getStatus())) {
//            log.warn("BIND MERCHANT WARN!merchant is disable,merchantId={},uid={}", merchantJoinRecord.getMerchantId(), uid);
//            return;
//        }
        
        MerchantAttr merchantAttr = merchantAttrService.queryByTenantId(merchant.getTenantId());
        if (Objects.isNull(merchantAttr)) {
            log.warn("BIND MERCHANT WARN!merchantAttr is null,merchantId={},uid={}", merchantJoinRecord.getMerchantId(), uid);
            return;
        }
        
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchant.getMerchantGradeId());
        if (Objects.isNull(merchantLevel)) {
            log.warn("BIND MERCHANT WARN!merchantLevel is null,merchantId={},uid={}", merchantJoinRecord.getMerchantId(), uid);
            return;
        }
        
        //根据商户等级&套餐id获取返利套餐
        RebateConfig rebateConfig = rebateConfigService.queryByMidAndMerchantLevel(memberCardId, merchantLevel.getLevel());
        if (Objects.isNull(rebateConfig)) {
            log.warn("BIND MERCHANT WARN!rebateConfig is null,merchantId={},uid={},level={}", merchantJoinRecord.getMerchantId(), uid, merchantLevel.getLevel());
            return;
        }
        
        if (Objects.isNull(rebateConfig.getStatus()) || Objects.equals(rebateConfig.getStatus(), MerchantConstant.REBATE_DISABLE)) {
            log.warn("BIND MERCHANT WARN!rebateConfig status illegal,id={},uid={}", rebateConfig.getId(), uid);
            return;
        }
        
        //邀请有效期内
        if (merchantAttrService.checkInvitationTime(merchantAttr, merchantJoinRecord.getStartTime())) {
            UserInfoExtra userInfoExtraUpdate = new UserInfoExtra();
            userInfoExtraUpdate.setUid(uid);
            userInfoExtraUpdate.setMerchantId(merchantJoinRecord.getMerchantId());
            userInfoExtraUpdate.setChannelEmployeeUid(merchantJoinRecord.getChannelEmployeeUid());
            userInfoExtraUpdate.setUpdateTime(System.currentTimeMillis());
            if (Objects.equals(MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_PLACE_EMPLOYEE, merchantJoinRecord.getInviterType())) {
                userInfoExtraUpdate.setPlaceUid(merchantJoinRecord.getInviterUid());
                userInfoExtraUpdate.setPlaceId(merchantJoinRecord.getPlaceId());
            }
            
            this.updateByUid(userInfoExtraUpdate);
            
            MerchantJoinRecord merchantJoinRecordUpdate = new MerchantJoinRecord();
            merchantJoinRecordUpdate.setId(merchantJoinRecord.getId());
            merchantJoinRecordUpdate.setStatus(MerchantJoinRecordConstant.STATUS_SUCCESS);
            merchantJoinRecordUpdate.setUpdateTime(System.currentTimeMillis());
            merchantJoinRecordService.updateById(merchantJoinRecordUpdate);
        } else {
            MerchantJoinRecord merchantJoinRecordUpdate = new MerchantJoinRecord();
            merchantJoinRecordUpdate.setId(merchantJoinRecord.getId());
            merchantJoinRecordUpdate.setStatus(MerchantJoinRecordConstant.STATUS_EXPIRED);
            merchantJoinRecordUpdate.setUpdateTime(System.currentTimeMillis());
            merchantJoinRecordService.updateById(merchantJoinRecordUpdate);
        }
    }
}
