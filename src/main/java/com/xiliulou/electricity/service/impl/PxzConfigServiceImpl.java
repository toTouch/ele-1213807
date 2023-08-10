package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.mapper.PxzConfigMapper;
import com.xiliulou.electricity.query.PxzConfigQuery;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * (PxzConfig)表服务实现类
 *
 * @author makejava
 * @since 2023-02-15 16:23:54
 */
@Service("pxzConfigService")
@Slf4j
public class PxzConfigServiceImpl implements PxzConfigService {
    
    @Resource
    private PxzConfigMapper pxzConfigMapper;
    
    @Autowired
    RedisService redisService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public PxzConfig queryByTenantIdFromDB(Integer id) {
        return this.pxzConfigMapper.queryByTenantId(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public PxzConfig queryByTenantIdFromCache(Integer id) {
        PxzConfig cache = redisService.getWithHash(CacheConstant.CACHE_PXZ_CONFIG + id, PxzConfig.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        
        PxzConfig pxzConfig = queryByTenantIdFromDB(id);
        if (Objects.isNull(pxzConfig)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_PXZ_CONFIG + id, pxzConfig);
        redisService.expire(CacheConstant.CACHE_PXZ_CONFIG + id, TimeUnit.HOURS.toMillis(24 * 30), true);
        return pxzConfig;
    }
    
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<PxzConfig> queryAllByLimit(int offset, int limit) {
        return this.pxzConfigMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param pxzConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PxzConfig insert(PxzConfig pxzConfig) {
        this.pxzConfigMapper.insertOne(pxzConfig);
        return pxzConfig;
    }
    
    /**
     * 修改数据
     *
     * @param pxzConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(PxzConfig pxzConfig) {
        return DbUtils.dbOperateSuccessThenHandleCache(this.pxzConfigMapper.update(pxzConfig), i -> {
            redisService.delete(CacheConstant.CACHE_PXZ_CONFIG + pxzConfig.getTenantId());
        });
        
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.pxzConfigMapper.deleteById(id) > 0;
    }
    
    @Override
    public Pair<Boolean, Object> queryByInfo() {
        return Pair.of(true, queryByTenantIdFromCache(TenantContextHolder.getTenantId()));
    }
    
    @Override
    public Pair<Boolean, Object> save(PxzConfigQuery pxzConfigQuery) {
        PxzConfig pxzConfig = queryByTenantIdFromDB(TenantContextHolder.getTenantId());
        if (!Objects.isNull(pxzConfig)) {
            return Pair.of(true, null);
        }
        
        PxzConfig config = PxzConfig.builder().tenantId(TenantContextHolder.getTenantId())
                .aesKey(pxzConfigQuery.getAesKey()).merchantCode(pxzConfigQuery.getMerchantCode())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        insert(config);
        return Pair.of(true, null);
    }
    
    @Override
    public Pair<Boolean, Object> modify(PxzConfigQuery pxzConfigQuery) {
        PxzConfig pxzConfig = queryByTenantIdFromDB(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig)) {
            return Pair.of(false, "未能查询到相关数据，无法修改");
        }
        
        pxzConfig.setAesKey(pxzConfigQuery.getAesKey());
        pxzConfig.setMerchantCode(pxzConfigQuery.getMerchantCode());
        update(pxzConfig);
        return Pair.of(true, null);
    }

    @Override
    public Pair<Boolean, Object> check() {
        PxzConfig pxzConfig = queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig)) {
            return Pair.of(true, Boolean.FALSE);
        }

        if (StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode()) || pxzConfig.getAesKey().length() != 16 || pxzConfig.getMerchantCode().length() != 7) {
            return Pair.of(true, Boolean.FALSE);
        }

        return Pair.of(true, Boolean.TRUE);
    }
}
