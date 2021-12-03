package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.OldUserActivity;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.query.OldUserActivityQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动表(TActivity)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface OldUserActivityMapper extends BaseMapper<OldUserActivity>{



    /**
     * 查询指定行数据
     *
     */
    List<OldUserActivity> queryList(@Param("query") OldUserActivityQuery oldUserActivityQuery);


    Integer queryCount(@Param("query")  OldUserActivityQuery oldUserActivityQuery);

	@Select(" select * from t_old_user_activity where time_type=1 and end_time < #{currentTime} and status = 1 and del_flag = 0 limit #{offset},#{size}")
	List<OldUserActivity> getExpiredActivity(@Param("currentTime") Long currentTime, @Param("offset") Integer offset, @Param("size") Integer size);
}
