package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.UserInfoExtraConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.entity.merchant.MerchantInviterModifyRecord;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.RebateConfig;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import com.xiliulou.electricity.mapper.UserInfoExtraMapper;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterRequest;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterUpdateRequest;
import com.xiliulou.electricity.request.merchant.MerchantPageRequest;
import com.xiliulou.electricity.request.userinfo.UserInfoLimitRequest;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantInviterModifyRecordService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.merchant.MerchantForModifyInviterVO;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;
import com.xiliulou.electricity.vo.merchant.MerchantVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private MerchantInviterModifyRecordService merchantInviterModifyRecordService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Resource
    private OperateRecordUtil operateRecordUtil;
    
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
    public void bindMerchant(UserInfo userInfo, String orderId, Long memberCardId) {
        Long uid = userInfo.getUid();
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderId);
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("BIND MERCHANT WARN!electricityMemberCardOrder is null,uid={},orderId={}", uid, orderId);
            return;
        }
        
        if (Objects.isNull(userInfo.getPayCount()) || userInfo.getPayCount() > 1) {
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
        
        MerchantAttr merchantAttr = merchantAttrService.queryByFranchiseeIdFromCache(merchant.getFranchiseeId());
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
        
        // 加盟商一致校验
        if (!Objects.equals(userInfo.getFranchiseeId(), electricityMemberCardOrder.getFranchiseeId())) {
            log.warn("BIND MERCHANT WARN! userInfo franchisee not equal order franchisee, uid ={} , order.franchiseeId={}", uid, electricityMemberCardOrder.getFranchiseeId());
            return;
        }
        
        if (!Objects.equals(merchant.getFranchiseeId(), electricityMemberCardOrder.getFranchiseeId())) {
            log.warn("BIND MERCHANT WARN! merchant franchisee not equal order franchisee,merchant.id = {} , order.franchiseeId={}", merchant.getId(),
                    electricityMemberCardOrder.getFranchiseeId());
            return;
        }
        
        List<RebateConfig> rebateConfigs = rebateConfigService.listRebateConfigByMid(electricityMemberCardOrder.getMemberCardId());
        if (CollUtil.isEmpty(rebateConfigs)) {
            log.warn("BIND MERCHANT WARN! current member not in rebateConfig, memberId={}", electricityMemberCardOrder.getMemberCardId());
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
            userInfoExtraUpdate.setActivitySource(UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode());
            userInfoExtraUpdate.setInviterUid(merchantJoinRecord.getInviterUid());
            
            this.updateByUid(userInfoExtraUpdate);
            
            MerchantJoinRecord merchantJoinRecordUpdate = new MerchantJoinRecord();
            merchantJoinRecordUpdate.setId(merchantJoinRecord.getId());
            merchantJoinRecordUpdate.setStatus(MerchantJoinRecordConstant.STATUS_SUCCESS);
            merchantJoinRecordUpdate.setUpdateTime(System.currentTimeMillis());
            merchantJoinRecordUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
            merchantJoinRecordUpdate.setPackageId(electricityMemberCardOrder.getMemberCardId());
            merchantJoinRecordUpdate.setPackageType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            // 加盟商
            merchantJoinRecordUpdate.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            merchantJoinRecordUpdate.setSuccessTime(System.currentTimeMillis());
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
    public R selectInviterList(MerchantModifyInviterRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Long uid = request.getUid();
        
        MerchantInviterVO successInviterVO = this.querySuccessInviter(uid);
        if (Objects.isNull(successInviterVO)) {
            log.warn("Modify inviter fail! not found success record, uid={}", uid);
            return R.fail("120107", "该用户未成功参与活动，无法修改邀请人");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("Modify inviter fail! not found userInfo, uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!Objects.equals(userInfo.getTenantId(), tenantId)) {
            log.warn("Modify inviter fail! not found userInfo, uid={}", uid);
            return R.ok();
        }
        
        // 商户列表
        MerchantPageRequest merchantPageRequest = MerchantPageRequest.builder().size(request.getSize()).offset(request.getOffset()).name(request.getMerchantName())
                .tenantId(tenantId).franchiseeId(userInfo.getFranchiseeId()).build();
        List<MerchantVO> merchantList = merchantService.queryList(merchantPageRequest);
        if (CollectionUtils.isEmpty(merchantList)) {
            return R.ok();
        }
        
        Integer inviterSource = successInviterVO.getInviterSource();
        Long inviterMerchantId = successInviterVO.getMerchantId();
        List<MerchantVO> filterMerchantList = merchantList;
        if (Objects.equals(inviterSource, UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode())) {
            // 去除原商户邀请人
            filterMerchantList = merchantList.stream().filter(merchant -> !Objects.equals(merchant.getId(), inviterMerchantId)).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(filterMerchantList)) {
            return R.ok();
        }
        
        List<MerchantForModifyInviterVO> merchantVOList = filterMerchantList.stream().map(item -> {
            MerchantForModifyInviterVO merchantForModifyInviterVO = new MerchantForModifyInviterVO();
            BeanUtils.copyProperties(item, merchantForModifyInviterVO);
            
            return merchantForModifyInviterVO;
        }).collect(Collectors.toList());
        
        return R.ok(merchantVOList);
    }
    
    @Override
    public MerchantInviterVO querySuccessInviter(Long uid) {
        UserInfoExtra userInfoExtra = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfoExtra)) {
            log.warn("querySuccessInviter userInfoExtra not exist, uid={}", uid);
            return null;
        }
        
        Long inviterUid = userInfoExtra.getInviterUid();
        Integer activitySource = userInfoExtra.getActivitySource();
        if (Objects.isNull(inviterUid) || Objects.equals(inviterUid, NumberConstant.ZERO_L) || Objects.isNull(activitySource) || Objects.equals(activitySource,
                NumberConstant.ZERO)) {
            log.warn("querySuccessInviter has no inviter, uid={}", uid);
            return null;
        }
        
        String inviterName = StringUtils.EMPTY;
        Long merchantId = NumberConstant.ZERO_L;
        if (Objects.equals(activitySource, UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode())) {
            MerchantInviterVO merchantInviterVO = this.judgeInviterTypeForMerchant(uid, inviterUid, userInfoExtra.getTenantId());
            if (Objects.nonNull(merchantInviterVO)) {
                inviterName = merchantInviterVO.getInviterName();
                merchantId = merchantInviterVO.getMerchantId();
            }
        } else {
            inviterName = Optional.ofNullable(userInfoService.queryByUidFromDb(inviterUid)).map(UserInfo::getName).orElse("");
        }
        
        return MerchantInviterVO.builder().uid(uid).inviterUid(inviterUid).inviterName(inviterName).inviterSource(activitySource).merchantId(merchantId).build();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R modifyInviter(MerchantModifyInviterUpdateRequest merchantModifyInviterUpdateRequest, Long operator, List<Long> franchiseeIds) {
        Long uid = merchantModifyInviterUpdateRequest.getUid();
        
        //操作频繁
        boolean result = redisService.setNx(CacheConstant.CACHE_MERCHANT_MODIFY_INVITER_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Integer tenantId = TenantContextHolder.getTenantId();
            Long merchantId = merchantModifyInviterUpdateRequest.getMerchantId();
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("Modify inviter fail! not found userInfo, uid={}", uid);
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            
            // 加盟商一致性校验
            Long franchiseeId = userInfo.getFranchiseeId();
            if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId)) {
                log.warn("ModifyInviter WARN! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
                return R.fail("120240", "当前加盟商无权限操作");
            }
            
            if (!Objects.equals(userInfo.getTenantId(), tenantId)) {
                log.warn("Modify inviter fail! not found userInfo, uid={}", uid);
                return R.ok();
            }
            
            Merchant merchant = merchantService.queryByIdFromCache(merchantId);
            if (Objects.isNull(merchant)) {
                log.warn("Modify inviter fail! merchant not exist, merchantId={}", merchantId);
                return R.fail("120212", "商户不存在");
            }
            
            // 获取商户保护期和有效期
            MerchantAttr merchantAttr = merchantAttrService.queryByFranchiseeIdFromCache(merchant.getFranchiseeId());
            if (Objects.isNull(merchantAttr)) {
                log.warn("Modify inviter fail! not found merchantAttr, merchantId={}", merchantId);
                return R.fail("120212", "商户不存在");
            }
            
            if (!Objects.equals(userInfo.getFranchiseeId(), merchant.getFranchiseeId())) {
                log.warn("Modify inviter fail! not same franchisee, uid={}, merchantId={}", uid, merchantId);
                return R.ok();
            }
            
            // 参与成功的记录
            MerchantInviterVO successInviterVO = querySuccessInviter(uid);
            if (Objects.isNull(successInviterVO)) {
                log.warn("Modify inviter fail! not found success record, uid={}", uid);
                return R.fail("120107", "该用户未成功参与活动，无法修改邀请人");
            }
            
            Integer inviterSource = successInviterVO.getInviterSource();
            Long oldInviterUid = successInviterVO.getInviterUid();
            String oldInviterName = successInviterVO.getInviterName();
            Long newInviterUid = merchant.getUid();
            Long channelEmployeeUid = merchant.getChannelEmployeeUid();
            
            // 修改前后不能一样
            if (Objects.equals(oldInviterUid, newInviterUid)) {
                log.warn("Modify inviter fail! inviters can not be the same, uid={}, oldInviterUid={}, newInviterUid={}", uid, oldInviterUid, newInviterUid);
                return R.fail("120108", "只可修改为非当前商户，请重新选择");
            }
            
            // 逻辑删除旧的记录
            switch (inviterSource) {
                case 1:
                    // 邀请返券
                    joinShareActivityHistoryService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                    break;
                case 2:
                    // 邀请返现
                    joinShareMoneyActivityHistoryService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                    break;
                case 3:
                    // 套餐返现：多个活动对应的多条参与记录的delFlag都改为1
                    invitationActivityJoinHistoryService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                    break;
                case 4:
                    // 渠道邀请
                    channelActivityHistoryService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                    break;
                case 5:
                    // 商户邀请
                    merchantJoinRecordService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                    break;
                default:
                    break;
            }
            
            // 新增用户商户绑定
            UserInfoExtra userInfoExtra = this.queryByUidFromCache(uid);
            if (Objects.nonNull(userInfoExtra)) {
                userInfoExtra.setMerchantId(merchantId);
                userInfoExtra.setChannelEmployeeUid(merchant.getChannelEmployeeUid());
                userInfoExtra.setPlaceId(NumberConstant.ZERO_L);
                userInfoExtra.setPlaceUid(NumberConstant.ZERO_L);
                userInfoExtra.setUpdateTime(System.currentTimeMillis());
                userInfoExtra.setActivitySource(UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode());
                userInfoExtra.setInviterUid(newInviterUid);
                userInfoExtra.setLatestActivitySource(UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode());
                
                this.updateByUid(userInfoExtra);
            } else {
                UserInfoExtra insertUserInfoExtra = UserInfoExtra.builder().merchantId(merchantId).channelEmployeeUid(merchant.getChannelEmployeeUid()).uid(uid).tenantId(tenantId)
                        .delFlag(MerchantConstant.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                        .activitySource(UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode()).inviterUid(newInviterUid)
                        .latestActivitySource(UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode()).eleLimit(UserInfoExtraConstant.ELE_LIMIT_YES).build();
                
                this.insert(insertUserInfoExtra);
            }
            
            // 新增商户参与记录
            MerchantJoinRecord merchantJoinRecord = this.assembleRecord(merchantId, newInviterUid, uid, channelEmployeeUid, merchantAttr, tenantId, merchant.getFranchiseeId());
            merchantJoinRecordService.insertOne(merchantJoinRecord);
            
            // 新增修改记录
            MerchantInviterModifyRecord merchantInviterModifyRecord = MerchantInviterModifyRecord.builder().uid(uid).inviterUid(newInviterUid)
                    .inviterName(Optional.ofNullable(merchantService.queryByIdFromCache(merchantId)).orElse(new Merchant()).getName()).oldInviterUid(oldInviterUid)
                    .oldInviterName(oldInviterName).oldInviterSource(inviterSource).merchantId(merchantId).franchiseeId(merchant.getFranchiseeId()).tenantId(tenantId)
                    .operator(operator).remark(merchantModifyInviterUpdateRequest.getRemark()).delFlag(MerchantConstant.DEL_NORMAL).createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            
            merchantInviterModifyRecordService.insertOne(merchantInviterModifyRecord);
            
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_MERCHANT_MODIFY_INVITER_LOCK + uid);
        }
    }
    
    @Override
    public MerchantInviterVO judgeInviterTypeForMerchant(Long joinUid, Long inviterUid, Integer tenantId) {
        Merchant merchant1 = merchantService.queryByUid(inviterUid);
        String inviterName = "";
        Long merchantId = null;
        if (Objects.nonNull(merchant1)) {
            // 邀请人是商户
            inviterName = merchant1.getName();
            merchantId = merchant1.getId();
        } else {
            // 邀请人是场地员工
            MerchantJoinRecord merchantJoinRecord = merchantJoinRecordService.querySuccessRecordByJoinUid(joinUid, tenantId);
            if (Objects.nonNull(merchantJoinRecord) && Objects.equals(merchantJoinRecord.getInviterType(), MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_PLACE_EMPLOYEE)) {
                Merchant merchant2 = merchantService.queryByIdFromCache(merchantJoinRecord.getMerchantId());
                if (Objects.nonNull(merchant2)) {
                    inviterName = merchant2.getName();
                    merchantId = merchant2.getId();
                }
            }
        }
        
        return MerchantInviterVO.builder().uid(joinUid).inviterUid(inviterUid).inviterName(inviterName)
                .inviterSource(UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode()).merchantId(merchantId).build();
    }
    
    private MerchantJoinRecord assembleRecord(Long merchantId, Long inviterUid, Long joinUid, Long channelEmployeeUid, MerchantAttr merchantAttr, Integer tenantId,
            Long franchiseeId) {
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
        return MerchantJoinRecord.builder().merchantId(merchantId).channelEmployeeUid(channelEmployeeUid).inviterUid(inviterUid)
                .inviterType(MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_SELF).joinUid(joinUid).startTime(nowTime).expiredTime(expiredTime)
                .status(MerchantJoinRecordConstant.STATUS_SUCCESS).protectionTime(protectionExpireTime).protectionStatus(MerchantJoinRecordConstant.PROTECTION_STATUS_NORMAL)
                .delFlag(NumberConstant.ZERO).createTime(nowTime).updateTime(nowTime).tenantId(tenantId).modifyInviter(MerchantJoinRecordConstant.MODIFY_INVITER_YES)
                .franchiseeId(franchiseeId).successTime(nowTime).build();
    }
    
    @Override
    public Triple<Boolean, String, String> isLimitPurchase(Long uid, Integer tenantId) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig)) {
            return Triple.of(false, null, null);
        }
        
        if (Objects.equals(electricityConfig.getEleLimit(), ElectricityConfig.ELE_LIMIT_CLOSE)) {
            return Triple.of(false, null, null);
        }
        
        UserInfoExtra userInfoExtra = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfoExtra)) {
            return Triple.of(false, null, null);
        }
        
        if (Objects.isNull(userInfoExtra.getEleLimit()) || Objects.equals(userInfoExtra.getEleLimit(), UserInfoExtraConstant.ELE_LIMIT_NO)) {
            return Triple.of(false, null, null);
        }
        
        List<UserBatteryMemberCardPackage> packageList = userBatteryMemberCardPackageService.selectByUid(uid);
        if (CollectionUtils.isEmpty(packageList)) {
            return Triple.of(false, null, null);
        }
        
        if (Objects.isNull(electricityConfig.getEleLimitCount())) {
            return Triple.of(false, null, null);
        }
        
        if (packageList.size() >= electricityConfig.getEleLimitCount()) {
            return Triple.of(true, "120151", "您已购买了多个尚未使用的换电套餐，为保障您的资金安全，请联系客服后购买");
        }
        
        return Triple.of(false, null, null);
    }
    
    @Override
    public R updateEleLimit(UserInfoLimitRequest request, List<Long> franchiseeIds) {
        Long uid = request.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        UserInfoExtra userInfoExtra = this.queryByUidFromCache(uid);
        
        if (Objects.isNull(userInfo) || Objects.isNull(userInfoExtra) || !Objects.equals(userInfoExtra.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("UpdateEleLimit warn! not found user, uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 加盟商一致性校验
        Long franchiseeId = userInfo.getFranchiseeId();
        if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId)) {
            log.warn("UpdateEleLimit warn! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return R.fail("120240", "当前加盟商无权限操作");
        }
        
        int update = userInfoExtraMapper.updateByUid(UserInfoExtra.builder().eleLimit(request.getEleLimit()).uid(uid).build());
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_INFO_EXTRA + userInfoExtra.getUid());
        });
        
        if (update > 0) {
            Map<String, Object> oldMap = new HashMap<>();
            oldMap.put("eleLimit", userInfoExtra.getEleLimit());
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("user", userInfo.getName() + "/" + userInfo.getPhone());
            newMap.put("eleLimit", request.getEleLimit());
            operateRecordUtil.record(oldMap, newMap);
        }
        
        return R.ok(update);
    }
}
