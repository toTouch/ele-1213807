package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.AppSignatureUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.TenantAppInfo;
import com.xiliulou.electricity.mapper.TenantAppInfoMapper;
import com.xiliulou.electricity.service.TenantAppInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.web.query.AppInfoQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * (TenantAppInfo)表服务实现类
 *
 * @author makejava
 * @since 2021-07-21 09:57:45
 */
@Service("tenantAppInfoService")
@Slf4j
public class TenantAppInfoServiceImpl implements TenantAppInfoService {
    @Resource
    private TenantAppInfoMapper tenantAppInfoMapper;

    @Autowired
    RedisService redisService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public TenantAppInfo queryByIdFromDB(Integer id) {
        return this.tenantAppInfoMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public TenantAppInfo queryByIdFromCache(Integer id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<TenantAppInfo> queryAllByLimit(int offset, int limit) {
        return this.tenantAppInfoMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param tenantAppInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantAppInfo insert(TenantAppInfo tenantAppInfo) {
        this.tenantAppInfoMapper.insertOne(tenantAppInfo);
        return tenantAppInfo;
    }

    /**
     * 修改数据
     *
     * @param tenantAppInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(TenantAppInfo tenantAppInfo) {
        return this.tenantAppInfoMapper.update(tenantAppInfo);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id) {
        return this.tenantAppInfoMapper.deleteById(id) > 0;
    }

    @Override
    public Triple<Boolean, String, Object> saveApp(AppInfoQuery appInfoQuery) {
        if (!appInfoQuery.getType().equals(TenantAppInfo.MT_TYPE) && !appInfoQuery.getType().equals(TenantAppInfo.CUPBOARD_TYPE)) {
            return Triple.of(false, "SYSTEM.0002", "参数不合法");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        TenantAppInfo tenantAppInfo = queryByTenantIdAndAppType(tenantId, appInfoQuery.getType());
        if (Objects.nonNull(tenantAppInfo)) {
            return Triple.of(false, "CUPBOARD.10001", "无法重复创建appId");
        }

        if (!redisService.setNx(ElectricityCabinetConstant.CACHE_APP_INFO_LIMIT + tenantId, "1", 3000L, false)) {
            return Triple.of(false, "SYSTEM.0004Te", "调用频繁，请稍后再试！");
        }

        TenantAppInfo appInfo = TenantAppInfo.builder()
                .appid(AppSignatureUtil.generateAppId(appInfoQuery.getType()))
                .appsecert(AppSignatureUtil.generateAppSecret(appInfoQuery.getType(), tenantId))
                .tenantId(tenantId)
                .createTime(System.currentTimeMillis())
                .status(TenantAppInfo.TYPE_NORMAL)
                .type(appInfoQuery.getType())
                .build();
        insert(appInfo);
        return Triple.of(true, null, null);
    }

    @Override
    public String generateAppCacheKey(Integer tenantId, String appType) {
        return ElectricityCabinetConstant.CACHE_APP_INFO_BASE + appType + "_" + tenantId;
    }

    @Override
    public String generateAppCacheKey(String appId, String appType) {
        return ElectricityCabinetConstant.CACHE_APP_INFO + appType + "_" + appId;
    }

    @Override
    public TenantAppInfo queryByTenantIdAndAppType(Integer tenantId, String appType) {
        TenantAppInfo cache = redisService.getWithHash(generateAppCacheKey(tenantId, appType), TenantAppInfo.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }

        TenantAppInfo dbResult = queryByTenantIdAndAppTypeWithDB(tenantId, appType);
        if (Objects.isNull(dbResult)) {
            return null;
        }

        redisService.saveWithHash(generateAppCacheKey(tenantId, appType), dbResult);
        return dbResult;
    }

    @Override
    public Triple<Boolean, String, Object> queryAppInfo(String appType) {
        if (StrUtil.isEmpty(appType)) {
            return Triple.of(false, "SYSTEM.0002", "不合法的参数");
        }

        if (!appType.equals(TenantAppInfo.MT_TYPE) && !appType.equals(TenantAppInfo.CUPBOARD_TYPE)) {
            return Triple.of(false, "SYSTEM.0002", "参数不合法");
        }

        return Triple.of(true, null, queryByTenantIdAndAppType(TenantContextHolder.getTenantId(), appType));
    }

    @Override
    public TenantAppInfo queryByAppId(String appId, String mtType) {
        TenantAppInfo appInfo = redisService.getWithHash(generateAppCacheKey(appId, mtType), TenantAppInfo.class);
        if (Objects.nonNull(appInfo)) {
            return appInfo;
        }

        TenantAppInfo dbResult = queryByAppIdWithDB(appId, mtType);
        if (Objects.isNull(dbResult)) {
            return null;
        }

        redisService.saveWithHash(generateAppCacheKey(appId, mtType), dbResult);
        redisService.expire(generateAppCacheKey(appId, mtType), TimeUnit.DAYS.toMillis(1), false);
        return dbResult;
    }

    private TenantAppInfo queryByAppIdWithDB(String appId, String mtType) {
        return tenantAppInfoMapper.queryByAppId(appId, mtType);
    }

    private TenantAppInfo queryByTenantIdAndAppTypeWithDB(Integer tenantId, String appType) {
        return tenantAppInfoMapper.queryByTenantIdAndType(tenantId, appType);
    }

}
