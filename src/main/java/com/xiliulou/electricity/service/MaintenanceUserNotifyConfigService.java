package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.MaintenanceUserNotifyConfig;
import com.xiliulou.electricity.query.MaintenanceUserNotifyConfigQuery;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * (MaintenanceUserNotifyConfig)表服务接口
 *
 * @author makejava
 * @since 2022-04-12 09:07:48
 */
public interface MaintenanceUserNotifyConfigService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    MaintenanceUserNotifyConfig queryByTenantIdFromDB(Integer id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    MaintenanceUserNotifyConfig queryByTenantIdFromCache(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<MaintenanceUserNotifyConfig> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param maintenanceUserNotifyConfig 实例对象
     * @return 实例对象
     */
    MaintenanceUserNotifyConfig insert(MaintenanceUserNotifyConfig maintenanceUserNotifyConfig);

    /**
     * 修改数据
     *
     * @param maintenanceUserNotifyConfig 实例对象
     * @return 实例对象
     */
    Integer update(MaintenanceUserNotifyConfig maintenanceUserNotifyConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id);

    Pair<Boolean, Object> queryConfigInfo();

    Pair<Boolean, Object> saveConfig(MaintenanceUserNotifyConfigQuery query);

    Pair<Boolean, Object> updateConfig(MaintenanceUserNotifyConfigQuery query);

    void sendDeviceNotifyMq(ElectricityCabinet electricityCabinet, String status, String time);

    void sendCellLockMsg(String sessionId, ElectricityCabinet electricityCabinet, Integer cellNo, String s);

//    Pair<Boolean, Object> testSendMsg();

}
