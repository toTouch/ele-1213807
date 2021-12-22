package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.NewUserActivity;
import com.xiliulou.electricity.query.NewUserActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.NewUserActivityQuery;

/**
 * 活动表(Activity)表服务接口
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface NewUserActivityService {


      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
      NewUserActivity queryByIdFromCache(Integer id);

    /**
     * 新增数据
     *
     * @param newUserActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R insert(NewUserActivityAddAndUpdateQuery newUserActivityAddAndUpdateQuery);


    /**
     * 修改数据
     *
     * @param newUserActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R update(NewUserActivityAddAndUpdateQuery newUserActivityAddAndUpdateQuery);


    R queryList(NewUserActivityQuery newUserActivityQuery);


    R queryCount(NewUserActivityQuery newUserActivityQuery);


    R queryInfo(Integer id);

    R queryNewUserActivity();

    NewUserActivity queryActivity();
}
