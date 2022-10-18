package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetServerOperRecord;
import java.util.List;

/**
 * (ElectricityCabinetServerOperRecord)表服务接口
 *
 * @author zgw
 * @since 2022-09-26 17:54:54
 */
public interface ElectricityCabinetServerOperRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetServerOperRecord queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetServerOperRecord queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetServerOperRecord> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param electricityCabinetServerOperRecord 实例对象
     * @return 实例对象
     */
    ElectricityCabinetServerOperRecord insert(ElectricityCabinetServerOperRecord electricityCabinetServerOperRecord);

    /**
     * 修改数据
     *
     * @param electricityCabinetServerOperRecord 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinetServerOperRecord electricityCabinetServerOperRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    R queryList(String createUserName, Long eleServerId, Long offset, Long size);
}
