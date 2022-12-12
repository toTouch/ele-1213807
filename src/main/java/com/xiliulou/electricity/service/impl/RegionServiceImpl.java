package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Region;
import com.xiliulou.electricity.mapper.RegionMapper;
import com.xiliulou.electricity.service.RegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * (Region)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-12 11:38:20
 */
@Service("regionService")
@Slf4j
public class RegionServiceImpl implements RegionService {
    @Autowired
    private RegionMapper regionMapper;
    @Autowired
    private RedisService redisService;

    @Override
    public Region selectByIdFromCache(Integer id) {

        Region cacheRegion = redisService.getWithHash(CacheConstant.CACHE_REGION_ID + id, Region.class);
        if (Objects.nonNull(cacheRegion)) {
            return cacheRegion;
        }

        Region region = this.selectByIdFromDB(id);
        if (Objects.isNull(region)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_REGION_ID + id, region);

        return region;
    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Region selectByIdFromDB(Integer id) {
        return this.regionMapper.selectById(id);
    }

    @Override
    public Region selectByCodeFromCache(String code) {
        Region cacheRegion = redisService.getWithHash(CacheConstant.CACHE_REGION_CODE + code, Region.class);
        if (Objects.nonNull(cacheRegion)) {
            return cacheRegion;
        }

        Region region = this.selectByCode(code);
        if (Objects.isNull(region)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_REGION_CODE + code, region);

        return region;
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param code 主键
     * @return 实例对象
     */
    @Override
    public Region selectByCode(String code) {
        return this.regionMapper.selectByCode(code);
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<Region> selectByPage(int offset, int limit) {
        return this.regionMapper.selectByPage(offset, limit);
    }


}
