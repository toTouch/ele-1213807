package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleOtaUpgradeHistory;

import java.util.List;

/**
 * (EleOtaUpgradeHistory)表服务接口
 *
 * @author Hardy
 * @since 2022-10-14 14:35:40
 */
public interface EleOtaUpgradeHistoryService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleOtaUpgradeHistory queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleOtaUpgradeHistory queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleOtaUpgradeHistory> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param eleOtaUpgradeHistory 实例对象
     * @return 实例对象
     */
    EleOtaUpgradeHistory insert(EleOtaUpgradeHistory eleOtaUpgradeHistory);
    
    /**
     * 修改数据
     *
     * @param eleOtaUpgradeHistory 实例对象
     * @return 实例对象
     */
    Integer update(EleOtaUpgradeHistory eleOtaUpgradeHistory);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    EleOtaUpgradeHistory queryByUpgradeNo(String upgradeNo);
    
    R queryList(Integer eid, Integer cellNo, Integer type, String upgradeVersion, String historyVersion, String status,
            Long startTime, Long endTime, Long offset, Long size);
}
