package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.biz.RentalPackageBizService;
import com.xiliulou.electricity.service.car.biz.SlippageBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租赁套餐相关的业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class RentalPackageBizServiceImpl implements RentalPackageBizService {

    @Resource
    private CouponService couponService;

    @Resource
    private UserCouponService userCouponService;
    
    @Resource
    private SlippageBizService slippageBizService;

    @Resource
    private UserOauthBindService userOauthBindService;

    @Resource
    private ElectricityPayParamsService electricityPayParamsService;

    @Resource
    private UserInfoService userInfoService;

    /**
     * 计算需要支付的金额<br />
     * 目前优惠券只有一种减免金额卷
     * @param amount    原金额
     * @param userCouponIds 用户的优惠券ID集合
     * @param uid       用户ID
     * @return Triple<BigDecimal, List<Long>, Boolean> 实际支付金额、已用的用户优惠券ID、Boolean（暂无实际意义）
     */
    @Override
    public Triple<BigDecimal, List<Long>, Boolean> calculatePaymentAmount(BigDecimal amount, List<Long> userCouponIds, Long uid) {
        if (BigDecimal.ZERO.compareTo(amount) == 0 || CollectionUtils.isEmpty(userCouponIds)) {
            return Triple.of(BigDecimal.ZERO, null, true) ;
        }

        // 查询用户名下是否存在未使用、未过期的优惠券
        List<UserCoupon> userCoupons = userCouponService.selectEffectiveByUid(uid, userCouponIds, System.currentTimeMillis());
        if (CollectionUtils.isEmpty(userCoupons)) {
            return Triple.of(BigDecimal.ZERO, null, true) ;
        }
        List<Integer> couponIdList = userCoupons.stream().map(UserCoupon::getCouponId).distinct().collect(Collectors.toList());

        // 查询优惠券信息
        CouponQuery couponQuery = CouponQuery.builder().ids(couponIdList).build();
        R couponResult = couponService.queryList(couponQuery);
        if (!couponResult.isSuccess()) {
            throw new BizException(couponResult.getErrMsg());
        }

        List<Coupon> couponList = (List<Coupon>) couponResult.getData();

        // 按照优惠券是否可叠加分组
        Map<Integer, List<Coupon>> superpositionMap = couponList.stream().collect(Collectors.groupingBy(Coupon::getSuperposition));
        if (superpositionMap.size() == 2 || (superpositionMap.size() == 1 && superpositionMap.containsKey(Coupon.SUPERPOSITION_NO) && superpositionMap.get(Coupon.SUPERPOSITION_NO).size() > 1)) {
            throw new BizException("使用优惠券有误");
        }

        // 真正使用的用户优惠券ID
        List<Long> userCouponIdList = userCoupons.stream().map(UserCoupon::getId).distinct().collect(Collectors.toList());

        // 计算总共减免金额
        BigDecimal discountAmount = couponList.stream().map(coupon -> coupon.getAmount()).reduce(BigDecimal::add).get();
        if (discountAmount.compareTo(amount) >= 0) {
            return Triple.of(BigDecimal.ZERO, userCouponIdList, true) ;
        }

        // 实际支付金额
        BigDecimal payAmount = amount.subtract(discountAmount);

        return Triple.of(payAmount, userCouponIdList, true) ;
    }

    /**
     * 购买套餐订单统一检测
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     */
    @Override
    public void checkBuyPackageCommon(Integer tenantId, Long uid) {
        // 1 获取用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("CheckBuyPackageCommon failed. Not found user. uid is {} ", uid);
            throw new BizException("未找到用户");
        }

        // 1.1 用户可用状态
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CheckBuyPackageCommon failed. User is unUsable. uid is {} ", uid);
            throw new BizException("用户已被禁用");
        }

        // 1.2 用户实名认证状态
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("CheckBuyPackageCommon failed. User not auth. uid is {}", uid);
            throw new BizException("用户尚未实名认证");
        }

        // 2. 支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("CheckBuyPackageCommon failed. Not found pay_params. uid is {}", uid);
            throw new BizException("未配置支付参数");
        }

        // 3. 三方授权相关
        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CheckBuyPackageCommon failed. Not found useroauthbind or thirdid is null. uid is {}", uid);
            throw new BizException("未找到用户的第三方授权信息");
        }

        // 4. 判定滞纳金
        if (slippageBizService.isExitUnpaid(tenantId, uid)) {
            log.error("CheckBuyPackageCommon failed. Not found useroauthbind or thirdid is null. uid is {}", uid);
            throw new BizException("存在滞纳金，请先缴纳");
        }

        // 5.

    }
}
