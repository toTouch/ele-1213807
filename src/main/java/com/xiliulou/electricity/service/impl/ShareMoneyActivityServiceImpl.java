package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.ShareActivityMapper;
import com.xiliulou.electricity.mapper.ShareMoneyActivityMapper;
import com.xiliulou.electricity.query.ShareMoneyActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ShareMoneyActivityVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    ShareActivityMapper shareActivityMapper;
    
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
        //创建账号
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("Coupon  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //查询该租户是否有邀请活动，有则不能添加
        // int count = shareMoneyActivityMapper.selectCount(new LambdaQueryWrapper<ShareMoneyActivity>().eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
        //3.0后修改为，如果有上架的，先提示确定上架，确定后则直接上架，并将之前的活动下架
        ShareMoneyActivity activityResult = shareMoneyActivityMapper.selectActivityByTenantIdAndStatus(tenantId.longValue(), ShareMoneyActivity.STATUS_ON);
        if (Objects.nonNull(activityResult)) {
            //return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
            //如果存在已上架的活动，则将该活动修改为下架
            ShareMoneyActivity moneyActivity = new ShareMoneyActivity();
            moneyActivity.setId(activityResult.getId());
            moneyActivity.setStatus(ShareMoneyActivity.STATUS_OFF);
            moneyActivity.setUpdateTime(System.currentTimeMillis());
            shareMoneyActivityMapper.updateActivity(moneyActivity);
        }

        //检查选择邀请标准为购买套餐时，当前所选的套餐是否存在
        if(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.equals(shareMoneyActivityAddAndUpdateQuery.getInvitationCriteria())){
            //检查是否有选择（换电,租车,车电一体）套餐信息
            if(CollectionUtils.isEmpty(shareMoneyActivityAddAndUpdateQuery.getBatteryPackages())
                    && CollectionUtils.isEmpty(shareMoneyActivityAddAndUpdateQuery.getCarRentalPackages())
                    && CollectionUtils.isEmpty(shareMoneyActivityAddAndUpdateQuery.getCarWithBatteryPackages())){
                return R.fail("000201", "请选择套餐信息");
            }

            Triple<Boolean, String, Object> verifyResult = verifySelectedPackages(shareMoneyActivityAddAndUpdateQuery);
            if(Boolean.FALSE.equals(verifyResult.getLeft())){
                return R.fail("000076", (String) verifyResult.getRight());
            }
        }

        ShareMoneyActivity shareMoneyActivity = new ShareMoneyActivity();
        BeanUtil.copyProperties(shareMoneyActivityAddAndUpdateQuery, shareMoneyActivity);
        shareMoneyActivity.setUid(user.getUid());
        shareMoneyActivity.setUserName(user.getUsername());
        shareMoneyActivity.setCreateTime(System.currentTimeMillis());
        shareMoneyActivity.setUpdateTime(System.currentTimeMillis());
        shareMoneyActivity.setTenantId(tenantId);
        //新增的邀请返现活动默认为上架状态
        shareMoneyActivity.setStatus(ShareMoneyActivity.STATUS_ON);

        if (Objects.isNull(shareMoneyActivity.getType())) {
            shareMoneyActivity.setType(ShareActivity.SYSTEM);
        }

        int insert = shareMoneyActivityMapper.insert(shareMoneyActivity);

        //保存所选套餐信息
        List<ShareMoneyActivityPackage> shareMoneyActivityPackages = buildShareMoneyActivityPackages(shareMoneyActivity.getId().longValue(), shareMoneyActivityAddAndUpdateQuery);
        if(CollectionUtils.isNotEmpty(shareMoneyActivityPackages)){
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

    private Triple<Boolean, String, Object> verifySelectedPackages(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery){
        List<Long> electricityPackages = shareMoneyActivityAddAndUpdateQuery.getBatteryPackages();
        for(Long packageId : electricityPackages){
            //检查所选套餐是否存在，并且可用
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, "000202", "换电套餐不存在");
            }
        }

        List<Long> carRentalPackages = shareMoneyActivityAddAndUpdateQuery.getCarRentalPackages();
        for(Long packageId : carRentalPackages){
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000203", "租车套餐不存在");
            }
        }

        List<Long> carElectricityPackages = shareMoneyActivityAddAndUpdateQuery.getCarWithBatteryPackages();
        for(Long packageId : carElectricityPackages){
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
        for(Long packageId : batteryPackages){
            ShareMoneyActivityPackage batteryPackage = buildShareMoneyActivityMemberCard(shareActivityId, packageId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            shareMoneyActivityPackages.add(batteryPackage);
        }

        List<Long> carRentalPackages = shareMoneyActivityAddAndUpdateQuery.getCarRentalPackages();
        for(Long packageId : carRentalPackages){
            ShareMoneyActivityPackage carRentalPackage = buildShareMoneyActivityMemberCard(shareActivityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode());
            shareMoneyActivityPackages.add(carRentalPackage);
        }

        List<Long> carWithBatteryPackages = shareMoneyActivityAddAndUpdateQuery.getCarWithBatteryPackages();
        for(Long packageId : carWithBatteryPackages){
            ShareMoneyActivityPackage carWithBatteryPackage = buildShareMoneyActivityMemberCard(shareActivityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode());
            shareMoneyActivityPackages.add(carWithBatteryPackage);
        }

        return shareMoneyActivityPackages;
    }

    private ShareMoneyActivityPackage buildShareMoneyActivityMemberCard(Long shareActivityId, Long packageId, Integer packageType){
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

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //查询该租户是否有邀请活动，有则不能启用
        if (Objects.equals(shareMoneyActivityAddAndUpdateQuery.getStatus(), ShareMoneyActivity.STATUS_ON)) {
            int count = shareMoneyActivityMapper.selectCount(new LambdaQueryWrapper<ShareMoneyActivity>()
                    .eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
            if (count > 0) {
                return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
            }
        }

        ShareMoneyActivity shareMoneyActivity = new ShareMoneyActivity();
        BeanUtil.copyProperties(shareMoneyActivityAddAndUpdateQuery, shareMoneyActivity);
        shareMoneyActivity.setUpdateTime(System.currentTimeMillis());

        int update = shareMoneyActivityMapper.updateById(shareMoneyActivity);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.SHARE_ACTIVITY_CACHE + oldShareMoneyActivity.getId());
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    public R queryList(ShareMoneyActivityQuery shareMoneyActivityQuery) {
        List<ShareMoneyActivity> shareMoneyActivityList = shareMoneyActivityMapper.queryList(shareMoneyActivityQuery);
        return R.ok(shareMoneyActivityList);
    }


    @Override
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

        return R.ok(shareMoneyActivity);
    }


    @Override
    public ShareMoneyActivity queryByStatus(Integer activityId) {
        return shareMoneyActivityMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivity>()
                .eq(ShareMoneyActivity::getId, activityId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
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
            log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("order  ERROR! user not auth,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //邀请活动
        ShareMoneyActivity shareMoneyActivity = shareMoneyActivityMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivity>()
                .eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
        if (Objects.isNull(shareMoneyActivity)) {
            log.info("queryInfo Activity INFO! not found Activity,tenantId={} ", tenantId);
            return R.ok();
        }


        ShareMoneyActivityVO shareMoneyActivityVO = new ShareMoneyActivityVO();
        BeanUtil.copyProperties(shareMoneyActivity, shareMoneyActivityVO);

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

        return R.ok(shareMoneyActivityVO);
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
            log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("order  ERROR! user not auth,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }


        //邀请返现活动
        ShareMoneyActivity shareMoneyActivity = shareMoneyActivityMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivity>()
                .eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
        if (Objects.isNull(shareMoneyActivity)) {
//            log.error("queryInfo Activity  ERROR! not found Activity ! tenantId:{} ", tenantId);
            map.put("shareMoneyActivity", 1);
        }

        //邀请活动
        ShareActivity shareActivity = shareActivityMapper.selectOne(new LambdaQueryWrapper<ShareActivity>()
                .eq(ShareActivity::getTenantId, tenantId).eq(ShareActivity::getStatus, ShareActivity.STATUS_ON));
        if (Objects.isNull(shareActivity)) {
//            log.error("queryInfo Activity  ERROR! not found Activity ! tenantId:{} ", tenantId);
            map.put("shareActivity", 1);
        }
    
        //渠道活动
        ChannelActivity usableActivity = channelActivityService.findUsableActivity(tenantId);
        if (Objects.isNull(usableActivity)) {
            map.put("channelActivity", 1);
        }

        Integer invitationActivity = invitationActivityService.checkUsableActivity(tenantId);
        if(Objects.isNull(invitationActivity)){
            map.put("invitationActivity", 1);
        }else{
            map.put("invitationActivity", 0);
        }

        return R.ok(map);
    }

    @Override
    public R checkActivityStatusOn() {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //查询该租户是否有邀请活动，有则不能添加
        int count = shareMoneyActivityMapper.selectCount(new LambdaQueryWrapper<ShareMoneyActivity>()
                .eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
        if (count > 0) {
            return R.fail("000205", "已有上架的邀请活动，是否更新替代");
        }

        return R.ok();
    }

}

