package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import java.util.List;

/**
 * (ElectricityCabinetServer)表服务接口
 *
 * @author Hardy
 * @since 2022-09-26 11:40:34
 */
public interface ElectricityCabinetServerService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetServer queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetServer queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetServer> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param electricityCabinetServer 实例对象
     * @return 实例对象
     */
    ElectricityCabinetServer insert(ElectricityCabinetServer electricityCabinetServer);

    ElectricityCabinetServer queryByProductKeyAndDeviceName(String productKey, String deviceName);

    /**
     * 修改数据
     *
     * @param electricityCabinetServer 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinetServer electricityCabinetServer);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    R queryList(String eleName, String deviceName, String tenantName, Long serverTimeStart, Long serverTimeEnd,
        Long offset, Long size);

    R deleteOne(Long id);

    void insertOrUpdateByElectricityCabinet(ElectricityCabinet electricityCabinet,
        ElectricityCabinet oldElectricityCabinet);

    R updateOne(Long id, Long serverTimeStart, Long serverTimeEnd);

    ElectricityCabinetServer selectByEid(Integer id);

    Integer deleteByEid(Integer id);
}
