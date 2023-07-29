package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.UserCouponMapper;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserCouponVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 优惠券表(TCoupon)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
@Service("userCouponService")
@Slf4j
public class UserCouponServiceImpl implements UserCouponService {
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

    /**
     * 根据订单编码更新优惠券状态
     *
     * @param orderId     订单编码
     * @param orderIdType 订单编码对应的类型
     * @param status      状态
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean updateStatusByOrderId(String orderId, Integer orderIdType, Integer status) {
        if (!ObjectUtils.allNotNull(orderId, orderIdType, status)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        int num = userCouponMapper.updateStatusByOrderId(orderId, orderIdType, status, System.currentTimeMillis());

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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
            log.error("ELECTRICITY  ERROR! not auth! uid={} ", user.getUid());
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

    @Override
    public Integer updateUserCouponStatus(UserCoupon userCoupon) {
        return userCouponMapper.updateUserCouponStatus(userCoupon);
    }

    @Override
    public UserCoupon selectBySourceOrderId(String orderId) {
        return userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getSourceOrderId,orderId));
    }
}
