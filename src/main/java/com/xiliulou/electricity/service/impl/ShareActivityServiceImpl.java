package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.CouponActivityPackage;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareActivityMemberCard;
import com.xiliulou.electricity.entity.ShareActivityOperateRecord;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.entity.ShareActivityRule;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.ShareActivityMapper;
import com.xiliulou.electricity.query.ShareActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.query.ShareActivityRuleQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.CouponActivityPackageService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityMemberCardService;
import com.xiliulou.electricity.service.ShareActivityOperateRecordService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityRuleService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.CouponMemberCardVO;
import com.xiliulou.electricity.vo.CouponVO;
import com.xiliulou.electricity.vo.ShareActivityVO;
import com.xiliulou.electricity.vo.activity.ActivityPackageVO;
import com.xiliulou.electricity.vo.activity.ShareActivityPackageVO;
import com.xiliulou.electricity.vo.activity.ShareActivityRuleVO;
import com.xiliulou.electricity.vo.activity.ShareMoneyAndShareActivityVO;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 活动表(TActivity)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Service("shareActivityService")
@Slf4j
public class ShareActivityServiceImpl implements ShareActivityService {
    
    @Resource
    private ShareActivityMapper shareActivityMapper;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    private ShareActivityRuleService shareActivityRuleService;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    ElectricityCabinetFileService electricityCabinetFileService;
    
    @Autowired
    StorageConfig storageConfig;
    
    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;
    
    @Autowired
    ShareActivityRecordService shareActivityRecordService;
    
    @Autowired
    UserCouponService userCouponService;
    
    @Autowired
    JoinShareActivityRecordService joinShareActivityRecordService;
    
    @Autowired
    JoinShareActivityHistoryService joinShareActivityHistoryService;
    
    @Autowired
    ShareActivityMemberCardService shareActivityMemberCardService;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    ShareActivityOperateRecordService shareActivityOperateRecordService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    
    @Autowired
    private ShareMoneyActivityService shareMoneyActivityService;
    
    
    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("shareActivityHandlerExecutor", 1, "SHARE_ACTIVITY_HANDLER_EXECUTOR");
    
    @Autowired
    private AssertPermissionService assertPermissionService;
    
    @Autowired
    private CouponActivityPackageService couponActivityPackageService;
    
    
    ExecutorService couponMemberExecutorService = XllThreadPoolExecutors.newFixedThreadPool("getCouponMemberExecutor", 3, "GET_COUPON_MEMBER_EXECUTOR");
    
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ShareActivity queryByIdFromCache(Integer id) {
        //先查缓存
        ShareActivity shareActivityCache = redisService.getWithHash(CacheConstant.SHARE_ACTIVITY_CACHE + id, ShareActivity.class);
        if (Objects.nonNull(shareActivityCache)) {
            return shareActivityCache;
        }
        
        //缓存没有再查数据库
        ShareActivity shareActivity = shareActivityMapper.selectById(id);
        if (Objects.isNull(shareActivity)) {
            return null;
        }
        
        //放入缓存
        redisService.saveWithHash(CacheConstant.SHARE_ACTIVITY_CACHE + id, shareActivity);
        return shareActivity;
    }
    
    /**
     * 新增数据
     *
     * @param shareActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R insert(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        if (ObjectUtil.isEmpty(shareActivityAddAndUpdateQuery.getHours()) && ObjectUtil.isEmpty(shareActivityAddAndUpdateQuery.getMinutes())) {
            return R.fail("110209", "有效时间不能为空");
        }
        
        // 加盟商一致性校验
        Long franchiseeId = shareActivityAddAndUpdateQuery.getFranchiseeId();
        List<Long> franchiseeIds = shareActivityAddAndUpdateQuery.getFranchiseeIds();
        if (CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId)) {
            log.info("Insert shareActivity fail! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return R.fail("120240", "当前加盟商无权限操作");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.equals(shareActivityAddAndUpdateQuery.getStatus(), ShareActivity.STATUS_ON)) {
            int count = shareActivityMapper.selectCount(new LambdaQueryWrapper<ShareActivity>().eq(ShareActivity::getTenantId, tenantId)
                    .eq(ShareActivity::getFranchiseeId, shareActivityAddAndUpdateQuery.getFranchiseeId()).eq(ShareActivity::getStatus, ShareActivity.STATUS_ON));
            if (count > 0) {
                return R.fail("ELECTRICITY.00102", "该加盟商已有启用中的邀请活动，请勿重复添加");
            }
        }
        
        //如果是循环领取方式，则优惠券数量不能大于1
        if (Objects.equals(shareActivityAddAndUpdateQuery.getReceiveType(), ShareActivity.RECEIVE_TYPE_CYCLE)
                && shareActivityAddAndUpdateQuery.getShareActivityRuleQueryList().size() > 1) {
            return R.fail("", "活动规则不合法！");
        }
        
        // 邀请标准：去掉实名认证，统一为 购买套餐
        if (ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode().equals(shareActivityAddAndUpdateQuery.getInvitationCriteria())) {
            shareActivityAddAndUpdateQuery.setInvitationCriteria(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode());
        }
        
        //检查所选套餐是否可用
        if (ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode().equals(shareActivityAddAndUpdateQuery.getInvitationCriteria())) {
            //检查是否有选择（换电,租车,车电一体）套餐信息
            if (CollectionUtils.isEmpty(shareActivityAddAndUpdateQuery.getBatteryPackages()) && CollectionUtils.isEmpty(shareActivityAddAndUpdateQuery.getCarRentalPackages())
                    && CollectionUtils.isEmpty(shareActivityAddAndUpdateQuery.getCarWithBatteryPackages())) {
                return R.fail("000201", "请选择套餐信息");
            }
            
            Triple<Boolean, String, Object> verifyResult = verifySelectedPackages(shareActivityAddAndUpdateQuery);
            if (Boolean.FALSE.equals(verifyResult.getLeft())) {
                return R.fail("000076", (String) verifyResult.getRight());
            }
        }
        
        List<ShareActivityRuleQuery> shareActivityRuleQueryList = shareActivityAddAndUpdateQuery.getShareActivityRuleQueryList();
        shareActivityAddAndUpdateQuery.setType(ShareActivity.FRANCHISEE);
        
        ShareActivity shareActivity = new ShareActivity();
        BeanUtil.copyProperties(shareActivityAddAndUpdateQuery, shareActivity);
        shareActivity.setCreateTime(System.currentTimeMillis());
        shareActivity.setUpdateTime(System.currentTimeMillis());
        shareActivity.setTenantId(tenantId);
        shareActivity.setHours(Objects.isNull(shareActivityAddAndUpdateQuery.getHours()) ? NumberConstant.ZERO : (shareActivityAddAndUpdateQuery.getHours()));
        shareActivity.setMinutes(Objects.isNull(shareActivityAddAndUpdateQuery.getMinutes()) ? NumberConstant.ZERO : (shareActivityAddAndUpdateQuery.getMinutes()));
        shareActivity.setFranchiseeId(shareActivityAddAndUpdateQuery.getFranchiseeId().intValue());
        
        int insert = shareActivityMapper.insert(shareActivity);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            
            //添加优惠
            if (ObjectUtil.isNotEmpty(shareActivityRuleQueryList)) {
                for (ShareActivityRuleQuery shareActivityRuleQuery : shareActivityRuleQueryList) {
                    ShareActivityRule.ShareActivityRuleBuilder activityBindCouponBuild = ShareActivityRule.builder().activityId(shareActivity.getId())
                            .couponId(shareActivityRuleQuery.getCouponId()).triggerCount(shareActivityRuleQuery.getTriggerCount()).createTime(System.currentTimeMillis())
                            .updateTime(System.currentTimeMillis());
                    ShareActivityRule shareActivityRule = activityBindCouponBuild.build();
                    shareActivityRuleService.insert(shareActivityRule);
                }
            }
            
            //保存可发优惠券的套餐
			/*List<ShareActivityMemberCard> shareActivityMemberCards = buildShareActivityMemberCard(shareActivity, shareActivityAddAndUpdateQuery.getMembercardIds());
			if (CollectionUtils.isNotEmpty(shareActivityMemberCards)) {
				shareActivityMemberCardService.batchInsert(shareActivityMemberCards);
			}*/
            
