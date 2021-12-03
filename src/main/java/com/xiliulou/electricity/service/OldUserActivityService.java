package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.OldUserActivity;
import com.xiliulou.electricity.query.OldUserActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.OldUserActivityQuery;

/**
 * 活动表(Activity)表服务接口
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface OldUserActivityService {


      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
      OldUserActivity queryByIdFromCache(Integer id);

    /**
     * 新增数据
     *
     * @param oldUserActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R insert(OldUserActivityAddAndUpdateQuery oldUserActivityAddAndUpdateQuery);


    /**
     * 修改数据
     *
     * @param oldUserActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R update(OldUserActivityAddAndUpdateQuery oldUserActivityAddAndUpdateQuery);


    R queryList(OldUserActivityQuery oldUserActivityQuery);


    R queryCount(OldUserActivityQuery oldUserActivityQuery);


    R queryInfo(Integer id);

	void handleActivityExpired();
}
