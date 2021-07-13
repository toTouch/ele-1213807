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
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserCoupon queryByIdFromDB(Long id);

    /**
     * 新增数据
     *
     * @param userCoupon 实例对象
     * @return 实例对象
     */
    UserCoupon insert(UserCoupon userCoupon);

    /**
     * 修改数据
     *
     * @param userCoupon 实例对象
     * @return 实例对象
     */
    Integer update(UserCoupon userCoupon);


    R queryList(UserCouponQuery userCouponQuery);

    R batchRelease(Integer id,   Long[] uidS);

    void handelUserCouponExpired();


    R queryMyCoupon( List<Integer> statusList,List<Integer> typeList);

    R getCoupon(List<Integer> couponIdList,Integer id,Integer type);
}