            //保存新的套餐设置信息，套餐范围增加，包含了换电，租车和车电一体的套餐
            List<ShareActivityMemberCard> shareActivityMemberCards = buildShareActivityPackages(shareActivity.getId().longValue(), shareActivityAddAndUpdateQuery);
            if (CollectionUtils.isNotEmpty(shareActivityMemberCards)) {
                shareActivityMemberCardService.batchInsert(shareActivityMemberCards);
            }
            //获取已选择的套餐
            //List<Long> packageList = shareActivityMemberCards.stream().map(ShareActivityMemberCard::getMemberCardId).collect(Collectors.toList());
            //shareActivityOperateRecordService.insert(buildShareActivityOperateRecord(shareActivity.getId().longValue(),shareActivity.getName(),packageList));
            
            //3.0版本，针对套餐需要做区分，新版的本的套餐多了租车和车电一体。之前只有换电套餐一种
            List<ActivityPackageVO> activityPackageVOS = getActivityPackages(shareActivityMemberCards);
            ShareActivityPackageVO shareActivityPackageVO = new ShareActivityPackageVO();
            shareActivityPackageVO.setPackages(activityPackageVOS);
            shareActivityOperateRecordService.insert(buildActivityOperateRecord(shareActivity, shareActivityPackageVO));
            
