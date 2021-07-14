package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import java.util.List;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表服务接口
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareActivityRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    JoinShareActivityRecord queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    JoinShareActivityRecord queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<JoinShareActivityRecord> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param joinShareActivityRecord 实例对象
     * @return 实例对象
     */
    JoinShareActivityRecord insert(JoinShareActivityRecord joinShareActivityRecord);

    /**
     * 修改数据
     *
     * @param joinShareActivityRecord 实例对象
     * @return 实例对象
     */
    Integer update(JoinShareActivityRecord joinShareActivityRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

}