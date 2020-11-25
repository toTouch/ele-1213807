package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.Provincial;
import java.util.List;

/**
 * (Provincial)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 16:20:43
 */
public interface ProvincialService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param pid 主键
     * @return 实例对象
     */
    Provincial queryByIdFromDB(Integer pid);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param pid 主键
     * @return 实例对象
     */
    Provincial queryByIdFromCache(Integer pid);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Provincial> queryAllByLimit(int offset, int limit);


}