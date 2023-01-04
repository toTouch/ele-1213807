package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleBatterySnapshot;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * (EleBatterySnapshot)表服务接口
 *
 * @author makejava
 * @since 2023-01-04 09:21:26
 */
public interface EleBatterySnapshotService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleBatterySnapshot queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleBatterySnapshot queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<EleBatterySnapshot> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param eleBatterySnapshot 实例对象
     * @return 实例对象
     */
    EleBatterySnapshot insert(EleBatterySnapshot eleBatterySnapshot);

    /**
     * 修改数据
     *
     * @param eleBatterySnapshot 实例对象
     * @return 实例对象
     */
    Integer update(EleBatterySnapshot eleBatterySnapshot);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    Pair<Boolean, Object> queryBatterySnapshot(Integer eId, Integer size, Integer offset, Long startTime, Long endTime);
}
