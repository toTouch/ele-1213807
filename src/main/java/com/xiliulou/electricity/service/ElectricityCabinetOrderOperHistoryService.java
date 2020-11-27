package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import java.util.List;

/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
public interface ElectricityCabinetOrderOperHistoryService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetOrderOperHistory queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetOrderOperHistory queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetOrderOperHistory> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param electricityCabinetOrderOperHistory 实例对象
     * @return 实例对象
     */
    ElectricityCabinetOrderOperHistory insert(ElectricityCabinetOrderOperHistory electricityCabinetOrderOperHistory);

    /**
     * 修改数据
     *
     * @param electricityCabinetOrderOperHistory 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinetOrderOperHistory electricityCabinetOrderOperHistory);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

}