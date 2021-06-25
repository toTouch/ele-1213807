package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.City;
import java.util.List;

/**
 * (City)表服务接口
 *
 * @author makejava
 * @since 2021-01-21 18:05:41
 */
public interface CityService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    City queryByIdFromDB(Integer id);

    City queryCityByCode(String cityCode);

    List<City> queryCityListByPid(Integer pid);

}
