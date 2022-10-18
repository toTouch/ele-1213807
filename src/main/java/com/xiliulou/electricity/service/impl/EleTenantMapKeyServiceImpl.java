package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.EleTenantMapKey;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.mapper.EleTenantMapKeyMapper;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.query.EleTenantMapKeyAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.service.EleTenantMapKeyService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 用户列表(t_ele_tenant_map_key)表服务实现类
 *
 * @author makejava
 * @since 2022-08-23 15:00:00
 */
@Service("eleTenantMapKeyService")
@Slf4j
public class EleTenantMapKeyServiceImpl extends ServiceImpl<EleTenantMapKeyMapper, EleTenantMapKey> implements EleTenantMapKeyService {

    @Resource
    EleTenantMapKeyMapper eleTenantMapKeyMapper;
    @Autowired
    RedisService redisService;


    @Override
    public R edit(EleTenantMapKeyAddAndUpdate eleTenantMapKeyAddAndUpdate) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //操作频繁
        boolean result = redisService.setNx(CacheConstant.ELE_TENANT_MAP_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();


        EleTenantMapKey eleTenantMapKey = eleTenantMapKeyMapper.selectOne(new LambdaQueryWrapper<EleTenantMapKey>().eq(EleTenantMapKey::getTenantId, tenantId));
        if (Objects.isNull(eleTenantMapKey)) {
            eleTenantMapKey = new EleTenantMapKey();
            eleTenantMapKey.setMapKey(eleTenantMapKeyAddAndUpdate.getMapKey());
            eleTenantMapKey.setMapSecret(eleTenantMapKeyAddAndUpdate.getMapSecret());
            eleTenantMapKey.setCreateTime(System.currentTimeMillis());
            eleTenantMapKey.setUpdateTime(System.currentTimeMillis());
            eleTenantMapKey.setTenantId(tenantId);
            eleTenantMapKeyMapper.insert(eleTenantMapKey);
            return R.ok();
        }

        eleTenantMapKey.setMapKey(eleTenantMapKeyAddAndUpdate.getMapKey());
        eleTenantMapKey.setMapSecret(eleTenantMapKeyAddAndUpdate.getMapSecret());
        eleTenantMapKey.setCreateTime(System.currentTimeMillis());
        eleTenantMapKey.setUpdateTime(System.currentTimeMillis());
        eleTenantMapKey.setTenantId(tenantId);
        int updateResult = eleTenantMapKeyMapper.updateById(eleTenantMapKey);

        if (updateResult > 0) {
            redisService.delete(CacheConstant.CACHE_ELE_SET_MAP_KEY + tenantId);
        }
        return R.ok();
    }

    @Override
    public EleTenantMapKey queryFromCacheByTenantId(Integer tenantId) {
        EleTenantMapKey cache = redisService.getWithHash(CacheConstant.CACHE_ELE_SET_MAP_KEY + tenantId, EleTenantMapKey.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }

        EleTenantMapKey eleTenantMapKey = eleTenantMapKeyMapper.selectOne(new LambdaQueryWrapper<EleTenantMapKey>()
                .eq(EleTenantMapKey::getTenantId, tenantId));
        if (Objects.isNull(eleTenantMapKey)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_ELE_SET_MAP_KEY + tenantId, eleTenantMapKey);
        return eleTenantMapKey;
    }

}
