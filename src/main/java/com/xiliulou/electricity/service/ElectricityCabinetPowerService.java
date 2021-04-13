package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.query.ElectricityCabinetPowerQuery;

import java.util.List;

/**
 * 换电柜电量表(ElectricityCabinetPower)表服务接口
 *
 * @author makejava
 * @since 2021-01-27 16:22:44
 */
public interface ElectricityCabinetPowerService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetPower queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetPower queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetPower> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param electricityCabinetPower 实例对象
     * @return 实例对象
     */
    ElectricityCabinetPower insert(ElectricityCabinetPower electricityCabinetPower);

    /**
     * 修改数据
     *
     * @param electricityCabinetPower 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinetPower electricityCabinetPower);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);


    Integer insertOrUpdate(ElectricityCabinetPower electricityCabinetPower);

    R queryList(ElectricityCabinetPowerQuery electricityCabinetPowerQuery);
}