            return null;
        });
        
        if (insert > 0) {
            return R.ok(shareActivity.getId());
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }
    
    /**
     * 校验所选的套餐是否可用
     *
     * @param shareActivityAddAndUpdateQuery
     * @return
     */
    private Triple<Boolean, String, Object> verifySelectedPackages(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        List<Long> electricityPackages = shareActivityAddAndUpdateQuery.getBatteryPackages();
        for (Long packageId : electricityPackages) {
            //检查所选套餐是否存在，并且可用
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
            
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, "000202", "换电套餐不存在");
            }
        }
        
        List<Long> carRentalPackages = shareActivityAddAndUpdateQuery.getCarRentalPackages();
        for (Long packageId : carRentalPackages) {
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000203", "租车套餐不存在");
            }
        }
        
        List<Long> carElectricityPackages = shareActivityAddAndUpdateQuery.getCarWithBatteryPackages();
        for (Long packageId : carElectricityPackages) {
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000204", "车电一体套餐不存在");
            }
        }
        return Triple.of(true, "", null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> updateShareActivity(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        ShareActivity shareActivity = queryByIdFromCache(shareActivityAddAndUpdateQuery.getId());
        if (Objects.isNull(shareActivity)) {
            log.error("update Activity ERROR! not found Activity ! ActivityId={} ", shareActivityAddAndUpdateQuery.getId());
            return Triple.of(false, "ELECTRICITY.0069", "未找到活动");
        }
        
        // 租户一致性校验
        if (!Objects.equals(TenantContextHolder.getTenantId(), shareActivity.getTenantId())) {
            return Triple.of(true, "", "");
        }
        
        // 加盟商一致性校验
        Integer franchiseeId = shareActivity.getFranchiseeId();
        List<Long> franchiseeIds = shareActivityAddAndUpdateQuery.getFranchiseeIds();
        if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId.longValue())) {
            log.info("Update shareActivity fail! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        shareActivity.setName(shareActivityAddAndUpdateQuery.getName());
        
        DbUtils.dbOperateSuccessThenHandleCache(shareActivityMapper.updateById(shareActivity), i -> {
            redisService.delete(CacheConstant.SHARE_ACTIVITY_CACHE + shareActivity.getId());
            
            //删除绑定的套餐
            shareActivityMemberCardService.deleteByActivityId(shareActivityAddAndUpdateQuery.getId());

			/*List<ShareActivityMemberCard> shareActivityMemberCards = buildShareActivityMemberCard(shareActivityUpdate, shareActivityQuery.getMembercardIds());
			if (CollectionUtils.isNotEmpty(shareActivityMemberCards)) {
				shareActivityMemberCardService.batchInsert(shareActivityMemberCards);
			}*/
            
            //保存新的套餐设置信息，套餐范围增加，包含了换电，租车和车电一体的套餐
            List<ShareActivityMemberCard> shareActivityMemberCards = buildShareActivityPackages(shareActivity.getId().longValue(), shareActivityAddAndUpdateQuery);
            if (CollectionUtils.isNotEmpty(shareActivityMemberCards)) {
                shareActivityMemberCardService.batchInsert(shareActivityMemberCards);
            }
            //获取已选择的套餐
            //List<Long> packageList = shareActivityMemberCards.stream().map(ShareActivityMemberCard::getMemberCardId).collect(Collectors.toList());
            
            //3.0版本，针对套餐需要做区分，新版的本的套餐多了租车和车电一体。之前只有换电套餐一种
            List<ActivityPackageVO> activityPackageVOS = getActivityPackages(shareActivityMemberCards);
            ShareActivityPackageVO shareActivityPackageVO = new ShareActivityPackageVO();
            shareActivityPackageVO.setPackages(activityPackageVOS);
            
            shareActivityOperateRecordService.insert(buildActivityOperateRecord(shareActivity, shareActivityPackageVO));
        });
        
        return Triple.of(true, "", "");
    }
    
    /**
     * 修改数据(暂只支持上下架）
     *
     * @param shareActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R update(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        ShareActivity oldShareActivity = queryByIdFromCache(shareActivityAddAndUpdateQuery.getId());
        if (Objects.isNull(oldShareActivity)) {
            log.error("update Activity  ERROR! not found Activity ! ActivityId={} ", shareActivityAddAndUpdateQuery.getId());
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }
        
        // 租户一致性校验
        Integer tenantId = TenantContextHolder.getTenantId();
        if (!Objects.equals(TenantContextHolder.getTenantId(), oldShareActivity.getTenantId())) {
            return R.ok();
        }
        
        // 加盟商一致性校验
        Integer franchiseeId = oldShareActivity.getFranchiseeId();
        List<Long> franchiseeIds = shareActivityAddAndUpdateQuery.getFranchiseeIds();
        if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId.longValue())) {
            log.info("Update shareActivity fail! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return R.fail("120240", "当前加盟商无权限操作");
        }
        
        // 判断该加盟商是否有启用的活动，有则不能启用
        if (Objects.nonNull(franchiseeId)) {
            if (Objects.equals(shareActivityAddAndUpdateQuery.getStatus(), ShareActivity.STATUS_ON)) {
                int count = shareActivityMapper.selectCount(
                        new LambdaQueryWrapper<ShareActivity>().eq(ShareActivity::getTenantId, tenantId).eq(ShareActivity::getFranchiseeId, franchiseeId)
                                .eq(ShareActivity::getStatus, ShareActivity.STATUS_ON));
                if (count > 0) {
                    return R.fail("ELECTRICITY.00102", "该加盟商已有启用中的邀请活动，请勿重复添加");
                }
            }
        } else {
            //查询该租户是否有邀请活动，有则不能启用
            if (Objects.equals(shareActivityAddAndUpdateQuery.getStatus(), ShareActivity.STATUS_ON)) {
                int count = shareActivityMapper.selectCount(new LambdaQueryWrapper<ShareActivity>().eq(ShareActivity::getTenantId, tenantId).isNull(ShareActivity::getFranchiseeId)
                        .eq(ShareActivity::getStatus, ShareActivity.STATUS_ON).eq(ShareActivity::getType, ShareActivity.SYSTEM));
                if (count > 0) {
                    return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
                }
            }
        }
        
        BeanUtil.copyProperties(shareActivityAddAndUpdateQuery, oldShareActivity);
        oldShareActivity.setUpdateTime(System.currentTimeMillis());
        
        int update = shareActivityMapper.updateById(oldShareActivity);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.SHARE_ACTIVITY_CACHE + oldShareActivity.getId());
            
            //如果是下架活动，则参与邀请记录改为活动已下架
            if (Objects.equals(shareActivityAddAndUpdateQuery.getStatus(), ShareActivity.STATUS_OFF)) {
                
                //修改邀请状态
                JoinShareActivityRecord joinShareActivityRecord = new JoinShareActivityRecord();
                joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_OFF);
                joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
                joinShareActivityRecord.setActivityId(shareActivityAddAndUpdateQuery.getId());
                joinShareActivityRecordService.updateByActivityId(joinShareActivityRecord);
                
                //修改历史记录状态
                JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
                joinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_OFF);
                joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
                joinShareActivityHistory.setActivityId(shareActivityAddAndUpdateQuery.getId());
                joinShareActivityHistoryService.updateByActivityId(joinShareActivityHistory);
                
            }
            return null;
        });
        
        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }
    
    @Override
    @Slave
    public R queryList(ShareActivityQuery shareActivityQuery) {
        List<ShareActivity> shareActivityList = shareActivityMapper.queryList(shareActivityQuery);
        List<ShareActivityVO> shareActivityVOList = Lists.newArrayList();
        
        for (ShareActivity shareActivity : shareActivityList) {
            ShareActivityVO shareActivityVO = new ShareActivityVO();
            BeanUtil.copyProperties(shareActivity, shareActivityVO);
            
            // 如果单位小时，timeType=1  如果单位分钟，timeType=2
            if (Objects.nonNull(shareActivity.getHours()) && !Objects.equals(shareActivity.getHours(), NumberConstant.ZERO)) {
                shareActivityVO.setHours(shareActivity.getHours().doubleValue());
                shareActivityVO.setTimeType(NumberConstant.ONE);
            } else {
                shareActivityVO.setMinutes(Objects.isNull(shareActivity.getMinutes()) ? NumberConstant.ZERO_L : shareActivity.getMinutes().longValue());
                shareActivityVO.setTimeType(NumberConstant.TWO);
            }
            
            if (ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode().equals(shareActivity.getInvitationCriteria())) {
                shareActivityVO.setBatteryPackages(getBatteryPackages(shareActivity.getId()));
                shareActivityVO.setCarRentalPackages(getCarBatteryPackages(shareActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
                shareActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(shareActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
            }
            shareActivityVOList.add(shareActivityVO);
            
            Integer franchiseeId = shareActivity.getFranchiseeId();
            if (Objects.nonNull(franchiseeId)) {
                shareActivityVO.setFranchiseeName(
                        Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId.longValue())).map(Franchisee::getName).orElse(StringUtils.EMPTY));
            }
        }
        
        return R.ok(shareActivityVOList);
    }
    
    @Override
    public R queryInfo(Integer id) {
        ShareActivity shareActivity = queryByIdFromCache(id);
        if (Objects.isNull(shareActivity)) {
            log.error("queryInfo Activity  ERROR! not found Activity ! ActivityId:{} ", id);
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }
        
        if (!Objects.equals(shareActivity.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
        
        ShareActivityVO shareActivityVO = new ShareActivityVO();
        BeanUtil.copyProperties(shareActivity, shareActivityVO);
        
        //小活动
        getCouponVOList(shareActivityVO);
        return R.ok(shareActivityVO);
    }
    
    private void getCouponVOList(ShareActivityVO shareActivityVO) {
        List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivity(shareActivityVO.getId());
        if (ObjectUtil.isEmpty(shareActivityRuleList)) {
            return;
        }
        
        List<CouponVO> couponVOList = new ArrayList<>();
        for (ShareActivityRule shareActivityRule : shareActivityRuleList) {
            CouponVO couponVO = new CouponVO();
            couponVO.setTriggerCount(shareActivityRule.getTriggerCount());
            Integer couponId = shareActivityRule.getCouponId();
            //优惠券名称
            Coupon coupon = couponService.queryByIdFromCache(couponId);
            if (Objects.nonNull(coupon)) {
                couponVO.setCoupon(coupon);
            }
            couponVOList.add(couponVO);
        }
        
        shareActivityVO.setCouponVOList(couponVOList);
    }
    
    private void getUserCouponVOList(ShareActivityVO shareActivityVO, TokenUser user) {
        List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivity(shareActivityVO.getId());
        if (ObjectUtil.isEmpty(shareActivityRuleList)) {
            return;
        }
        
        //邀请好友数
        int count = 0;
        //可用邀请好友数
        int availableCount = 0;
        ShareActivityRecord shareActivityRecord = shareActivityRecordService.queryByUid(user.getUid(), shareActivityVO.getId());
        if (Objects.nonNull(shareActivityRecord)) {
            count = shareActivityRecord.getCount();
            availableCount = shareActivityRecord.getAvailableCount();
        }
        
        //
        List<CouponVO> couponVOList = new ArrayList<>();
        int couponCount = 0;
        for (ShareActivityRule shareActivityRule : shareActivityRuleList) {
            
            CouponVO couponVO = new CouponVO();
            couponVO.setTriggerCount(shareActivityRule.getTriggerCount());
            Integer couponId = shareActivityRule.getCouponId();
            
            //是否可以领取优惠券
            if (shareActivityRule.getTriggerCount() <= availableCount) {
                couponVO.setIsGet(CouponVO.IS_NOT_RECEIVE);
            } else {
                couponVO.setIsGet(CouponVO.IS_CANNOT_RECEIVE);
            }
            
            //优惠券名称
            Coupon coupon = couponService.queryByIdFromCache(couponId);
            if (Objects.nonNull(coupon)) {
                
                //是否领取该活动该优惠券
                UserCoupon userCoupon = userCouponService.queryByActivityIdAndCouponId(shareActivityVO.getId(), shareActivityRule.getId(), coupon.getId(), user.getUid());
                if (Objects.nonNull(userCoupon)) {
                    couponVO.setIsGet(CouponVO.IS_RECEIVED);
                    couponCount = couponCount + 1;
                }
                couponVO.setCoupon(coupon);
                getCouponPackage(coupon, couponVO);
            }
            
            couponVOList.add(couponVO);
        }
        
        //邀请好友数
        shareActivityVO.setCount(count);
        //可用邀请好友数
        shareActivityVO.setAvailableCount(availableCount);
        
        //领卷次数
        shareActivityVO.setCouponCount(couponCount);
        
        shareActivityVO.setCouponVOList(couponVOList);
    }
    
    @Override
    @Slave
    public R queryCount(ShareActivityQuery shareActivityQuery) {
        Integer count = shareActivityMapper.queryCount(shareActivityQuery);
        return R.ok(count);
    }
    
    @Override
    public R activityInfo() {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ACTIVITY ERROR!not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //用户是否可用
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("ACTIVITY WARN!not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("ACTIVITY WARN!user not auth,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        //邀请活动
        ShareActivity shareActivity = this.queryOnlineActivity(tenantId, userInfo.getFranchiseeId());
        if (Objects.isNull(shareActivity)) {
            log.warn("ACTIVITY WARN!not found Activity,tenantId={},uid={}", tenantId, user.getUid());
            return R.ok();
        }
        
        ShareActivityVO shareActivityVO = new ShareActivityVO();
        BeanUtil.copyProperties(shareActivity, shareActivityVO);
        
        // 兼容旧版小程序
        if (Objects.nonNull(shareActivity.getHours()) && !Objects.equals(shareActivity.getHours(), NumberConstant.ZERO)) {
            shareActivityVO.setHours(shareActivity.getHours().doubleValue());
            shareActivityVO.setMinutes(shareActivity.getHours() * TimeConstant.HOURS_MINUTE);
            shareActivityVO.setTimeType(NumberConstant.ONE);
        }
        if (Objects.nonNull(shareActivity.getMinutes()) && !Objects.equals(shareActivity.getMinutes(), NumberConstant.ZERO)) {
            shareActivityVO.setMinutes(shareActivity.getMinutes().longValue());
            shareActivityVO.setHours(
                    Math.round((double) shareActivity.getMinutes().longValue() / TimeConstant.HOURS_MINUTE * NumberConstant.ONE_HUNDRED_D) / NumberConstant.ONE_HUNDRED_D);
            shareActivityVO.setTimeType(NumberConstant.TWO);
        }
        
        if (Objects.equals(shareActivity.getReceiveType(), ShareActivity.RECEIVE_TYPE_CYCLE)) {
            acquireUserCouponInfo(shareActivityVO, user.getUid());
        } else {
            getUserCouponVOList(shareActivityVO, user);
        }
        
        //设置邀请活动对应的套餐信息
        if (ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode().equals(shareActivityVO.getInvitationCriteria())) {
            shareActivityVO.setBatteryPackages(getBatteryPackages(shareActivity.getId()));
            shareActivityVO.setCarRentalPackages(getCarBatteryPackages(shareActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
            shareActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(shareActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
        }
        
        Integer franchiseeId = shareActivity.getFranchiseeId();
        if (Objects.nonNull(franchiseeId)) {
            shareActivityVO.setFranchiseeName(
                    Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId.longValue())).map(Franchisee::getName).orElse(StringUtils.EMPTY));
        }
        
        return R.ok(shareActivityVO);
    }
    
    private void acquireUserCouponInfo(ShareActivityVO shareActivityVO, Long uid) {
        List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivity(shareActivityVO.getId());
        if (CollectionUtils.isEmpty(shareActivityRuleList)) {
            return;
        }
        
        //邀请好友数
        int count = 0;
        //可用邀请好友数
        int availableCount = 0;
        //领券次数
        int couponCount = 0;
        
        //循环领取的领取规则有且仅有一个
        ShareActivityRule shareActivityRule = shareActivityRuleList.get(0);
        
        ShareActivityRecord shareActivityRecord = shareActivityRecordService.queryByUid(uid, shareActivityVO.getId());
        if (Objects.nonNull(shareActivityRecord)) {
            count = shareActivityRecord.getCount();
            availableCount = shareActivityRecord.getAvailableCount();
        }
        
        shareActivityVO.setCount(count);
        shareActivityVO.setAvailableCount(availableCount);
        
        Coupon coupon = couponService.queryByIdFromCache(shareActivityRule.getCouponId());
        if (Objects.nonNull(coupon)) {
            List<UserCoupon> userCoupons = userCouponService.selectListByActivityIdAndCouponId(shareActivityVO.getId(), shareActivityRule.getId(), coupon.getId(), uid);
            if (Objects.nonNull(userCoupons)) {
                couponCount = userCoupons.size();
            }
        }
        
        List<CouponVO> couponVOList = new ArrayList<>();
        
        //不可领取
        if (Objects.isNull(shareActivityRecord) || Objects.isNull(shareActivityRecord.getAvailableCount()) || Objects.isNull(shareActivityRule.getTriggerCount())
                || shareActivityRecord.getAvailableCount() <= 0 || shareActivityRule.getTriggerCount() <= 0
                || shareActivityRecord.getAvailableCount() < shareActivityRule.getTriggerCount()) {
            
            CouponVO couponVO = new CouponVO();
            couponVO.setTriggerCount(shareActivityRule.getTriggerCount());
            couponVO.setCoupon(coupon);
            couponVO.setIsGet(CouponVO.IS_CANNOT_RECEIVE);
            getCouponPackage(coupon, couponVO);
            
            couponVOList.add(couponVO);
            
            shareActivityVO.setCouponCount(couponCount);
            shareActivityVO.setCouponVOList(couponVOList);
            return;
        }
        
        //可领取
        if (shareActivityRecord.getAvailableCount() >= shareActivityRule.getTriggerCount()) {
            CouponVO couponVO = new CouponVO();
            couponVO.setCoupon(coupon);
            couponVO.setIsGet(CouponVO.IS_NOT_RECEIVE);
            couponVO.setTriggerCount(shareActivityRule.getTriggerCount());
            getCouponPackage(coupon, couponVO);
            
            couponVOList.add(couponVO);
        }
        // couponVOList
        
        shareActivityVO.setCouponCount(couponCount);
        shareActivityVO.setCouponVOList(couponVOList);
    }
    
    private void getCouponPackage(Coupon coupon, CouponVO couponVO) {
        if (Objects.isNull(coupon)) {
            return;
        }
        if (Objects.isNull(coupon.getId())) {
            return;
        }
        Long couponId = Long.valueOf(coupon.getId());
        
        List<CouponActivityPackage> packages = couponActivityPackageService.findActivityPackagesByCouponId(couponId);
        if (CollUtil.isEmpty(packages)) {
            return;
        }
        
        List<CouponMemberCardVO> batteryCouponCards = CollUtil.newArrayList();
        CompletableFuture<Void> batteryCouponFuture = CompletableFuture.runAsync(() -> {
            // 换电优惠券ids
            List<Long> batteryPackageIds = packages.parallelStream().filter(e -> Objects.equals(e.getPackageType(), PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode()))
                    .map(CouponActivityPackage::getPackageId).collect(Collectors.toList());
            
            batteryPackageIds.forEach(t -> {
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(t);
                if (Objects.nonNull(batteryMemberCard)) {
                    batteryCouponCards.add(new CouponMemberCardVO(batteryMemberCard.getId(), batteryMemberCard.getName()));
                }
            });
        }, couponMemberExecutorService).exceptionally(e -> {
            log.error("batteryCouponFuture is error ", e);
            return null;
        });
        
        List<CouponMemberCardVO> carRentalCouponCards = CollUtil.newArrayList();
        CompletableFuture<Void> carRentalCouponFuture = CompletableFuture.runAsync(() -> {
            List<Long> carPackageIds = packages.parallelStream().filter(e -> Objects.equals(e.getPackageType(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()))
                    .map(CouponActivityPackage::getPackageId).collect(Collectors.toList());
            
            carPackageIds.forEach(t -> {
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(t);
                if (Objects.nonNull(carRentalPackagePO)) {
                    carRentalCouponCards.add(new CouponMemberCardVO(carRentalPackagePO.getId(), carRentalPackagePO.getName()));
                }
            });
        }, couponMemberExecutorService).exceptionally(e -> {
            log.error("carRentalCouponFuture is error ", e);
            return null;
        });
        
        List<CouponMemberCardVO> carWithBatteryCouponCards = CollUtil.newArrayList();
        CompletableFuture<Void> carWithBatteryCouponFuture = CompletableFuture.runAsync(() -> {
            List<Long> carWithBatteryPackageIds = packages.parallelStream().filter(e -> Objects.equals(e.getPackageType(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()))
                    .map(CouponActivityPackage::getPackageId).collect(Collectors.toList());
            carWithBatteryPackageIds.forEach(t -> {
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(t);
                if (Objects.nonNull(carRentalPackagePO)) {
                    carWithBatteryCouponCards.add(new CouponMemberCardVO(carRentalPackagePO.getId(), carRentalPackagePO.getName()));
                }
            });
        }, couponMemberExecutorService).exceptionally(e -> {
            log.error("carWithBatteryCouponFuture is error ", e);
            return null;
        });
        
        try {
            CompletableFuture.allOf(batteryCouponFuture, carRentalCouponFuture, carWithBatteryCouponFuture).get();
        } catch (Exception e) {
            log.warn("getCouponPackage is error", e);
        }
        
        couponVO.setBatteryCouponCards(batteryCouponCards);
        couponVO.setCarRentalCouponCards(carRentalCouponCards);
        couponVO.setCarWithBatteryCouponCards(carWithBatteryCouponCards);
    }
    
    @Override
    public Triple<Boolean, String, Object> shareActivityDetail(Integer id) {
        ShareActivity shareActivity = this.queryByIdFromCache(id);
        if (Objects.isNull(shareActivity) || !Objects.equals(shareActivity.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.0069", "未找到活动");
        }
        
        ShareActivityVO shareActivityVO = new ShareActivityVO();
        BeanUtil.copyProperties(shareActivity, shareActivityVO);
        
        // 如果单位小时，timeType=1  如果单位分钟，timeType=2
        if (Objects.nonNull(shareActivity.getHours()) && !Objects.equals(shareActivity.getHours(), NumberConstant.ZERO)) {
            shareActivityVO.setHours(shareActivity.getHours().doubleValue());
            shareActivityVO.setTimeType(NumberConstant.ONE);
        } else {
            shareActivityVO.setMinutes(Objects.isNull(shareActivity.getMinutes()) ? NumberConstant.ZERO_L : shareActivity.getMinutes().longValue());
            shareActivityVO.setTimeType(NumberConstant.TWO);
        }
        
        //重新设置购买的套餐信息,设置换电套餐信息
        List<BatteryMemberCardVO> batteryPackageList = getBatteryPackages(shareActivity.getId());
        if (CollectionUtils.isNotEmpty(batteryPackageList)) {
            shareActivityVO.setBatteryPackages(batteryPackageList);
        }
        
        //设置租车套餐信息
        List<BatteryMemberCardVO> carRentalPackageList = getCarBatteryPackages(shareActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode());
        if (CollectionUtils.isNotEmpty(carRentalPackageList)) {
            shareActivityVO.setCarRentalPackages(carRentalPackageList);
        }
        
        //设置车电一体套餐信息
        List<BatteryMemberCardVO> carWithBatteryPackageList = getCarBatteryPackages(shareActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode());
        if (CollectionUtils.isNotEmpty(carWithBatteryPackageList)) {
            shareActivityVO.setCarWithBatteryPackages(carWithBatteryPackageList);
        }
        
        List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivity(shareActivity.getId());
        if (CollectionUtils.isNotEmpty(shareActivityRuleList)) {
            shareActivityVO.setShareActivityRuleQueryList(getShareActivityRules(shareActivityRuleList));
        }
        
        Integer franchiseeId = shareActivity.getFranchiseeId();
        if (Objects.nonNull(franchiseeId)) {
            shareActivityVO.setFranchiseeName(
                    Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId.longValue())).map(Franchisee::getName).orElse(StringUtils.EMPTY));
        }
        
        return Triple.of(true, "", shareActivityVO);
    }
    
    private List<ShareActivityRuleVO> getShareActivityRules(List<ShareActivityRule> shareActivityRuleList) {
        List<ShareActivityRuleVO> shareActivityRuleVOList = Lists.newArrayList();
        for (ShareActivityRule shareActivityRule : shareActivityRuleList) {
            ShareActivityRuleVO shareActivityRuleVO = new ShareActivityRuleVO();
            BeanUtil.copyProperties(shareActivityRule, shareActivityRuleVO);
            Integer couponId = shareActivityRule.getCouponId();
            Coupon coupon = couponService.queryByIdFromCache(couponId);
            if (Objects.nonNull(coupon)) {
                shareActivityRuleVO.setCouponName(coupon.getName());
            }
            shareActivityRuleVOList.add(shareActivityRuleVO);
        }
        
        return shareActivityRuleVOList;
        
    }
    
    private List<BatteryMemberCardVO> getBatteryPackages(Integer activityId) {
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<ShareActivityMemberCard> batteryPackageList = shareActivityMemberCardService.selectByActivityIdAndPackageType(activityId,
                PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        for (ShareActivityMemberCard shareActivityMemberCard : batteryPackageList) {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(shareActivityMemberCard.getMemberCardId());
            if (Objects.nonNull(batteryMemberCard) && CommonConstant.DEL_N.equals(batteryMemberCard.getDelFlag())) {
                BeanUtils.copyProperties(batteryMemberCard, batteryMemberCardVO);
                memberCardVOList.add(batteryMemberCardVO);
            }
        }
        return memberCardVOList;
    }
    
    private List<BatteryMemberCardVO> getCarBatteryPackages(Integer activityId, Integer packageType) {
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<ShareActivityMemberCard> batteryPackageList = shareActivityMemberCardService.selectByActivityIdAndPackageType(activityId, packageType);
        for (ShareActivityMemberCard shareActivityMemberCard : batteryPackageList) {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(shareActivityMemberCard.getMemberCardId());
            if (Objects.nonNull(carRentalPackagePO) && CommonConstant.DEL_N.equals(carRentalPackagePO.getDelFlag())) {
                batteryMemberCardVO.setId(carRentalPackagePO.getId());
                batteryMemberCardVO.setName(carRentalPackagePO.getName());
                batteryMemberCardVO.setCreateTime(carRentalPackagePO.getCreateTime());
                memberCardVOList.add(batteryMemberCardVO);
            }
            
        }
        
        return memberCardVOList;
    }
    
    @Override
    @Slave
    public ShareActivity queryByStatus(Integer activityId) {
        return shareActivityMapper.selectOne(new LambdaQueryWrapper<ShareActivity>().eq(ShareActivity::getId, activityId).eq(ShareActivity::getStatus, ShareActivity.STATUS_ON));
    }
    
    private List<ShareActivityMemberCard> buildShareActivityPackages(Long shareActivityId, ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        List<ShareActivityMemberCard> shareActivityPackages = Lists.newArrayList();
        List<Long> batteryPackages = shareActivityAddAndUpdateQuery.getBatteryPackages();
        for (Long packageId : batteryPackages) {
            ShareActivityMemberCard batteryPackage = buildShareActivityMemberCard(shareActivityId, packageId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            shareActivityPackages.add(batteryPackage);
        }
        
        List<Long> carRentalPackages = shareActivityAddAndUpdateQuery.getCarRentalPackages();
        for (Long packageId : carRentalPackages) {
            ShareActivityMemberCard carRentalPackage = buildShareActivityMemberCard(shareActivityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode());
            shareActivityPackages.add(carRentalPackage);
        }
        
        List<Long> carWithBatteryPackages = shareActivityAddAndUpdateQuery.getCarWithBatteryPackages();
        for (Long packageId : carWithBatteryPackages) {
            ShareActivityMemberCard carWithBatteryPackage = buildShareActivityMemberCard(shareActivityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode());
            shareActivityPackages.add(carWithBatteryPackage);
        }
        
        return shareActivityPackages;
    }
    
    private ShareActivityMemberCard buildShareActivityMemberCard(Long shareActivityId, Long packageId, Integer packageType) {
        ShareActivityMemberCard shareActivityMemberCard = new ShareActivityMemberCard();
        shareActivityMemberCard.setActivityId(shareActivityId);
        shareActivityMemberCard.setMemberCardId(packageId);
        shareActivityMemberCard.setPackageType(packageType);
        shareActivityMemberCard.setTenantId(TenantContextHolder.getTenantId());
        shareActivityMemberCard.setCreateTime(System.currentTimeMillis());
        shareActivityMemberCard.setUpdateTime(System.currentTimeMillis());
        return shareActivityMemberCard;
    }
    
    @Deprecated
    private List<ShareActivityMemberCard> buildShareActivityMemberCard(ShareActivity shareActivity, List<Long> membercardIds) {
        List<ShareActivityMemberCard> list = Lists.newArrayList();
        
        for (Long membercardId : membercardIds) {
            ShareActivityMemberCard shareActivityMemberCard = new ShareActivityMemberCard();
            shareActivityMemberCard.setActivityId(shareActivity.getId().longValue());
            shareActivityMemberCard.setMemberCardId(membercardId);
            shareActivityMemberCard.setTenantId(TenantContextHolder.getTenantId());
            shareActivityMemberCard.setCreateTime(System.currentTimeMillis());
            shareActivityMemberCard.setUpdateTime(System.currentTimeMillis());
            list.add(shareActivityMemberCard);
        }
        
        return list;
    }
    
    @Deprecated
    private ShareActivityOperateRecord buildShareActivityOperateRecord(Long id, String name, List<Long> membercardIds) {
        ShareActivityOperateRecord shareActivityOperateRecord = new ShareActivityOperateRecord();
        shareActivityOperateRecord.setUid(SecurityUtils.getUid());
        shareActivityOperateRecord.setShareActivityId(id);
        shareActivityOperateRecord.setName(name);
        shareActivityOperateRecord.setMemberCard(JsonUtil.toJson(membercardIds));
        shareActivityOperateRecord.setTenantId(TenantContextHolder.getTenantId());
        shareActivityOperateRecord.setCreateTime(System.currentTimeMillis());
        shareActivityOperateRecord.setUpdateTime(System.currentTimeMillis());
        return shareActivityOperateRecord;
    }
    
    private ShareActivityOperateRecord buildActivityOperateRecord(ShareActivity shareActivity, ShareActivityPackageVO shareActivityPackageVO) {
        ShareActivityOperateRecord shareActivityOperateRecord = new ShareActivityOperateRecord();
        shareActivityOperateRecord.setUid(SecurityUtils.getUid());
        shareActivityOperateRecord.setShareActivityId(shareActivity.getId().longValue());
        shareActivityOperateRecord.setName(shareActivity.getName());
        //shareActivityOperateRecord.setMemberCard(JsonUtil.toJson(shareActivityPackageVO));
        shareActivityOperateRecord.setPackageInfo(JsonUtil.toJson(shareActivityPackageVO));
        shareActivityOperateRecord.setTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(shareActivity.getFranchiseeId())) {
            shareActivityOperateRecord.setFranchiseeId(shareActivity.getFranchiseeId());
        }
        shareActivityOperateRecord.setCreateTime(System.currentTimeMillis());
        shareActivityOperateRecord.setUpdateTime(System.currentTimeMillis());
        return shareActivityOperateRecord;
    }
    
    private List<ActivityPackageVO> getActivityPackages(List<ShareActivityMemberCard> shareActivityMemberCards) {
        List<ActivityPackageVO> activityPackageVOList = Lists.newArrayList();
        for (ShareActivityMemberCard shareActivityMemberCard : shareActivityMemberCards) {
            Long packageId = shareActivityMemberCard.getMemberCardId();
            Integer packageType = shareActivityMemberCard.getPackageType();
            ActivityPackageVO activityPackageVO = new ActivityPackageVO();
            activityPackageVO.setPackageId(packageId);
            activityPackageVO.setPackageType(packageType);
            if (PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(packageType)) {
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(shareActivityMemberCard.getMemberCardId());
                if (Objects.nonNull(batteryMemberCard)) {
                    activityPackageVO.setPackageName(batteryMemberCard.getName());
                }
            } else {
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(shareActivityMemberCard.getMemberCardId());
                if (Objects.nonNull(carRentalPackagePO)) {
                    activityPackageVO.setPackageName(carRentalPackagePO.getName());
                }
            }
            activityPackageVOList.add(activityPackageVO);
            
        }
        return activityPackageVOList;
    }
    
    @Override
    public R checkExistActivity() {
        ShareMoneyAndShareActivityVO shareActivityVO = new ShareMoneyAndShareActivityVO();
        //查询该租户是否有邀请活动，
        Integer shareMoneyActivityCount = shareMoneyActivityService.existShareMoneyActivity(TenantContextHolder.getTenantId());
        if (Objects.nonNull(shareMoneyActivityCount)) {
            shareActivityVO.setExistShareMoneyActivity(Boolean.TRUE);
        } else {
            shareActivityVO.setExistShareMoneyActivity(Boolean.FALSE);
        }
        
        //查询该租户是否有邀请活动，
        Integer shareActivityCount = shareActivityMapper.existShareActivity(TenantContextHolder.getTenantId());
        if (Objects.nonNull(shareActivityCount)) {
            shareActivityVO.setExistShareActivity(Boolean.TRUE);
        } else {
            shareActivityVO.setExistShareActivity(Boolean.FALSE);
        }
        
        return R.ok(shareActivityVO);
    }
    
    /**
     * <p>
     * Description: delete 9. 活动管理-套餐返现活动里面的套餐配置记录想能够手动删除
     * </p>
     *
     * @param id id 主键id
     * @return com.xiliulou.core.web.R<?>
     * <p>Project: saas-electricity</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#UH1YdEuCwojVzFxtiK6c3jltneb"></a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/14
     */
    @Override
    public R<?> removeById(Long id, List<Long> franchiseeIds) {
        ShareActivity shareActivity = this.queryByIdFromCache(Math.toIntExact(id));
        if (Objects.isNull(shareActivity)) {
            log.warn("delete Activity WARN! not found Activity ! ActivityId={} ", id);
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }
        
        // 租户一致性校验
        if (!Objects.equals(TenantContextHolder.getTenantId(), shareActivity.getTenantId())) {
            return R.ok();
        }
        
        // 加盟商一致性校验
        Integer franchiseeId = shareActivity.getFranchiseeId();
        if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId.longValue())) {
            log.info("Remove shareActivity fail! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return R.fail("120240", "当前加盟商无权限操作");
        }
        
        int count = this.shareActivityMapper.removeById(id, TenantContextHolder.getTenantId().longValue());
        DbUtils.dbOperateSuccessThenHandleCache(Math.toIntExact(id), (identification) -> {
            redisService.delete(CacheConstant.SHARE_ACTIVITY_CACHE + identification);
        });
        
        if (Objects.equals(shareActivity.getStatus(), ShareActivity.STATUS_OFF)) {
            return R.ok(count);
        }
        
        executorService.submit(() -> {
            //修改邀请状态
            JoinShareActivityRecord joinShareActivityRecord = new JoinShareActivityRecord();
            joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_OFF);
            joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
            joinShareActivityRecord.setActivityId(Math.toIntExact(id));
            joinShareActivityRecordService.updateByActivityId(joinShareActivityRecord);
            
            //修改历史记录状态
            JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
            joinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_OFF);
            joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
            joinShareActivityHistory.setActivityId(Math.toIntExact(id));
            joinShareActivityHistoryService.updateByActivityId(joinShareActivityHistory);
        });
        return R.ok(count);
    }
    
    @Slave
    @Override
    public ShareActivity queryOnlineActivity(Integer tenantId, Long franchiseeId) {
        List<ShareActivity> activityList = shareActivityMapper.selectOnlineActivity(tenantId);
        if (CollectionUtils.isEmpty(activityList)) {
            return null;
        }
        
        List<ShareActivity> activityListByFranchisee = activityList.stream()
                .filter(shareActivity -> Objects.nonNull(shareActivity.getFranchiseeId()) && Objects.equals(shareActivity.getFranchiseeId().longValue(), franchiseeId))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(activityListByFranchisee)) {
            return activityListByFranchisee.get(0);
        }
        
        return activityList.get(0);
    }
    
}

