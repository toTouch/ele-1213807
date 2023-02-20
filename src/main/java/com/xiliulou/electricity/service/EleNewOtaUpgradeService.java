package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleNewOtaUpgrade;

import java.util.List;

/**
 * (EleNewOtaUpgrade)表服务接口
 *
 * @author Hardy
 * @since 2023-02-20 15:58:54
 */
public interface EleNewOtaUpgradeService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleNewOtaUpgrade queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleNewOtaUpgrade queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleNewOtaUpgrade> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param eleNewOtaUpgrade 实例对象
     * @return 实例对象
     */
    EleNewOtaUpgrade insert(EleNewOtaUpgrade eleNewOtaUpgrade);
    
    /**
     * 修改数据
     *
     * @param eleNewOtaUpgrade 实例对象
     * @return 实例对象
     */
    Integer update(EleNewOtaUpgrade eleNewOtaUpgrade);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
}
