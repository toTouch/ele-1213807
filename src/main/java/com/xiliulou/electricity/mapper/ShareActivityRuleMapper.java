package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ShareActivityRule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 加盟商活动绑定表(TActivityBindCoupon)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-23 16:43:23
 */
public interface ShareActivityRuleMapper extends BaseMapper<ShareActivityRule> {
    
    @Update("update  t_activity_bind_coupon set status=2,update_time=#{currentTime} where activity_id = #{id} and status = 1 and del_flg = 0 ")
    void updateByActivity(Integer id, long currentTimeMillis);
    
    ShareActivityRule selectByCouponId(@Param("couponId") Long couponId);
    
    Integer removeByActivityId(@Param("activityId") Long activityId, @Param("tenantId") Integer tenantId);
}
