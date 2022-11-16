package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryChargeConfig;
import com.xiliulou.electricity.query.BatteryChargeConfigQuery;
import com.xiliulou.electricity.vo.BatteryChargeConfigVO;

import java.util.List;

/**
 * (BatteryChargeConfig)表服务接口
 *
 * @author zzlong
 * @since 2022-08-12 14:49:37
 */
public interface BatteryChargeConfigService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryChargeConfig selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryChargeConfig selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<BatteryChargeConfig> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param batteryChargeConfig 实例对象
     * @return 实例对象
     */
    BatteryChargeConfig insert(BatteryChargeConfigQuery batteryChargeConfig);

    /**
     * 修改数据
     *
     * @param batteryChargeConfig 实例对象
     * @return 实例对象
     */
    Integer update(BatteryChargeConfigQuery batteryChargeConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    BatteryChargeConfigVO selectByElectricityCabinetId(BatteryChargeConfigQuery query);

    int insertOrUpdate(BatteryChargeConfigQuery query);
}
