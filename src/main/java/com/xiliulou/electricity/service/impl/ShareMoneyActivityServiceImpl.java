package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ChannelActivity;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityHistory;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityRecord;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivityPackage;
import com.xiliulou.electricity.entity.ShareMoneyActivityRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.ShareMoneyActivityMapper;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.ChannelActivityService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InvitationActivityService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.ShareMoneyActivityPackageService;
import com.xiliulou.electricity.service.ShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.ShareMoneyActivityVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * 活动表(TActivity)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Service("shareMoneyActivityService")
@Slf4j
public class ShareMoneyActivityServiceImpl implements ShareMoneyActivityService {
    
    @Resource
    private ShareMoneyActivityMapper shareMoneyActivityMapper;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    ShareMoneyActivityRecordService shareMoneyActivityRecordService;
    
    @Resource
    ShareActivityService shareActivityService;
    
    @Autowired
    ChannelActivityService channelActivityService;
    
    @Autowired
    InvitationActivityService invitationActivityService;
    
    @Autowired
    ShareMoneyActivityPackageService shareMoneyActivityPackageService;
    
    @Autowired
    private ElectricityMemberCardService memberCardService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    
    @Autowired
    private JoinShareMoneyActivityRecordService joinShareMoneyActivityRecordService;
    
    @Autowired
    private JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;
    
    
    @Resource
    private FranchiseeService franchiseeService;
    
    
    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("shareActivityHandlerExecutor", 1, "SHARE_ACTIVITY_HANDLER_EXECUTOR");
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ShareMoneyActivity queryByIdFromCache(Integer id) {
        //先查缓存
        ShareMoneyActivity shareMoneyActivityCache = redisService.getWithHash(CacheConstant.SHARE_MONEY_ACTIVITY_CACHE + id, ShareMoneyActivity.class);
        if (Objects.nonNull(shareMoneyActivityCache)) {
            return shareMoneyActivityCache;
        }
        
        //缓存没有再查数据库
        ShareMoneyActivity shareMoneyActivity = shareMoneyActivityMapper.selectById(id);
        if (Objects.isNull(shareMoneyActivity)) {
            return null;
        }
        
        //放入缓存
        redisService.saveWithHash(CacheConstant.SHARE_MONEY_ACTIVITY_CACHE + id, shareMoneyActivity);
        return shareMoneyActivity;
    }
    
