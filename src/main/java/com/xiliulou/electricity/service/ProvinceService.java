package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.Province;
import java.util.List;

/**
 * (Province)表服务接口
 *
 * @author makejava
 * @since 2021-01-21 18:05:46
 */
public interface ProvinceService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    Province queryByIdFromDB(Integer id);


	List<Province> queryList();
}
