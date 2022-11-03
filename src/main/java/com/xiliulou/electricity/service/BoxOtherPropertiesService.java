package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BoxOtherProperties;

import java.util.List;

/**
 * 换电柜仓门其它属性(BoxOtherProperties)表服务接口
 *
 * @author zzlong
 * @since 2022-11-03 19:51:35
 */
public interface BoxOtherPropertiesService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BoxOtherProperties selectByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    BoxOtherProperties selectByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<BoxOtherProperties> selectByPage(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param boxOtherProperties 实例对象
     * @return 实例对象
     */
    BoxOtherProperties insert(BoxOtherProperties boxOtherProperties);
    
    /**
     * 修改数据
     *
     * @param boxOtherProperties 实例对象
     * @return 实例对象
     */
    Integer update(BoxOtherProperties boxOtherProperties);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
}
