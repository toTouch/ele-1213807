package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.query.UserCouponQuery;

import java.util.List;

/**
 * 优惠券表(TCoupon)表服务接口
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
public interface UserCouponService {



    R queryList(UserCouponQuery userCouponQuery);

    R batchRelease(Integer id,   Long[] uids);

    void handelUserCouponExpired();

    R queryMyCoupon( List<Integer> statusList,List<Integer> typeList);

    R getShareCoupon(Integer activityId,Integer couponId);

    UserCoupon queryByIdFromDB(Integer userCouponId);

    UserCoupon queryByActivityIdAndCouponId(Integer activityId, Long activityRuleId, Integer couponId,Long uid);

	void update(UserCoupon userCoupon);

    R queryCount(UserCouponQuery userCouponQuery);
}
