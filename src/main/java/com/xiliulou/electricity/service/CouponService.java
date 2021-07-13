package com.xiliulou.electricity.service;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.query.CouponQuery;

/**
 * 优惠券规则表(TCoupon)表服务接口
 *
 * @author makejava
 * @since 2021-04-14 09:28:22
 */
public interface CouponService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    Coupon queryByIdFromDB(Integer id);

      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    Coupon queryByIdFromCache(Integer id);

    /**
     * 新增数据
     *
     * @param coupon 实例对象
     * @return 实例对象
     */
    R insert(Coupon coupon);

    /**
     * 修改数据
     *
     * @param coupon 实例对象
     * @return 实例对象
     */
    R update(Coupon coupon);


    R delete(Integer id);


    R queryList(CouponQuery couponQuery);


    void handelCouponExpired();
}
