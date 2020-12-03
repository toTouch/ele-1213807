package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Provincial;
import com.xiliulou.electricity.mapper.CityMapper;
import com.xiliulou.electricity.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

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
    @Autowired
    RedisService redisService;

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
        //先查缓存
        City cacheCity = redisService.getWithHash(ElectricityCabinetConstant.CACHE_CITY + cid, City.class);
        if (Objects.nonNull(cacheCity)) {
            return cacheCity;
        }
        //缓存没有再查数据库
        City city =cityMapper.queryById(cid);
        if (Objects.isNull(city)) {
            return null;
        }
        //放入缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_CITY + cid, city);
        return city;
    }

    @Override
    public List<City> queryByPid(Integer pid) {
        return cityMapper.selectList(Wrappers.<City>lambdaQuery().eq(City::getPid,pid));
    }

}