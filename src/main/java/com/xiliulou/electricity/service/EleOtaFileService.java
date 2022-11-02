package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleOtaFile;
import com.xiliulou.electricity.mapper.EleOtaFileMapper;

import java.util.List;

/**
 *
 * @author zgw
 * @since 2022-10-12 17:31:10
 */
public interface EleOtaFileService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleOtaFile queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleOtaFile queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleOtaFile> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @return 实例对象
     */
    EleOtaFile insert(EleOtaFile eleOtaFile);
    
    /**
     * 修改数据
     *
     * @return 实例对象
     */
    Integer update(EleOtaFile eleOtaFile);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    EleOtaFile queryByEid(Integer id);
    
    R queryInfo(Integer eid);
}
