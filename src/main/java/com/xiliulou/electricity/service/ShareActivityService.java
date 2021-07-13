package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.query.ActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ActivityQuery;
import com.xiliulou.electricity.query.FranchiseeActivityQuery;

/**
 * 活动表(Activity)表服务接口
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface ShareActivityService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivity queryByIdFromDB(Integer id);

      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivity queryByIdFromCache(Integer id);

    /**
     * 新增数据
     *
     * @param activityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R insert(ActivityAddAndUpdateQuery activityAddAndUpdateQuery);

    /**
     * 修改数据
     *
     * @param activityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R update(ActivityAddAndUpdateQuery activityAddAndUpdateQuery);


    R delete(Integer id);


    R queryList(ActivityQuery activityQuery);

    R queryInfo(Integer id,Boolean flag);

    R queryCount(ActivityQuery activityQuery);


    R franchiseeHome();

    R systemHome();

    R franchiseeCenter();

    R systemCenter();

    R activityInfo(Integer id);

    void handelActivityExpired();
}
