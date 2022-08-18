package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetPhysicsOperRecord;

import java.util.List;

/**
 * (ElectricityCabinetPhysicsOperRecord)表服务接口
 *
 * @author Hardy
 * @since 2022-08-16 15:31:13
 */
public interface ElectricityCabinetPhysicsOperRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetPhysicsOperRecord queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetPhysicsOperRecord queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetPhysicsOperRecord> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param electricityCabinetPhysicsOperRecord 实例对象
     * @return 实例对象
     */
    ElectricityCabinetPhysicsOperRecord insert(ElectricityCabinetPhysicsOperRecord electricityCabinetPhysicsOperRecord);

    /**
     * 修改数据
     *
     * @param electricityCabinetPhysicsOperRecord 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinetPhysicsOperRecord electricityCabinetPhysicsOperRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    R electricityCabinetOperRecordList(Integer size, Integer offset, Integer eleId, Integer type, Long beginTime, Long endTime, Integer cellNo);
}
