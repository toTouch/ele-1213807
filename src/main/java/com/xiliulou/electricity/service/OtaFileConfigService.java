package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.OtaFileConfig;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * (OtaFileConfig)表服务接口
 *
 * @author Hardy
 * @since 2022-10-12 09:24:48
 */
public interface OtaFileConfigService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    OtaFileConfig queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    OtaFileConfig queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<OtaFileConfig> queryAllByLimit(int offset, int limit);
    
    List<OtaFileConfig> queryAll();
    
    /**
     * 新增数据
     *
     * @param otaFileConfig 实例对象
     * @return 实例对象
     */
    OtaFileConfig insert(OtaFileConfig otaFileConfig);
    
    /**
     * 修改数据
     *
     * @param otaFileConfig 实例对象
     * @return 实例对象
     */
    Integer update(OtaFileConfig otaFileConfig);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    OtaFileConfig queryByType(Integer type);
    
    R otaFileConfigUpload(MultipartFile file, String name, String version, Integer type);
    
    R otaFileConfigDelete(Long id);
    
    R otaFileConfigQueryList();
}
