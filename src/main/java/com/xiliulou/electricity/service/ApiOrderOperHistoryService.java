package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ApiOrderOperHistory;
import java.util.List;

/**
 * (ApiOrderOperHistory)表服务接口
 *
 * @author makejava
 * @since 2021-11-09 16:57:54
 */
public interface ApiOrderOperHistoryService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiOrderOperHistory queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiOrderOperHistory queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ApiOrderOperHistory> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param apiOrderOperHistory 实例对象
     * @return 实例对象
     */
    ApiOrderOperHistory insert(ApiOrderOperHistory apiOrderOperHistory);

    /**
     * 修改数据
     *
     * @param apiOrderOperHistory 实例对象
     * @return 实例对象
     */
    Integer update(ApiOrderOperHistory apiOrderOperHistory);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

}
