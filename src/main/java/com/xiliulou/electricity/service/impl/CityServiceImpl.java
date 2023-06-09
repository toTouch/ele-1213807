package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.Region;
import com.xiliulou.electricity.mapper.CityMapper;
import com.xiliulou.electricity.service.CityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (City)表服务实现类
 *
 * @author makejava
 * @since 2021-01-21 18:05:43
 */
@Service("cityService")
@Slf4j
public class CityServiceImpl implements CityService {
    @Resource
    private CityMapper cityMapper;
    @Autowired
    private RedisService redisService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public City queryByIdFromDB(Integer id) {
        return this.cityMapper.selectById(id);
    }

    @Override
    public City queryByCodeFromCache(String cityCode) {

        City cacheCity = redisService.getWithHash(CacheConstant.CACHE_CITY_CODE + cityCode, City.class);
        if (Objects.nonNull(cacheCity)) {
            return cacheCity;
        }

        City city = this.queryCityByCode(cityCode);
        if (Objects.isNull(city)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_REGION_CODE + cityCode, city);

        return city;
    }

    @Override
    public City queryCityByCode(String code) {
        return cityMapper.selectOne(new LambdaQueryWrapper<City>().eq(City::getCode, code));
    }

    @Slave
    @Override
    public List<City> queryCityListByPid(Integer pid) {
        return cityMapper.selectList(new LambdaQueryWrapper<City>().eq(City::getPid, pid));
    }

    @Override
    public List<City> selectByCids(List<Integer> cids) {
        return cityMapper.selectList(new LambdaQueryWrapper<City>().in(City::getId, cids));
    }
}
