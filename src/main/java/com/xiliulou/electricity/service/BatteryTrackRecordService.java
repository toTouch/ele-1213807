package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryTrackRecord;
import java.util.List;

/**
 * (BatteryTrackRecord)表服务接口
 *
 * @author makejava
 * @since 2023-01-03 16:24:37
 */
public interface BatteryTrackRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryTrackRecord queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryTrackRecord queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<BatteryTrackRecord> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param batteryTrackRecord 实例对象
     * @return 实例对象
     */
    BatteryTrackRecord insert(BatteryTrackRecord batteryTrackRecord);

    /**
     * 修改数据
     *
     * @param batteryTrackRecord 实例对象
     * @return 实例对象
     */
    Integer update(BatteryTrackRecord batteryTrackRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

}
