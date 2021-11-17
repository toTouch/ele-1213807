package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.OldUserActivity;
import com.xiliulou.electricity.query.OldUserActivityQuery;
import org.apache.ibatis.annotations.Param;

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

}
