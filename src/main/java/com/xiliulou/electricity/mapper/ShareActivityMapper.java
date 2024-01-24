package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ShareActivity;
import java.util.List;

import com.xiliulou.electricity.query.ShareActivityQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * 活动表(TActivity)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface ShareActivityMapper extends BaseMapper<ShareActivity>{



    /**
     * 查询指定行数据
     *
     */
    List<ShareActivity> queryList(@Param("query") ShareActivityQuery shareActivityQuery);


    Integer queryCount(@Param("query") ShareActivityQuery shareActivityQuery);

    @Select("select  id, name, type, status,  description, del_flg, create_time,\n" +
            "        update_time, uid,user_name\n" +
            "        from t_share_activity where end_time < #{currentTime} and status = 1 and del_flg = 0 ")
    List<ShareActivity> getExpiredActivity(long currentTimeMillis);
    
    Integer existShareActivity(@Param("tenantId") Integer tenantId);
}