    /**
     * 新增数据
     *
     * @param shareMoneyActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R insert(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
        if (ObjectUtil.isEmpty(shareMoneyActivityAddAndUpdateQuery.getHours()) && ObjectUtil.isEmpty(shareMoneyActivityAddAndUpdateQuery.getMinutes())) {
            return R.fail("110209", "有效时间不能为空");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        //加盟商
        Long franchiseeId = shareMoneyActivityAddAndUpdateQuery.getFranchiseeId();
        
        //查询该租户是否有邀请活动，有则不能添加
        // int count = shareMoneyActivityMapper.selectCount(new LambdaQueryWrapper<ShareMoneyActivity>().eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
        //3.0后修改为，如果状态为上架时，先提示确定上架，确定后则直接上架，并将之前的活动下架
        if (ShareMoneyActivity.STATUS_ON.equals(shareMoneyActivityAddAndUpdateQuery.getStatus())) {
            ShareMoneyActivity activityResult = shareMoneyActivityMapper.selectActivityByTenantIdAndStatus(tenantId.longValue(), franchiseeId.intValue(),
                    ShareMoneyActivity.STATUS_ON);
            if (Objects.nonNull(activityResult)) {
                //return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
                //如果存在已上架的活动，则将该活动修改为下架
                ShareMoneyActivity moneyActivity = new ShareMoneyActivity();
                moneyActivity.setId(activityResult.getId());
                moneyActivity.setStatus(ShareMoneyActivity.STATUS_OFF);
                moneyActivity.setUpdateTime(System.currentTimeMillis());
                shareMoneyActivityMapper.updateActivity(moneyActivity);
            }
        }
        
        //检查选择邀请标准为购买套餐时，当前所选的套餐是否存在
        if (ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode().equals(shareMoneyActivityAddAndUpdateQuery.getInvitationCriteria())) {
            //检查是否有选择（换电,租车,车电一体）套餐信息
            if (CollectionUtils.isEmpty(shareMoneyActivityAddAndUpdateQuery.getBatteryPackages()) && CollectionUtils.isEmpty(
                    shareMoneyActivityAddAndUpdateQuery.getCarRentalPackages()) && CollectionUtils.isEmpty(shareMoneyActivityAddAndUpdateQuery.getCarWithBatteryPackages())) {
                return R.fail("000201", "请选择套餐信息");
            }
            
            Triple<Boolean, String, Object> verifyResult = verifySelectedPackages(shareMoneyActivityAddAndUpdateQuery);
            if (Boolean.FALSE.equals(verifyResult.getLeft())) {
                return R.fail("000076", (String) verifyResult.getRight());
            }
        }
        
        shareMoneyActivityAddAndUpdateQuery.setType(ShareMoneyActivity.FRANCHISEE);
        
        ShareMoneyActivity shareMoneyActivity = new ShareMoneyActivity();
        BeanUtil.copyProperties(shareMoneyActivityAddAndUpdateQuery, shareMoneyActivity);
        shareMoneyActivity.setCreateTime(System.currentTimeMillis());
        shareMoneyActivity.setUpdateTime(System.currentTimeMillis());
        shareMoneyActivity.setTenantId(tenantId);
        shareMoneyActivity.setHours(Objects.isNull(shareMoneyActivityAddAndUpdateQuery.getHours()) ? NumberConstant.ZERO : (shareMoneyActivityAddAndUpdateQuery.getHours()));
        shareMoneyActivity.setMinutes(Objects.isNull(shareMoneyActivityAddAndUpdateQuery.getMinutes()) ? NumberConstant.ZERO : (shareMoneyActivityAddAndUpdateQuery.getMinutes()));
        shareMoneyActivity.setFranchiseeId(franchiseeId.intValue());
        
        int insert = shareMoneyActivityMapper.insert(shareMoneyActivity);
        
        //保存所选套餐信息
        List<ShareMoneyActivityPackage> shareMoneyActivityPackages = buildShareMoneyActivityPackages(shareMoneyActivity.getId().longValue(), shareMoneyActivityAddAndUpdateQuery);
        if (CollectionUtils.isNotEmpty(shareMoneyActivityPackages)) {
            shareMoneyActivityPackageService.addShareMoneyActivityPackages(shareMoneyActivityPackages);
        }
        
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //放入缓存
            redisService.saveWithHash(CacheConstant.SHARE_MONEY_ACTIVITY_CACHE + shareMoneyActivity.getId(), shareMoneyActivity);
            return null;
        });
        
        if (insert > 0) {
            return R.ok(shareMoneyActivity.getId());
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }
    
    private Triple<Boolean, String, Object> verifySelectedPackages(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
        List<Long> electricityPackages = shareMoneyActivityAddAndUpdateQuery.getBatteryPackages();
        for (Long packageId : electricityPackages) {
            //检查所选套餐是否存在，并且可用
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, "000202", "换电套餐不存在");
            }
        }
        
        List<Long> carRentalPackages = shareMoneyActivityAddAndUpdateQuery.getCarRentalPackages();
        for (Long packageId : carRentalPackages) {
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000203", "租车套餐不存在");
            }
        }
        
        List<Long> carElectricityPackages = shareMoneyActivityAddAndUpdateQuery.getCarWithBatteryPackages();
        for (Long packageId : carElectricityPackages) {
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000204", "车电一体套餐不存在");
            }
        }
        return Triple.of(true, "", null);
    }
    
    private List<ShareMoneyActivityPackage> buildShareMoneyActivityPackages(Long shareActivityId, ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
        List<ShareMoneyActivityPackage> shareMoneyActivityPackages = Lists.newArrayList();
        List<Long> batteryPackages = shareMoneyActivityAddAndUpdateQuery.getBatteryPackages();
        for (Long packageId : batteryPackages) {
            ShareMoneyActivityPackage batteryPackage = buildShareMoneyActivityMemberCard(shareActivityId, packageId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            shareMoneyActivityPackages.add(batteryPackage);
        }
        
        List<Long> carRentalPackages = shareMoneyActivityAddAndUpdateQuery.getCarRentalPackages();
        for (Long packageId : carRentalPackages) {
            ShareMoneyActivityPackage carRentalPackage = buildShareMoneyActivityMemberCard(shareActivityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode());
            shareMoneyActivityPackages.add(carRentalPackage);
        }
        
        List<Long> carWithBatteryPackages = shareMoneyActivityAddAndUpdateQuery.getCarWithBatteryPackages();
        for (Long packageId : carWithBatteryPackages) {
            ShareMoneyActivityPackage carWithBatteryPackage = buildShareMoneyActivityMemberCard(shareActivityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode());
            shareMoneyActivityPackages.add(carWithBatteryPackage);
        }
        
        return shareMoneyActivityPackages;
    }
    
    private ShareMoneyActivityPackage buildShareMoneyActivityMemberCard(Long shareActivityId, Long packageId, Integer packageType) {
        ShareMoneyActivityPackage shareMoneyActivityPackage = new ShareMoneyActivityPackage();
        shareMoneyActivityPackage.setActivityId(shareActivityId);
        shareMoneyActivityPackage.setPackageId(packageId);
        shareMoneyActivityPackage.setPackageType(packageType);
        shareMoneyActivityPackage.setTenantId(TenantContextHolder.getTenantId().longValue());
        shareMoneyActivityPackage.setDelFlag(CommonConstant.DEL_N);
        shareMoneyActivityPackage.setCreateTime(System.currentTimeMillis());
        shareMoneyActivityPackage.setUpdateTime(System.currentTimeMillis());
        
        return shareMoneyActivityPackage;
    }
    
    /**
     * 修改数据(暂只支持上下架）
     *
     * @param shareMoneyActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R update(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
        ShareMoneyActivity oldShareMoneyActivity = queryByIdFromCache(shareMoneyActivityAddAndUpdateQuery.getId());
        if (Objects.isNull(oldShareMoneyActivity)) {
            log.error("update Activity  ERROR! not found Activity ! ActivityId:{} ", shareMoneyActivityAddAndUpdateQuery.getId());
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }
        
        // 租户一致性校验
        Integer tenantId = TenantContextHolder.getTenantId();
        if (!Objects.equals(TenantContextHolder.getTenantId(), oldShareMoneyActivity.getTenantId())) {
            return R.ok();
        }
        
        // 判断该加盟商是否有启用的活动，有则不能启用
        Integer activityFranchiseeId = oldShareMoneyActivity.getFranchiseeId();
        if (Objects.nonNull(activityFranchiseeId)) {
            if (Objects.equals(shareMoneyActivityAddAndUpdateQuery.getStatus(), ShareActivity.STATUS_ON)) {
                int count = shareMoneyActivityMapper.selectCount(
                        new LambdaQueryWrapper<ShareMoneyActivity>().eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getFranchiseeId, activityFranchiseeId)
                                .eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
                if (count > 0) {
                    return R.fail("ELECTRICITY.00102", "该加盟商已有启用中的邀请活动，请勿重复添加");
                }
            }
        } else {
            //查询该租户是否有邀请活动，有则不能启用
            if (Objects.equals(shareMoneyActivityAddAndUpdateQuery.getStatus(), ShareMoneyActivity.STATUS_ON)) {
                int count = shareMoneyActivityMapper.selectCount(
                        new LambdaQueryWrapper<ShareMoneyActivity>().eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
                if (count > 0) {
                    return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
                }
            }
        }
        
        ShareMoneyActivity shareMoneyActivity = new ShareMoneyActivity();
        BeanUtil.copyProperties(shareMoneyActivityAddAndUpdateQuery, shareMoneyActivity);
        shareMoneyActivity.setUpdateTime(System.currentTimeMillis());
        
        int update = shareMoneyActivityMapper.updateById(shareMoneyActivity);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.SHARE_ACTIVITY_CACHE + oldShareMoneyActivity.getId());
            
            //如果是下架活动，则将参与记录中为已参与的状态修改为已下架
            if (Objects.equals(shareMoneyActivityAddAndUpdateQuery.getStatus(), ShareMoneyActivity.STATUS_OFF)) {
                //修改邀请状态
                JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = new JoinShareMoneyActivityRecord();
                joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_OFF);
                joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
                joinShareMoneyActivityRecord.setActivityId(shareMoneyActivity.getId());
                joinShareMoneyActivityRecordService.updateByActivityId(joinShareMoneyActivityRecord);
                
                //修改历史记录状态
                JoinShareMoneyActivityHistory joinShareMoneyActivityHistory = new JoinShareMoneyActivityHistory();
                joinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_OFF);
                joinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
                joinShareMoneyActivityHistory.setActivityId(shareMoneyActivity.getId());
                joinShareMoneyActivityHistoryService.updateByActivityId(joinShareMoneyActivityHistory);
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
    public R queryList(ShareMoneyActivityQuery shareMoneyActivityQuery) {
        List<ShareMoneyActivity> shareMoneyActivityList = shareMoneyActivityMapper.queryList(shareMoneyActivityQuery);
        List<ShareMoneyActivityVO> shareMoneyActivityVOList = Lists.newArrayList();
        
        for (ShareMoneyActivity shareMoneyActivity : shareMoneyActivityList) {
            ShareMoneyActivityVO shareMoneyActivityVO = new ShareMoneyActivityVO();
            BeanUtil.copyProperties(shareMoneyActivity, shareMoneyActivityVO);
            
            // 如果单位小时，timeType=1  如果单位分钟，timeType=2
            if (Objects.nonNull(shareMoneyActivity.getHours()) && !Objects.equals(shareMoneyActivity.getHours(), NumberConstant.ZERO)) {
                shareMoneyActivityVO.setHours(shareMoneyActivity.getHours().doubleValue());
                shareMoneyActivityVO.setTimeType(NumberConstant.ONE);
            } else {
                shareMoneyActivityVO.setMinutes(Objects.isNull(shareMoneyActivity.getMinutes()) ? NumberConstant.ZERO_L : shareMoneyActivity.getMinutes().longValue());
                shareMoneyActivityVO.setTimeType(NumberConstant.TWO);
            }
            
            if (ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode().equals(shareMoneyActivity.getInvitationCriteria())) {
                shareMoneyActivityVO.setBatteryPackages(getBatteryPackages(shareMoneyActivity.getId()));
                
                shareMoneyActivityVO.setCarRentalPackages(getCarBatteryPackages(shareMoneyActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
                shareMoneyActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(shareMoneyActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
                
                //兼容2.0的旧数据，如果三个套餐均为空值，则默认使用全部的换电套餐
                if (CollectionUtils.isEmpty(shareMoneyActivityVO.getBatteryPackages()) && CollectionUtils.isEmpty(shareMoneyActivityVO.getCarRentalPackages())
                        && CollectionUtils.isEmpty(shareMoneyActivityVO.getCarWithBatteryPackages())) {
                    shareMoneyActivityVO.setBatteryPackages(getAllBatteryPackages(shareMoneyActivityQuery.getTenantId()));
                }
            }
            
            Integer franchiseeId = shareMoneyActivity.getFranchiseeId();
            if (Objects.nonNull(franchiseeId)) {
                shareMoneyActivityVO.setFranchiseeName(
                        Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId.longValue())).map(Franchisee::getName).orElse(StringUtils.EMPTY));
            }
            
            shareMoneyActivityVOList.add(shareMoneyActivityVO);
        }
        
        return R.ok(shareMoneyActivityVOList);
    }
    
    
    private List<BatteryMemberCardVO> getAllBatteryPackages(Integer tenantId) {
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().delFlag(BatteryMemberCard.DEL_NORMAL).status(BatteryMemberCard.STATUS_UP).isRefund(BatteryMemberCard.NO)
                .tenantId(tenantId).build();
        
        List<BatteryMemberCardVO> batteryMemberCardVOS = batteryMemberCardService.selectListByQuery(query);
        
        return batteryMemberCardVOS;
    }
    
    @Override
    @Slave
    public R queryCount(ShareMoneyActivityQuery shareMoneyActivityQuery) {
        Integer count = shareMoneyActivityMapper.queryCount(shareMoneyActivityQuery);
        return R.ok(count);
    }
    
    
    @Override
    public R queryInfo(Integer id) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        ShareMoneyActivity shareMoneyActivity = queryByIdFromCache(id);
        if (Objects.isNull(shareMoneyActivity)) {
            log.error("queryInfo Activity  ERROR! not found Activity ! ActivityId:{} ", id);
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }
        
        if (!Objects.equals(tenantId, shareMoneyActivity.getTenantId())) {
            return R.ok();
        }
        
        ShareMoneyActivityVO activityVO = new ShareMoneyActivityVO();
        BeanUtils.copyProperties(shareMoneyActivity, activityVO);
    
        Integer franchiseeId = shareMoneyActivity.getFranchiseeId();
        if (Objects.nonNull(franchiseeId)) {
            activityVO.setFranchiseeName(
                    Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId.longValue())).map(Franchisee::getName).orElse(StringUtils.EMPTY));
        }
        
        return R.ok(shareMoneyActivity);
    }
    
    
    @Override
    @Slave
    public ShareMoneyActivity queryByStatus(Integer activityId) {
        return shareMoneyActivityMapper.selectOne(
                new LambdaQueryWrapper<ShareMoneyActivity>().eq(ShareMoneyActivity::getId, activityId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
    }
    
    @Override
    public R activityInfo() {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //用户是否可用
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("ELECTRICITY  WARN! not found userInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("ACTIVITY WARN! user not auth,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        //邀请活动
        ShareMoneyActivity shareMoneyActivity = this.queryOnlineActivity(tenantId, Objects.isNull(userInfo.getFranchiseeId()) ? null : userInfo.getFranchiseeId().intValue());
        if (Objects.isNull(shareMoneyActivity)) {
            log.info("queryInfo Activity INFO! not found Activity,tenantId={} ", tenantId);
            return R.ok();
        }
        
        ShareMoneyActivityVO shareMoneyActivityVO = new ShareMoneyActivityVO();
        BeanUtil.copyProperties(shareMoneyActivity, shareMoneyActivityVO);
        
        // 兼容旧版小程序
        if (Objects.nonNull(shareMoneyActivity.getHours()) && !Objects.equals(shareMoneyActivity.getHours(), NumberConstant.ZERO)) {
            shareMoneyActivityVO.setHours(shareMoneyActivity.getHours().doubleValue());
            shareMoneyActivityVO.setMinutes(shareMoneyActivity.getHours() * TimeConstant.HOURS_MINUTE);
            shareMoneyActivityVO.setTimeType(NumberConstant.ONE);
        }
        if (Objects.nonNull(shareMoneyActivity.getMinutes()) && !Objects.equals(shareMoneyActivity.getMinutes(), NumberConstant.ZERO)) {
            shareMoneyActivityVO.setMinutes(shareMoneyActivity.getMinutes().longValue());
            shareMoneyActivityVO.setHours(
                    Math.round((double) shareMoneyActivity.getMinutes().longValue() / TimeConstant.HOURS_MINUTE * NumberConstant.ONE_HUNDRED_D) / NumberConstant.ONE_HUNDRED_D);
            shareMoneyActivityVO.setTimeType(NumberConstant.TWO);
        }
        
        //邀请好友数
        int count = 0;
        BigDecimal totalMoney = BigDecimal.ZERO;
        ShareMoneyActivityRecord shareMoneyActivityRecord = shareMoneyActivityRecordService.queryByUid(user.getUid(), shareMoneyActivityVO.getId());
        if (Objects.nonNull(shareMoneyActivityRecord)) {
            count = shareMoneyActivityRecord.getCount();
            totalMoney = shareMoneyActivityRecord.getMoney();
        }
        
        shareMoneyActivityVO.setCount(count);
        shareMoneyActivityVO.setTotalMoney(totalMoney);
        
        //设置对应的套餐信息
        if (ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode().equals(shareMoneyActivityVO.getInvitationCriteria())) {
            shareMoneyActivityVO.setBatteryPackages(getBatteryPackages(shareMoneyActivity.getId()));
            
            shareMoneyActivityVO.setCarRentalPackages(getCarBatteryPackages(shareMoneyActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
            shareMoneyActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(shareMoneyActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
            
            //兼容2.0的旧数据，如果三个套餐均为空值，则默认使用全部的换电套餐
            if (CollectionUtils.isEmpty(shareMoneyActivityVO.getBatteryPackages()) && CollectionUtils.isEmpty(shareMoneyActivityVO.getCarRentalPackages())
                    && CollectionUtils.isEmpty(shareMoneyActivityVO.getCarWithBatteryPackages())) {
                shareMoneyActivityVO.setBatteryPackages(getAllBatteryPackages(tenantId));
            }
        }
    
        Integer franchiseeId = shareMoneyActivity.getFranchiseeId();
        if (Objects.nonNull(franchiseeId)) {
            shareMoneyActivityVO.setFranchiseeName(
                    Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId.longValue())).map(Franchisee::getName).orElse(StringUtils.EMPTY));
        }
        
        return R.ok(shareMoneyActivityVO);
    }
    
    private List<BatteryMemberCardVO> getBatteryPackages(Integer activityId) {
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<ShareMoneyActivityPackage> batteryPackageList = shareMoneyActivityPackageService.findPackagesByActivityIdAndType(activityId.longValue(),
                PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        for (ShareMoneyActivityPackage shareMoneyActivityPackage : batteryPackageList) {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(shareMoneyActivityPackage.getPackageId());
            if (Objects.nonNull(batteryMemberCard) && CommonConstant.DEL_N.equals(batteryMemberCard.getDelFlag())) {
                BeanUtils.copyProperties(batteryMemberCard, batteryMemberCardVO);
                memberCardVOList.add(batteryMemberCardVO);
            }
        }
        return memberCardVOList;
    }
    
    private List<BatteryMemberCardVO> getCarBatteryPackages(Integer activityId, Integer packageType) {
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<ShareMoneyActivityPackage> carBatteryPackageList = shareMoneyActivityPackageService.findPackagesByActivityIdAndType(activityId.longValue(), packageType);
        for (ShareMoneyActivityPackage shareMoneyActivityPackage : carBatteryPackageList) {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(shareMoneyActivityPackage.getPackageId());
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
    public R checkActivity() {
        
        Map<String, Integer> map = new HashMap<>();
        map.put("shareMoneyActivity", 0);
        map.put("shareActivity", 0);
        map.put("channelActivity", 0);
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //用户是否可用
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("ELECTRICITY  WARN! not found userInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("ACTIVITY WARN! user not auth,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        //邀请返现活动
        
        ShareMoneyActivity shareMoneyActivity = this.queryOnlineActivity(tenantId, userInfo.getFranchiseeId().intValue());
        if (Objects.isNull(shareMoneyActivity)) {
            map.put("shareMoneyActivity", 1);
        }
        
        //邀请活动
        ShareActivity shareActivity = shareActivityService.queryOnlineActivity(tenantId, userInfo.getFranchiseeId().intValue());
        if (Objects.isNull(shareActivity)) {
            map.put("shareActivity", 1);
        }
        
        //渠道活动
        ChannelActivity usableActivity = channelActivityService.findUsableActivity(tenantId);
        if (Objects.isNull(usableActivity)) {
            map.put("channelActivity", 1);
        }
        
        Integer invitationActivity = invitationActivityService.checkUsableActivity(tenantId, userInfo.getFranchiseeId());
        if (Objects.isNull(invitationActivity)) {
            map.put("invitationActivity", 1);
        } else {
            map.put("invitationActivity", 0);
        }
        
        return R.ok(map);
    }
    
    @Override
    @Slave
    public R checkActivityStatusOn() {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //查询该租户是否有邀请活动，有则不能添加
        int count = shareMoneyActivityMapper.selectCount(
                new LambdaQueryWrapper<ShareMoneyActivity>().eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
        if (count > 0) {
            return R.fail("000205", "已有上架的邀请活动，是否更新替代");
        }
        
        return R.ok();
    }
    
    @Override
    @Slave
    public Integer existShareMoneyActivity(Integer tenantId) {
        return shareMoneyActivityMapper.existShareMoneyActivity(tenantId);
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
    public R<?> removeById(Long id) {
        ShareMoneyActivity shareMoneyActivity = this.queryByIdFromCache(Math.toIntExact(id));
        if (Objects.isNull(shareMoneyActivity)) {
            log.error("delete Activity  ERROR! not found Activity ! ActivityId:{} ", id);
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }
        
        // 租户一致性校验
        if (!Objects.equals(TenantContextHolder.getTenantId(), shareMoneyActivity.getTenantId())) {
            return R.ok();
        }
        
        int count = this.shareMoneyActivityMapper.removeById(id, TenantContextHolder.getTenantId().longValue());
        
        DbUtils.dbOperateSuccessThenHandleCache(Math.toIntExact(id), (identifier) -> {
            redisService.delete(CacheConstant.SHARE_MONEY_ACTIVITY_CACHE + identifier);
        });
        
        if (Objects.equals(shareMoneyActivity.getStatus(), ShareActivity.STATUS_OFF)) {
            return R.ok(count);
        }
        
        executorService.submit(() -> {
            //修改邀请状态
            JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = new JoinShareMoneyActivityRecord();
            joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_OFF);
            joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
            joinShareMoneyActivityRecord.setActivityId(Math.toIntExact(id));
            joinShareMoneyActivityRecordService.updateByActivityId(joinShareMoneyActivityRecord);
            
            //修改历史记录状态
            JoinShareMoneyActivityHistory joinShareMoneyActivityHistory = new JoinShareMoneyActivityHistory();
            joinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_OFF);
            joinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
            joinShareMoneyActivityHistory.setActivityId(Math.toIntExact(id));
            joinShareMoneyActivityHistoryService.updateByActivityId(joinShareMoneyActivityHistory);
        });
        return R.ok(count);
    }
    
    @Slave
    @Override
    public ShareMoneyActivity queryOnlineActivity(Integer tenantId, Integer franchiseeId) {
        return shareMoneyActivityMapper.selectOnlineActivity(tenantId, franchiseeId);
    }
}

