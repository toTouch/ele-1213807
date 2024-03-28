package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.entity.ChannelActivityHistory;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.InvitationActivityJoinHistory;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityHistory;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.entity.merchant.MerchantInviterModifyRecord;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.RebateConfig;
import com.xiliulou.electricity.enums.merchant.MerchantInviterSourceEnum;
import com.xiliulou.electricity.mapper.UserInfoExtraMapper;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterRequest;
import com.xiliulou.electricity.request.merchant.MerchantPageRequest;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantInviterModifyRecordService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.merchant.MerchantForModifyInviterVO;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;
import com.xiliulou.electricity.vo.merchant.MerchantVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    @Resource
    JoinShareActivityHistoryService joinShareActivityHistoryService;
    
    @Resource
    JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;
    
    @Resource
    InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;
    
    @Resource
    ChannelActivityHistoryService channelActivityHistoryService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private MerchantInviterModifyRecordService merchantInviterModifyRecordService;
    
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
    
    @Override
    public R selectModifyInviterInfo(Long uid, Long size, Long offset) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        MerchantInviterVO successInviterVO = this.querySuccessInviter(uid, tenantId);
        if (Objects.isNull(successInviterVO)) {
            log.warn("Modify inviter fail! not found success record, uid={}", uid);
            return R.fail("120107", "该用户未成功参与活动，无法修改邀请人");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("Modify inviter fail! not found userInfo, uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 商户列表
        MerchantPageRequest merchantPageRequest = MerchantPageRequest.builder().size(size).offset(offset).tenantId(tenantId).franchiseeId(userInfo.getFranchiseeId()).build();
        List<MerchantVO> merchantList = merchantService.listByPage(merchantPageRequest);
        if (CollectionUtils.isEmpty(merchantList)) {
            log.warn("Modify inviter fail! merchantList is empty, uid={}", uid);
            return R.fail("120007", "未找到商户");
        }
        
        Integer inviterSource = successInviterVO.getInviterSource();
        Long inviterUid = successInviterVO.getInviterUid();
        List<MerchantVO> filterMerchantList = null;
        if (Objects.equals(inviterSource, MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_MERCHANT.getCode())) {
            // 去除原商户邀请人
            filterMerchantList = merchantList.stream().filter(merchant -> !Objects.equals(merchant.getUid(), inviterUid)).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(filterMerchantList)) {
            log.warn("Modify inviter fail! filterMerchantList is empty, uid={}", uid);
            return R.fail("120007", "未找到商户");
        }
        
        List<MerchantForModifyInviterVO> merchantVOList = filterMerchantList.stream().map(item -> {
            MerchantForModifyInviterVO merchantForModifyInviterVO = new MerchantForModifyInviterVO();
            BeanUtils.copyProperties(item, merchantForModifyInviterVO);
            
            return merchantForModifyInviterVO;
        }).collect(Collectors.toList());
        
        return R.ok(merchantVOList);
    }
    
    @Override
    public MerchantInviterVO querySuccessInviter(Long uid, Integer tenantId) {
        Long id = null;
        Long inviterUid = null;
        String inviterName = null;
        Integer inviterSource = null;
        
        JoinShareActivityHistory joinShareActivityHistory = joinShareActivityHistoryService.querySuccessHistoryByJoinUid(uid, tenantId);
        if (Objects.nonNull(joinShareActivityHistory)) {
            id = joinShareActivityHistory.getId();
            inviterUid = joinShareActivityHistory.getUid();
            inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_SHARE_ACTIVITY.getCode();
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(inviterUid);
            if (Objects.nonNull(userInfo)) {
                inviterName = userInfo.getName();
            }
        }
        
        JoinShareMoneyActivityHistory joinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.querySuccessHistoryByJoinUid(uid, tenantId);
        if (Objects.nonNull(joinShareMoneyActivityHistory)) {
            id = joinShareMoneyActivityHistory.getId();
            inviterUid = joinShareMoneyActivityHistory.getUid();
            inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_SHARE_MONEY_ACTIVITY.getCode();
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(inviterUid);
            if (Objects.nonNull(userInfo)) {
                inviterName = userInfo.getName();
            }
        }
        
        InvitationActivityJoinHistory invitationActivityJoinHistory = invitationActivityJoinHistoryService.querySuccessHistoryByJoinUid(uid, tenantId);
        if (Objects.nonNull(invitationActivityJoinHistory)) {
            id = invitationActivityJoinHistory.getId();
            inviterUid = invitationActivityJoinHistory.getUid();
            inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_INVITATION_ACTIVITY.getCode();
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(inviterUid);
            if (Objects.nonNull(userInfo)) {
                inviterName = userInfo.getName();
            }
        }
        
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.querySuccessHistoryByJoinUid(uid, tenantId);
        if (Objects.nonNull(channelActivityHistory)) {
            id = channelActivityHistory.getId();
            inviterUid = channelActivityHistory.getUid();
            inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_CHANNEL_ACTIVITY.getCode();
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(inviterUid);
            if (Objects.nonNull(userInfo)) {
                inviterName = userInfo.getName();
            }
        }
        
        MerchantJoinRecord merchantJoinRecord = merchantJoinRecordService.querySuccessRecordByJoinUid(uid, tenantId);
        if (Objects.nonNull(merchantJoinRecord)) {
            id = merchantJoinRecord.getId();
            inviterUid = merchantJoinRecord.getInviterUid();
            inviterName = Optional.ofNullable(userService.queryByUidFromCache(inviterUid)).map(User::getName).orElse("");
            inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_MERCHANT.getCode();
        }
        
        if (Objects.isNull(inviterUid)) {
            return null;
        }
        
        return MerchantInviterVO.builder().id(id).inviterUid(inviterUid).inviterName(inviterName).inviterSource(inviterSource).build();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R modifyInviter(MerchantModifyInviterRequest merchantModifyInviterRequest, Long operator) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Long uid = merchantModifyInviterRequest.getUid();
        Long merchantId = merchantModifyInviterRequest.getMerchantId();
        
        Merchant merchant = merchantService.queryByIdFromCache(merchantId);
        if (Objects.isNull(merchant)) {
            log.warn("Modify inviter fail! merchant not exist, merchantId={}", merchantId);
            return R.fail("120212", "商户不存在");
        }
        
        // 参与成功的记录
        MerchantInviterVO successInviterVO = querySuccessInviter(uid, tenantId);
        if (Objects.isNull(successInviterVO)) {
            log.warn("Modify inviter fail! not found success record, uid={}", uid);
            return R.fail("120107", "该用户未成功参与活动，无法修改邀请人");
        }
        
        Long id = successInviterVO.getId();
        Integer inviterSource = successInviterVO.getInviterSource();
        Long oldInviterUid = successInviterVO.getInviterUid();
        Long newInviterUid = merchant.getUid();
        Long channelEmployeeUid = merchant.getChannelEmployeeUid();
        
        if (Objects.equals(oldInviterUid, newInviterUid)) {
            log.warn("Modify inviter fail! inviters can not be the same, uid={}, oldInviterUid={}, newInviterUid={}", uid, oldInviterUid, newInviterUid);
            return R.fail("120108", "修改前的邀请人和修改后的邀请人不能相同");
        }
        
        // 逻辑删除旧的记录
        switch (inviterSource) {
            case 1:
                // 邀请返券
                joinShareActivityHistoryService.removeById(id);
                break;
            case 2:
                // 邀请返现
                joinShareMoneyActivityHistoryService.removeById(id);
                break;
            case 3:
                // 套餐返现
                invitationActivityJoinHistoryService.removeById(id);
                break;
            case 4:
                // 渠道邀请
                channelActivityHistoryService.removeById(id);
                break;
            case 5:
                // 商户邀请
                merchantJoinRecordService.updateOldRecord(id);
                break;
            default:
                break;
        }
        
        // 新增用户商户绑定
        UserInfoExtra userInfoExtra = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfoExtra)) {
            UserInfoExtra insertUserInfoExtra = UserInfoExtra.builder().merchantId(merchantId).channelEmployeeUid(merchant.getChannelEmployeeUid()).uid(uid).tenantId(tenantId)
                    .delFlag(MerchantConstant.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            
            this.insert(insertUserInfoExtra);
        } else {
            userInfoExtra.setMerchantId(merchantId);
            userInfoExtra.setChannelEmployeeUid(merchant.getChannelEmployeeUid());
            userInfoExtra.setTenantId(tenantId);
            userInfoExtra.setUpdateTime(System.currentTimeMillis());
            
            this.updateByUid(userInfoExtra);
        }
        
        // 获取商户保护期和有效期
        MerchantAttr merchantAttr = merchantAttrService.queryByTenantIdFromCache(merchant.getTenantId());
        if (Objects.isNull(merchantAttr)) {
            log.error("Modify inviter fail! not found merchantAttr, merchantId={}", merchantId);
            return R.fail("120107", "该用户未成功参与活动，无法修改邀请人");
        }
        
        // 新增商户参与记录
        MerchantJoinRecord merchantJoinRecord = this.assembleRecord(merchantId, newInviterUid, MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_SELF, uid, channelEmployeeUid,
                merchantAttr, tenantId);
        merchantJoinRecordService.insertOne(merchantJoinRecord);
        
        // 新增修改记录
        MerchantInviterModifyRecord merchantInviterModifyRecord = MerchantInviterModifyRecord.builder().uid(uid).inviterUid(newInviterUid)
                .inviterName(Optional.ofNullable(userService.queryByUidFromCache(newInviterUid)).orElse(new User()).getName()).oldInviterUid(oldInviterUid)
                .oldInviterName(successInviterVO.getInviterName()).oldInviterSource(inviterSource).merchantId(merchantId).franchiseeId(merchant.getFranchiseeId())
                .tenantId(tenantId).operator(operator).remark(merchantModifyInviterRequest.getRemark()).delFlag(MerchantConstant.DEL_NORMAL).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        
        merchantInviterModifyRecordService.insertOne(merchantInviterModifyRecord);
        
        return R.ok();
    }
    
    private MerchantJoinRecord assembleRecord(Long merchantId, Long inviterUid, Integer inviterType, Long joinUid, Long channelEmployeeUid, MerchantAttr merchantAttr,
            Integer tenantId) {
        long nowTime = System.currentTimeMillis();
        Integer protectionTime = merchantAttr.getInvitationProtectionTime();
        Integer protectionTimeUnit = merchantAttr.getProtectionTimeUnit();
        Integer validTime = merchantAttr.getInvitationValidTime();
        Integer validTimeUnit = merchantAttr.getValidTimeUnit();
        
        // 保护期过期时间
        long protectionExpireTime = nowTime;
        //分钟转毫秒
        if (Objects.equals(protectionTimeUnit, CommonConstant.TIME_UNIT_MINUTES)) {
            protectionExpireTime += protectionTime * TimeConstant.MINUTE_MILLISECOND;
        }
        //小时转毫秒
        if (Objects.equals(protectionTimeUnit, CommonConstant.TIME_UNIT_HOURS)) {
            protectionExpireTime += protectionTime * TimeConstant.HOURS_MILLISECOND;
        }
        
        // 参与有效期过期时间
        long expiredTime = nowTime;
        //分钟转毫秒
        if (Objects.equals(validTimeUnit, CommonConstant.TIME_UNIT_MINUTES)) {
            expiredTime += validTime * TimeConstant.MINUTE_MILLISECOND;
        }
        //小时转毫秒
        if (Objects.equals(validTimeUnit, CommonConstant.TIME_UNIT_HOURS)) {
            expiredTime += validTime * TimeConstant.HOURS_MILLISECOND;
        }
        
        // 生成参与记录
        return MerchantJoinRecord.builder().merchantId(merchantId).channelEmployeeUid(channelEmployeeUid).inviterUid(inviterUid).inviterType(inviterType).joinUid(joinUid)
                .startTime(nowTime).expiredTime(expiredTime).status(MerchantJoinRecordConstant.STATUS_INIT).protectionTime(protectionExpireTime)
                .protectionStatus(MerchantJoinRecordConstant.PROTECTION_STATUS_NORMAL).delFlag(NumberConstant.ZERO).createTime(nowTime).updateTime(nowTime).tenantId(tenantId)
                .build();
    }
}
