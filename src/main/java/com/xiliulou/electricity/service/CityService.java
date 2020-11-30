package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.City;
import java.util.List;

/**
 * (City)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 16:20:18
 */
public interface CityService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param cid 主键
     * @return 实例对象
     */
    City queryByIdFromDB(Integer cid);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param cid 主键
     * @return 实例对象
     */
    City queryByIdFromCache(Integer cid);


    List<City> queryByPid(Integer pid);
}