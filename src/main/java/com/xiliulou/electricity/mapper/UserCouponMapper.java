package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.vo.UserCouponVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 优惠券表(TCoupon)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
public interface UserCouponMapper extends BaseMapper<UserCoupon>{

    /**
     * 根据来源订单编码作废掉未使用的优惠券
     *
     * @param sourceOrderId     来源订单编码
     * @param optTime 操作时间
     * @return 操作条数
     */
    int cancelBySourceOrderIdAndUnUse(@Param("sourceOrderId") String sourceOrderId, @Param("optTime") Long optTime);

    /**
     * 根据订单编码更新优惠券状态
     *
     * @param orderId     订单编码
     * @param status      状态
     * @param optTime      操作时间
     * @return 操作条数
     */
    int updateStatusByOrderId(@Param("orderId") String orderId, @Param("status") Integer status, @Param("optTime") Long optTime);

    /**
     * 查询用户名下有效的优惠券
     *
     * @param uid      用户ID
     * @param ids      主键ID
     * @param deadline 到期时间
     * @return
     */
    List<UserCoupon> selectEffectiveByUid(@Param("uid") Long uid, @Param("ids") List<Long> ids, @Param("deadline") Long deadline);

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

    Integer batchUpdateUserCoupon(List<UserCoupon> buildUserCouponList);

    Integer updateUserCouponStatus(UserCoupon userCoupon);
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);
    
    Integer batchInsert(@Param("userCouponList") List<UserCoupon> userCouponList);
}
