package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.query.ShareActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareActivityQuery;

/**
 * 活动表(Activity)表服务接口
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface ShareActivityService {


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
     * @param shareActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R insert(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery);

    /**
     * 修改数据
     *
     * @param shareActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R update(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery);


    R queryList(ShareActivityQuery shareActivityQuery);

    R queryInfo(Integer id);

    R queryCount(ShareActivityQuery shareActivityQuery);

    R activityInfo();

}
