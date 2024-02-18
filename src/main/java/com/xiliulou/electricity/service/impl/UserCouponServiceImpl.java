package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.UserCouponDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.SpecificPackagesEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.UserCouponMapper;
import com.xiliulou.electricity.query.CouponBatchSendWithPhonesRequest;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatchSendCouponVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.UserCouponVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 优惠券表(TCoupon)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
@Service("userCouponService")
@Slf4j
public class UserCouponServiceImpl implements UserCouponService {

    protected XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("SEND_COUPON_TO_USER_THREAD_POOL", 7, "send_coupon_to_user_thread");

    @Resource
    private UserCouponMapper userCouponMapper;
    @Autowired
    private CouponService couponService;

    @Autowired
    private RedisService redisService;

    @Autowired
    UserService userService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    ShareActivityRecordService shareActivityRecordService;

    @Autowired
    ShareActivityService shareActivityService;

    @Autowired
    ShareActivityRuleService shareActivityRuleService;

    @Autowired
    CouponIssueOperateRecordService couponIssueOperateRecordService;

    @Autowired
    private CarRentalPackageService carRentalPackageService;
    @Autowired
    private CouponActivityPackageService couponActivityPackageService;
    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    /**
     * 根据ID集查询用户优惠券信息
     *
     * @param idList 主键ID集
     * @return 用户优惠券集
     */
    @Slave
    @Override
    public List<UserCoupon> listByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }

        return userCouponMapper.selectBatchIds(idList);

    }

    /**
     * 根据来源订单编码作废掉未使用的优惠券
     *
     * @param sourceOrderId     订单编码
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean cancelBySourceOrderIdAndUnUse(String sourceOrderId) {
        if (!ObjectUtils.allNotNull(sourceOrderId)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        int num = userCouponMapper.cancelBySourceOrderIdAndUnUse(sourceOrderId, System.currentTimeMillis());

        return num >= 0;
    }

    /**
     * 根据订单编码更新优惠券状态
     *
     * @param orderId     订单编码
     * @param status      状态
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean updateStatusByOrderId(String orderId, Integer status) {
        if (!ObjectUtils.allNotNull(orderId, status)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        int num = userCouponMapper.updateStatusByOrderId(orderId, status, System.currentTimeMillis());

        return num >= 0;
    }

    /**
     * 查询用户名下有效的优惠券
     *
     * @param uid      用户ID
     * @param ids      主键ID
     * @param deadline 到期时间
     * @return
     */
    @Override
    public List<UserCoupon> selectEffectiveByUid(Long uid, List<Long> ids, Long deadline) {
        if (!ObjectUtils.allNotNull(uid, ids, deadline)) {
            return null;
        }
        return userCouponMapper.selectEffectiveByUid(uid, ids, deadline);
    }

    @Override
    public R queryList(UserCouponQuery userCouponQuery) {
        List<UserCouponVO> userCouponList = userCouponMapper.queryList(userCouponQuery);
        return R.ok(userCouponList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchRelease(Integer id, Long[] uids) {
        if (ObjectUtil.isEmpty(uids)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Coupon coupon = couponService.queryByIdFromCache(id);
        if (Objects.isNull(coupon)) {
            log.error("Coupon  ERROR! not found coupon ! couponId:{} ", id);
            return R.fail("ELECTRICITY.0085", "未找到优惠券");
        }

        UserCoupon.UserCouponBuilder couponBuild = UserCoupon.builder()
                .name(coupon.getName())
                .source(UserCoupon.TYPE_SOURCE_ADMIN_SEND)
                .couponId(coupon.getId())
                .discountType(coupon.getDiscountType())
                .status(UserCoupon.STATUS_UNUSED)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(coupon.getTenantId());

        //优惠券过期时间

        LocalDateTime now = LocalDateTime.now().plusDays(coupon.getDays());
        couponBuild.deadline(TimeUtils.convertTimeStamp(now));

        //批量插入
        for (Long uid : uids) {
            //查询用户手机号
            User user = userService.queryByUidFromCache(uid);
            if (Objects.isNull(user)) {
                log.error("batchRelease  ERROR! not found user,uid:{} ", user.getUid());
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }
            couponBuild.uid(uid);
            couponBuild.phone(user.getPhone());
            UserCoupon userCoupon = couponBuild.build();
            userCouponMapper.insert(userCoupon);
        }

        return R.ok();
    }

    @Override
    public R adminBatchRelease(Integer id, Long[] uids) {
        //用户区分
        TokenUser operateUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(operateUser)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (ObjectUtil.isEmpty(uids)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Coupon coupon = couponService.queryByIdFromCache(id);
        if (Objects.isNull(coupon) || !Objects.equals(coupon.getTenantId(), tenantId)) {
            log.error("Coupon  ERROR! not found coupon ! couponId={} ", id);
            return R.fail("ELECTRICITY.0085", "未找到优惠券");
        }

        UserCoupon.UserCouponBuilder couponBuild = UserCoupon.builder()
                .name(coupon.getName())
                .source(UserCoupon.TYPE_SOURCE_ADMIN_SEND)
                .couponId(coupon.getId())
                .discountType(coupon.getDiscountType())
                .status(UserCoupon.STATUS_UNUSED)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(tenantId);

        //优惠券过期时间

        LocalDateTime now = LocalDateTime.now().plusDays(coupon.getDays());
        couponBuild.deadline(TimeUtils.convertTimeStamp(now));

        //发放操作记录
        CouponIssueOperateRecord.CouponIssueOperateRecordBuilder couponIssueOperateRecord = CouponIssueOperateRecord.builder()
                .couponId(coupon.getId())
                .tenantId(tenantId)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis());


        //批量插入
        for (Long uid : uids) {
            //查询用户手机号
            User user = userService.queryByUidFromCache(uid);
            if (Objects.isNull(user)) {
                log.error("batchRelease  ERROR! not found user,uid:{} ", uid);
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }

            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.error("batchRelease  ERROR! not found user,uid:{} ", uid);
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }
            couponBuild.uid(uid);
            couponBuild.phone(user.getPhone());
            UserCoupon userCoupon = couponBuild.build();
            userCouponMapper.insert(userCoupon);

            couponIssueOperateRecord.uid(user.getUid());
            couponIssueOperateRecord.name(userInfo.getName());
            couponIssueOperateRecord.operateName(operateUser.getUsername());
            couponIssueOperateRecord.phone(user.getPhone());
            couponIssueOperateRecord.tenantId(tenantId);
            CouponIssueOperateRecord couponIssueOperateRecordBuild = couponIssueOperateRecord.build();
            couponIssueOperateRecordService.insert(couponIssueOperateRecordBuild);
        }

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R destruction(Long[] couponIds) {

        //用户区分
        TokenUser operateUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(operateUser)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (ObjectUtil.isEmpty(couponIds)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        for (Long couponId : couponIds) {
            UserCoupon couponBuild = UserCoupon.builder()
                    .id(couponId)
                    .status(UserCoupon.STATUS_DESTRUCTION)
                    .updateTime(System.currentTimeMillis())
                    .tenantId(tenantId).build();

            userCouponMapper.update(couponBuild);
        }

        return R.ok();
    }

    @Override
    public void handelUserCouponExpired() {
        //分页只修改200条
        List<UserCoupon> userCouponList = userCouponMapper.getExpiredUserCoupon(System.currentTimeMillis(), 0, 200);
        if (!DataUtil.collectionIsUsable(userCouponList)) {
            return;
        }
        for (UserCoupon userCoupon : userCouponList) {
            userCoupon.setStatus(UserCoupon.STATUS_EXPIRED);
            userCoupon.setUpdateTime(System.currentTimeMillis());
            userCouponMapper.updateById(userCoupon);
        }
    }

    @Deprecated
    @Override
    public R queryMyCoupon(List<Integer> statusList, List<Integer> typeList) {
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //2.判断用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unusable!uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("ELECTRICITY WARN! not auth! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //查看用户优惠券
        UserCouponQuery userCouponQuery = new UserCouponQuery();
        userCouponQuery.setStatusList(statusList);
        userCouponQuery.setUid(uid);
        userCouponQuery.setTypeList(typeList);
        List<UserCouponVO> userCouponVOList = userCouponMapper.queryList(userCouponQuery);

        return R.ok(userCouponVOList);

    }

    @Override
    public R queryMyCoupons(List<Integer> statusList, List<Integer> typeList) {
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //2.判断用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unusable!uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("ELECTRICITY WARN! not auth! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //查看用户优惠券
        UserCouponQuery userCouponQuery = new UserCouponQuery();
        userCouponQuery.setStatusList(statusList);
        userCouponQuery.setUid(uid);
        userCouponQuery.setTypeList(typeList);
        List<UserCouponVO> userCouponVOList = userCouponMapper.queryList(userCouponQuery);

        //若是不可叠加的优惠券且指定了使用套餐,则将对应的套餐信息设置到优惠券中
        for(UserCouponVO userCouponVO : userCouponVOList){
            if(Coupon.SUPERPOSITION_NO.equals(userCouponVO.getSuperposition())
                    && SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(userCouponVO.getSpecificPackages())){
                Long couponId = userCouponVO.getCouponId().longValue();
                userCouponVO.setBatteryPackages(getBatteryPackages(couponId));
                userCouponVO.setCarRentalPackages(getCarBatteryPackages(couponId, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
                userCouponVO.setCarWithBatteryPackages(getCarBatteryPackages(couponId, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
            }
        }

        return  R.ok(userCouponVOList);
    }

    private List<BatteryMemberCardVO> getBatteryPackages(Long couponId){
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<CouponActivityPackage> couponActivityPackages = couponActivityPackageService.findPackagesByCouponIdAndType(couponId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());

        for(CouponActivityPackage couponActivityPackage : couponActivityPackages){
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(couponActivityPackage.getPackageId());
            if(Objects.nonNull(batteryMemberCard) && CommonConstant.DEL_N.equals(batteryMemberCard.getDelFlag())){
                BeanUtils.copyProperties(batteryMemberCard, batteryMemberCardVO);
                memberCardVOList.add(batteryMemberCardVO);
            }
        }

        return memberCardVOList;
    }

    private List<BatteryMemberCardVO> getCarBatteryPackages(Long couponId, Integer packageType){
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<CouponActivityPackage> couponActivityPackages = couponActivityPackageService.findPackagesByCouponIdAndType(couponId, packageType);
        for(CouponActivityPackage couponActivityPackage : couponActivityPackages){
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(couponActivityPackage.getPackageId());
            if(Objects.nonNull(carRentalPackagePO) && CommonConstant.DEL_N.equals(carRentalPackagePO.getDelFlag())){
                batteryMemberCardVO.setId(carRentalPackagePO.getId());
                batteryMemberCardVO.setName(carRentalPackagePO.getName());
                batteryMemberCardVO.setCreateTime(carRentalPackagePO.getCreateTime());
                memberCardVOList.add(batteryMemberCardVO);
            }
        }

        return memberCardVOList;
    }

    /*

     1、判断优惠券是否上架
     2、判断用户是否可以领取优惠券
     3、领取优惠券
     4、优惠券领取成功或失败

     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getShareCoupon(Integer activityId, Integer couponId) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("getShareCoupon ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.CACHE_GET_COUPON + SecurityUtils.getUid(), "1", 1000L, false)) {
            return R.fail( "ELECTRICITY.0034", "领取的太快啦，请稍后");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if(Objects.isNull(userInfo)){
            log.error("getShareCoupon ERROR! not found user,uid={}",user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("getShareCoupon  ERROR! user not auth,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("getShareCoupon  ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        ShareActivity shareActivity = shareActivityService.queryByIdFromCache(activityId);
        if (Objects.isNull(shareActivity)) {
            log.error("getShareCoupon  ERROR! not found Activity,ActivityId={},uid={}", activityId, user.getUid());
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }

        //查询活动规则
        List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivity(activityId);
        if (ObjectUtil.isEmpty(shareActivityRuleList)) {
            log.error("getShareCoupon ERROR! not found Activity ! ActivityId={},uid={}", activityId, user.getUid());
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }

        //判断用户是否可以领取优惠券
        ShareActivityRecord shareActivityRecord = shareActivityRecordService.queryByUid(user.getUid(), activityId);
        if (Objects.isNull(shareActivityRecord)) {
            return R.fail("ELECTRICITY.00103", "该用户邀请好友不够，领劵失败");
        }

        if (Objects.equals(shareActivity.getReceiveType(), ShareActivity.RECEIVE_TYPE_CYCLE)) {
            //循环领取

            //查询优惠券是否在活动中间
            for (ShareActivityRule shareActivityRule : shareActivityRuleList) {
                if (Objects.equals(shareActivityRule.getCouponId(), couponId)) {
                    if (shareActivityRecord.getAvailableCount() < shareActivityRule.getTriggerCount()) {
                        return R.fail("ELECTRICITY.00103", "该用户邀请好友不够，领劵失败");
                    } else {
                        //领劵
                        Coupon coupon = couponService.queryByIdFromCache(couponId);
                        if (Objects.isNull(coupon)) {
                            log.error("getShareCoupon  ERROR! not found coupon,couponId={},uid={}", couponId, user.getUid());
                            return R.fail("ELECTRICITY.0085", "未找到优惠券");
                        }

//                        UserCoupon oldUserCoupon = queryByActivityIdAndCouponId(activityId, shareActivityRule.getId(), couponId, user.getUid());
//                        if (Objects.nonNull(oldUserCoupon)) {
//                            continue;
//                        }

                        LocalDateTime now = LocalDateTime.now().plusDays(coupon.getDays());
                        UserCoupon.UserCouponBuilder couponBuild = UserCoupon.builder()
                                .name(coupon.getName())
                                .source(UserCoupon.TYPE_SOURCE_ADMIN_SEND)
                                .activityId(activityId)
                                .activityRuleId(shareActivityRule.getId())
                                .couponId(couponId)
                                .discountType(coupon.getDiscountType())
                                .status(UserCoupon.STATUS_UNUSED)
                                .createTime(System.currentTimeMillis())
                                .updateTime(System.currentTimeMillis())
                                .uid(user.getUid())
                                .phone(user.getPhone())
                                .deadline(TimeUtils.convertTimeStamp(now))
                                .tenantId(tenantId);

                        UserCoupon userCoupon = couponBuild.build();
                        userCouponMapper.insert(userCoupon);

                        //领劵完，可用邀请人数减少
                        shareActivityRecordService.reduceAvailableCountByUid(user.getUid(), shareActivityRule.getTriggerCount(), shareActivityRecord.getActivityId());
                        return R.ok("领取成功");
                    }
                }
            }
        } else {
            //阶梯领取

            //查询优惠券是否在活动中间
            for (ShareActivityRule shareActivityRule : shareActivityRuleList) {
                if (Objects.equals(shareActivityRule.getCouponId(), couponId)) {
                    if (shareActivityRecord.getAvailableCount() < shareActivityRule.getTriggerCount()) {
                        return R.fail("ELECTRICITY.00103", "该用户邀请好友不够，领劵失败");
                    } else {
                        //领劵
                        Coupon coupon = couponService.queryByIdFromCache(couponId);
                        if (Objects.isNull(coupon)) {
                            log.error("getShareCoupon  ERROR! not found coupon,couponId={},uid={}", couponId, user.getUid());
                            return R.fail("ELECTRICITY.0085", "未找到优惠券");
                        }

                        UserCoupon oldUserCoupon = queryByActivityIdAndCouponId(activityId, shareActivityRule.getId(), couponId, user.getUid());
                        if (Objects.nonNull(oldUserCoupon)) {
                            continue;
                        }

                        LocalDateTime now = LocalDateTime.now().plusDays(coupon.getDays());
                        UserCoupon.UserCouponBuilder couponBuild = UserCoupon.builder()
                                .name(coupon.getName())
                                .source(UserCoupon.TYPE_SOURCE_ADMIN_SEND)
                                .activityId(activityId)
                                .activityRuleId(shareActivityRule.getId())
                                .couponId(couponId)
                                .discountType(coupon.getDiscountType())
                                .status(UserCoupon.STATUS_UNUSED)
                                .createTime(System.currentTimeMillis())
                                .updateTime(System.currentTimeMillis())
                                .uid(user.getUid())
                                .phone(user.getPhone())
                                .deadline(TimeUtils.convertTimeStamp(now))
                                .tenantId(tenantId);

                        UserCoupon userCoupon = couponBuild.build();
                        userCouponMapper.insert(userCoupon);

                        //领劵完，可用邀请人数减少
                        shareActivityRecordService.reduceAvailableCountByUid(user.getUid(), shareActivityRule.getTriggerCount(), shareActivityRecord.getActivityId());
                        return R.ok("领取成功");
                    }
                }
            }
        }

        return R.fail("ELECTRICITY.00104", "已领过该张优惠券，请不要贪心哦");

    }

    @Override
    public UserCoupon queryByIdFromDB(Integer userCouponId) {
        return userCouponMapper.selectById(userCouponId);
    }

    @Override
    public UserCoupon queryByActivityIdAndCouponId(Integer activityId, Long activityRuleId, Integer couponId, Long uid) {
        return userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getActivityId, activityId).eq(UserCoupon::getActivityRuleId, activityRuleId)
                .eq(UserCoupon::getCouponId, couponId).eq(UserCoupon::getUid, uid));
    }

    @Override
    public List<UserCoupon> selectListByActivityIdAndCouponId(Integer activityId, Long activityRuleId, Integer couponId, Long uid) {
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getActivityId, activityId).eq(UserCoupon::getActivityRuleId, activityRuleId)
                .eq(UserCoupon::getCouponId, couponId).eq(UserCoupon::getUid, uid));
    }

    @Override
    public void update(UserCoupon userCoupon) {
        userCouponMapper.updateById(userCoupon);
    }

    @Override
    public int updateStatus(UserCoupon userCoupon) {
        return userCouponMapper.updateStatus(userCoupon);
    }

    @Slave
    @Override
    public R queryCount(UserCouponQuery userCouponQuery) {
        Integer count = userCouponMapper.queryCount(userCouponQuery);
        return R.ok(count);
    }

    @Slave
    @Override
    public List<UserCoupon> selectCouponUserCountById(Long id) {
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getCouponId, id)
                .eq(UserCoupon::getDelFlag, UserCoupon.DEL_NORMAL)
                .eq(UserCoupon::getTenantId, TenantContextHolder.getTenantId())
                .eq(UserCoupon::getStatus, UserCoupon.STATUS_UNUSED));
    }

    @Override
    public Integer batchUpdateUserCoupon(List<UserCoupon> buildUserCouponList) {
        if (CollectionUtils.isEmpty(buildUserCouponList)) {
            return NumberConstant.ZERO;
        }

        for (UserCoupon userCoupon : buildUserCouponList) {
            userCouponMapper.updateUserCouponStatus(userCoupon);
        }

        return NumberConstant.ONE;
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return userCouponMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }

    @Override
    public R adminBatchReleaseV2(CouponBatchSendWithPhonesRequest request) {
        Set<String> phones = new HashSet<>(JsonUtil.fromJsonArray(request.getJsonPhones(), String.class));
        if (CollectionUtils.isEmpty(phones)) {
            return R.fail("ELECTRICITY.0007", "手机号不可以为空");
        }

        User operateUser = userService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(operateUser)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        Coupon coupon = couponService.queryByIdFromCache(request.getCouponId());
        if (Objects.isNull(coupon) || !Objects.equals(coupon.getTenantId(), tenantId)) {
            log.error("Coupon  ERROR! not found coupon ! couponId={} ", request.getCouponId());
            return R.fail("ELECTRICITY.0085", "未找到优惠券");
        }

        ConcurrentHashSet<String> notExistsPhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<User> existsPhone = new ConcurrentHashSet<>();

        phones.parallelStream().forEach(e -> {
            User user = userService.queryByUserPhone(e, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
            if (Objects.isNull(user)) {
                notExistsPhone.add(e);
            } else {
                UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
                user.setName(userInfo.getName());
                existsPhone.add(user);
            }
        });

        String sessionId = UUID.fastUUID().toString(true);
        BatchSendCouponVO batchSendCouponVO = new BatchSendCouponVO();
        batchSendCouponVO.setSessionId(sessionId);
        batchSendCouponVO.setNotExistPhones(notExistsPhone);
        if (existsPhone.isEmpty()) {
            batchSendCouponVO.setIsSend(false);
            return R.ok(batchSendCouponVO);
        }

        batchSendCouponVO.setIsSend(true);
        executorService.execute(() -> {
            handleBatchSaveCoupon(existsPhone, coupon, sessionId, operateUser.getName());
        });

        return R.ok(batchSendCouponVO);
    }

    @Override
    public R checkSendFinish(String sessionId) {
        if (!redisService.hasKey(CacheConstant.CACHE_BATCH_SEND_COUPON + sessionId)) {
            return R.fail("300066", "正在发送中，请稍后再试");
        }
        return R.ok();
    }

    private void handleBatchSaveCoupon(ConcurrentHashSet<User> existsPhone, Coupon coupon, String sessionId, String name) {
        Iterator<User> iterator = existsPhone.iterator();
        List<UserCoupon> userCouponList = new ArrayList<>();
        List<CouponIssueOperateRecord> couponIssueOperateRecords = new ArrayList<>();
        int maxSize = 300;
        int size = 0;
        LocalDateTime now = LocalDateTime.now().plusDays(coupon.getDays());

        while (iterator.hasNext()) {
            if (size >= 300) {
                userCouponMapper.batchInsert(userCouponList);
                couponIssueOperateRecordService.batchInsert(couponIssueOperateRecords);
                userCouponList.clear();
                couponIssueOperateRecords.clear();
                size = 0;
                continue;
            }

            User user = iterator.next();
            UserCoupon saveCoupon = new UserCoupon();
            saveCoupon.setSource(UserCoupon.TYPE_SOURCE_ADMIN_SEND);
            saveCoupon.setCouponId(coupon.getId());
            saveCoupon.setName(coupon.getName());
            saveCoupon.setDiscountType(coupon.getDiscountType());
            saveCoupon.setUid(user.getUid());
            saveCoupon.setPhone(user.getPhone());
            saveCoupon.setDeadline(TimeUtils.convertTimeStamp(now));
            saveCoupon.setCreateTime(System.currentTimeMillis());
            saveCoupon.setUpdateTime(System.currentTimeMillis());
            saveCoupon.setStatus(UserCoupon.STATUS_UNUSED);
            saveCoupon.setDelFlag(UserCoupon.DEL_NORMAL);
            saveCoupon.setTenantId(user.getTenantId());
            userCouponList.add(saveCoupon);

            CouponIssueOperateRecord record = CouponIssueOperateRecord.builder()
                    .couponId(coupon.getId())
                    .tenantId(user.getTenantId())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .uid(user.getUid())
                    .name(user.getName())
                    .operateName(name)
                    .phone(user.getPhone()).build();
            couponIssueOperateRecords.add(record);
            size++;
        }
        redisService.set(CacheConstant.CACHE_BATCH_SEND_COUPON + sessionId, "1", 60L, TimeUnit.SECONDS);
    }

    @Override
    public Integer updateUserCouponStatus(UserCoupon userCoupon) {
        return userCouponMapper.updateUserCouponStatus(userCoupon);
    }

    @Override
    public UserCoupon selectBySourceOrderId(String orderId) {
        return userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getSourceOrderId,orderId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendCouponToUser(UserCouponDTO userCouponDTO) {
        MDC.put(CommonConstant.TRACE_ID, userCouponDTO.getTraceId());
        String lockValue = userCouponDTO.getSourceOrderNo() + "_" + userCouponDTO.getCouponId() + "_" + userCouponDTO.getUid();
        if (!redisService
                .setNx(CacheConstant.CACHE_SEND_COUPON_PACKAGE_PURCHASE_KEY + lockValue, lockValue, 10 * 1000L, false)) {
            log.error("Handle activity for real name auth error, operations frequently, uid = {}", userCouponDTO.getUid());
        }

        try{
            log.info("send coupon to user start for purchase package, source order number = {}, coupon id = {}, uid = {}",userCouponDTO.getSourceOrderNo(), userCouponDTO.getCouponId(), userCouponDTO.getUid());
            //Integer tenantId = TenantContextHolder.getTenantId();
            Long uid = userCouponDTO.getUid();
            Long couponId = userCouponDTO.getCouponId();

            UserInfo userInfo = userInfoService.queryByUidFromDb(uid);
            if(Objects.isNull(userInfo)){
                log.error("send coupon failed! not found user,uid = {}",uid);
                return;
            }

            Coupon coupon = couponService.queryByIdFromDB(couponId.intValue());
            if (Objects.isNull(coupon)) {
                log.error("query coupon issue! not found coupon ! couponId = {} ", couponId);
                return;
            }

            UserCoupon.UserCouponBuilder couponBuild = UserCoupon.builder()
                    .name(coupon.getName())
                    .source(UserCoupon.TYPE_SOURCE_BUY_PACKAGE)
                    .couponId(coupon.getId())
                    .discountType(coupon.getDiscountType())
                    .status(UserCoupon.STATUS_UNUSED)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .tenantId(coupon.getTenantId())
                    .uid(uid)
                    .phone(userInfo.getPhone())
                    .sourceOrderId(userCouponDTO.getSourceOrderNo());

            //优惠券过期时间
            LocalDateTime now = LocalDateTime.now().plusDays(coupon.getDays());
            couponBuild.deadline(TimeUtils.convertTimeStamp(now));

            UserCoupon userCoupon = couponBuild.build();
            userCouponMapper.insert(userCoupon);

            log.info("send coupon to user end for purchase package, source order number = {}, coupon id = {}, uid = {}",userCouponDTO.getSourceOrderNo(), userCouponDTO.getCouponId(), userCouponDTO.getUid());

        }catch (Exception e){
            log.error("Send coupon to user for purchase package error, uid = {}, coupon id = {}, source order number = {}", userCouponDTO.getUid(), userCouponDTO.getCouponId(), userCouponDTO.getSourceOrderNo(), e);
            throw new BizException("200000", e.getMessage());
        }finally {
            redisService.delete(CacheConstant.CACHE_SEND_COUPON_PACKAGE_PURCHASE_KEY + lockValue);
            MDC.clear();
        }

    }

    @Override
    public void asyncSendCoupon(UserCouponDTO userCouponDTO) {

        executorService.execute(() -> {
            //购买套餐后发送优惠券给用户
            sendCouponToUser(userCouponDTO);
        });
    }
}
