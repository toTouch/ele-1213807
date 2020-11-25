package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.mapper.CityMapper;
import com.xiliulou.electricity.service.CityService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * (City)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 16:20:18
 */
@Service("cityService")
public class CityServiceImpl implements CityService {
    @Resource
    private CityMapper cityMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param cid 主键
     * @return 实例对象
     */
    @Override
    public City queryByIdFromDB(Integer cid) {
        return this.cityMapper.queryById(cid);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param cid 主键
     * @return 实例对象
     */
    @Override
    public  City queryByIdFromCache(Integer cid) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<City> queryAllByLimit(int offset, int limit) {
        return this.cityMapper.queryAllByLimit(offset, limit);
    }

}