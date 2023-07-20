package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Coupon;
import java.util.List;

import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.vo.SearchVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * 优惠券规则表(TCoupon)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-14 09:28:22
 */
public interface CouponMapper extends BaseMapper<Coupon>{


    /**
     * 查询指定行数据
     *
     */
    List<Coupon> queryList(@Param("query") CouponQuery couponQuery);


    @Select("select  id, name, status, apply_type, trigger_amount, discount_type, amount, discount, count, time_type, days, start_time, end_time, max_times_everyone, description, uid, user_name, create_time, update_time, del_flg" +
            " from t_coupon where time_type=1 and end_time < #{currentTime} and status = 1 and del_flg = 0 ")
    List<Coupon> getExpiredCoupon(@Param("currentTime") Long currentTime);

    Integer queryCount(@Param("query") CouponQuery couponQuery);
    
    int deleteById(@Param("id") Long id, @Param("tenantId") Integer tenantId);

    List<SearchVo> search(CouponQuery query);
}
