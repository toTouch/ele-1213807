package com.xiliulou.electricity.service;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.query.CouponQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 优惠券规则表(TCoupon)表服务接口
 *
 * @author makejava
 * @since 2021-04-14 09:28:22
 */
public interface CouponService {


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



    R queryList(CouponQuery couponQuery);



    R queryCount(CouponQuery couponQuery);
    
    Triple<Boolean, String, Object> deleteById(Long id);
}
