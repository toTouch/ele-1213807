package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserCoupon;
import java.util.List;

import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.vo.UserCouponVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * 优惠券表(TCoupon)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
public interface UserCouponMapper extends BaseMapper<UserCoupon>{


    /**
     * 查询指定行数据
     *
     */
    List<UserCouponVO> queryList(@Param("query") UserCouponQuery userCouponQuery);

    @Select(" select id, name, uid, phone, coupon_id, deadline, order_id, create_time, update_time, status, source " +
            "from t_user_coupon where deadline < #{currentTime} and status = 1 and del_flag = 0 limit #{offset},#{size}")
    List<UserCoupon> getExpiredUserCoupon(@Param("currentTime") Long currentTime, @Param("offset") Integer offset, @Param("size") Integer size);

    Integer queryCount(@Param("query") UserCouponQuery userCouponQuery);

    int update(UserCoupon userCoupon);

    int updateStatus(UserCoupon userCoupon);
}
