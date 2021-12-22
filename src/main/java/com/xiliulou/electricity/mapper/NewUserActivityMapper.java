package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.NewUserActivity;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.query.NewUserActivityQuery;
import com.xiliulou.electricity.query.ShareActivityQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动表(TActivity)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface NewUserActivityMapper extends BaseMapper<NewUserActivity>{



    /**
     * 查询指定行数据
     *
     */
    List<NewUserActivity> queryList(@Param("query") NewUserActivityQuery newUserActivityQuery);


    Integer queryCount(@Param("query") NewUserActivityQuery newUserActivityQuery);

}
