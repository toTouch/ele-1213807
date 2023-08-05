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

    /**
     * 根据订单编码作废掉未使用的优惠券
     * @param orderId 订单编码
     * @param orderIdType 订单编码对应的类型
     * @return true(成功)、false(失败)
     */
    boolean cancelByOrderIdAndUnUse(String orderId, Integer orderIdType);

    /**
     * 根据订单编码更新优惠券状态
     * @param orderId 订单编码
     * @param orderIdType 订单编码对应的类型
     * @param status 状态
     * @return true(成功)、false(失败)
     */
    boolean updateStatusByOrderId(String orderId, Integer orderIdType, Integer status);

    /**
     * 查询用户名下有效的优惠券
     * @param uid 用户ID
     * @param ids 主键ID
     * @param deadline 到期时间
     * @return
     */
    List<UserCoupon> selectEffectiveByUid(Long uid, List<Long> ids, Long deadline);

    R queryList(UserCouponQuery userCouponQuery);

    R batchRelease(Integer id, Long[] uids);

    R adminBatchRelease(Integer id, Long[] uids);

    R destruction(Long[] couponIds);

    void handelUserCouponExpired();

    R queryMyCoupon( List<Integer> statusList,List<Integer> typeList);

    R queryMyCoupons( List<Integer> statusList,List<Integer> typeList);

    R getShareCoupon(Integer activityId,Integer couponId);

    UserCoupon queryByIdFromDB(Integer userCouponId);

    UserCoupon queryByActivityIdAndCouponId(Integer activityId, Long activityRuleId, Integer couponId,Long uid);
    List<UserCoupon> selectListByActivityIdAndCouponId(Integer activityId, Long activityRuleId, Integer couponId,Long uid);

	void update(UserCoupon userCoupon);

	int updateStatus(UserCoupon userCoupon);

    R queryCount(UserCouponQuery userCouponQuery);
    
    List<UserCoupon> selectCouponUserCountById(Long id);

    Integer batchUpdateUserCoupon(List<UserCoupon> buildUserCouponList);

    Integer updateUserCouponStatus(UserCoupon userCoupon);

    UserCoupon selectBySourceOrderId(String orderId);
}
