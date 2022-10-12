package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.OtaFileEleSha256;

import java.util.List;

/**
 * (OtaFileEleSha256)表服务接口
 *
 * @author zgw
 * @since 2022-10-12 17:31:10
 */
public interface OtaFileEleSha256Service {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    OtaFileEleSha256 queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    OtaFileEleSha256 queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<OtaFileEleSha256> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param otaFileEleSha256 实例对象
     * @return 实例对象
     */
    OtaFileEleSha256 insert(OtaFileEleSha256 otaFileEleSha256);
    
    /**
     * 修改数据
     *
     * @param otaFileEleSha256 实例对象
     * @return 实例对象
     */
    Integer update(OtaFileEleSha256 otaFileEleSha256);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
}
