package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.EleChargeConfigCalcDetailDto;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.EleChargeConfigMapper;
import com.xiliulou.electricity.query.ChargeConfigListQuery;
import com.xiliulou.electricity.query.ChargeConfigQuery;
import com.xiliulou.electricity.service.EleChargeConfigService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.ChargeConfigVo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (EleChargeConfig)表服务实现类
 *
 * @author makejava
 * @since 2023-07-18 10:21:40
 */
@Service("eleChargeConfigService")
@Slf4j
public class EleChargeConfigServiceImpl implements EleChargeConfigService {
    @Resource
    private EleChargeConfigMapper eleChargeConfigMapper;

    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    StoreService storeService;

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    RedisService redisService;

    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("H");

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleChargeConfig queryByIdFromDb(Long id) {
        return this.eleChargeConfigMapper.queryById(id);
    }

    @Override
    public EleChargeConfig queryFromDb(Long franchiseeId, Long storeId, Long eid, Integer type) {
        return this.eleChargeConfigMapper.queryByCondition(franchiseeId, storeId, eid, type);
    }

    public Integer queryExistsName(String name, Integer tenantId) {
        return this.eleChargeConfigMapper.queryExistsName(name, tenantId);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleChargeConfig queryByIdFromCache(Long id) {
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
    public List<EleChargeConfig> queryAllByLimit(int offset, int limit) {
        return this.eleChargeConfigMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param eleChargeConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleChargeConfig insert(EleChargeConfig eleChargeConfig) {
        this.eleChargeConfigMapper.insertOne(eleChargeConfig);
        return eleChargeConfig;
    }

    /**
     * 修改数据
     *
     * @param eleChargeConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleChargeConfig eleChargeConfig, EleChargeConfig originalConfig) {
        return DbUtils.dbOperateSuccessThenHandleCache(this.eleChargeConfigMapper.update(eleChargeConfig), id -> {
            delConfigCache(originalConfig);
        });

    }

    private void delConfigCache(EleChargeConfig originalConfig) {
        switch (originalConfig.getType()) {
            case EleChargeConfig.TYPE_ALL_FRANCHISEE:
                redisService.delete(generateCacheKey(originalConfig.getType(), originalConfig.getId()));
                break;
            case EleChargeConfig.TYPE_ALL_STORE:
                redisService.delete(generateCacheKey(originalConfig.getType(), originalConfig.getFranchiseeId()));
                break;
            case EleChargeConfig.TYPE_ALL_CABINET:
                redisService.delete(generateCacheKey(originalConfig.getType(), originalConfig.getStoreId()));
                break;
            case EleChargeConfig.TYPE_SINGLE_CABINET:
                redisService.delete(generateCacheKey(originalConfig.getType(), originalConfig.getEid()));
                break;

        }
    }

    public String generateCacheKey(Integer configType, Long id) {
        return CacheConstant.CACHE_CHARGE_POWER_CONFIG + configType + ":" + id;
    }

    public String generateNoneCacheKey(Integer configType, Long id) {
        return CacheConstant.CACHE_CHARGE_POWER_CONFIG_NONE + configType + ":" + id;
    }

    /**
     * 通过主键删除数据
     *
     * @param id     主键
     * @param config
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id, EleChargeConfig config) {
        return DbUtils.dbOperateSuccessThenHandleCache(this.eleChargeConfigMapper.deleteById(id), result -> {
            delConfigCache(config);
        }) > 0;
    }

    @Override
    public Pair<Boolean, Object> queryList(ChargeConfigListQuery chargeConfigListQuery) {
        List<EleChargeConfig> list = this.eleChargeConfigMapper.queryList(chargeConfigListQuery);
        if (!DataUtil.collectionIsUsable(list)) {
            return Pair.of(true, null);
        }

        return Pair.of(true, list.parallelStream().map(e -> {
            ChargeConfigVo configVo = new ChargeConfigVo();
            configVo.setName(e.getName());
            configVo.setId(e.getId());
            configVo.setJsonRule(e.getJsonRule());
            configVo.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(e.getFranchiseeId())).orElse(new Franchisee()).getName());
            configVo.setStoreName(Optional.ofNullable(storeService.queryByIdFromCache(e.getStoreId())).orElse(new Store()).getName());
            configVo.setElectricityName(Optional.ofNullable(electricityCabinetService.queryByIdFromCache(e.getEid().intValue())).orElse(new ElectricityCabinet()).getName());
            configVo.setFranchiseeId(e.getFranchiseeId());
            configVo.setStoreId(e.getStoreId());
            configVo.setCreateTime(e.getCreateTime());
            configVo.setType(e.getType());
            return configVo;
        }).collect(Collectors.toList()));
    }

    @Override
    public Pair<Boolean, Object> queryListCount(ChargeConfigListQuery chargeConfigListQuery) {
        Integer count = this.eleChargeConfigMapper.queryListCount(chargeConfigListQuery);
    }

    @Override
    public Pair<Boolean, Object> saveConfig(ChargeConfigQuery chargeConfigQuery) {
        if (!redisService.setNx(CacheConstant.CACHE_CHARGE_CONFIG_OPERATE_LIMIT + TenantContextHolder.getTenantId(), "1", TimeUnit.SECONDS.toMillis(3), false)) {
            return Pair.of(false, "频繁调用");
        }

        try {
            if (!checkParamsIllegal(chargeConfigQuery)) {
                return Pair.of(false, "参数不合法");
            }

            //处理价格类型
            Integer configType = checkConfigBelongType(chargeConfigQuery);

            Integer existsName = queryExistsName(chargeConfigQuery.getName(), TenantContextHolder.getTenantId());
            if (Objects.nonNull(existsName)) {
                return Pair.of(false, "电费名称不可以重复");
            }

            if (checkConfigTypeExists(configType, chargeConfigQuery, TenantContextHolder.getTenantId(), null)) {
                return Pair.of(false, "不能重复创建同种类型的规则");
            }

            //处理价格详情
            Pair<Boolean, String> calcDetailResult = handleChargeConfigDetail(chargeConfigQuery.getJsonRule());
            if (!calcDetailResult.getLeft()) {
                return Pair.of(false, calcDetailResult.getRight());
            }

            EleChargeConfig config = new EleChargeConfig();
            config.setName(chargeConfigQuery.getName());
            config.setFranchiseeId(Optional.ofNullable(chargeConfigQuery.getFranchiseeId()).orElse(EleChargeConfig.DEFAULT_ALL));
            config.setStoreId(Optional.ofNullable(chargeConfigQuery.getStoreId()).orElse(EleChargeConfig.DEFAULT_ALL));
            config.setEid(Optional.ofNullable(chargeConfigQuery.getEid()).orElse(EleChargeConfig.DEFAULT_ALL));
            config.setTenantId(TenantContextHolder.getTenantId());
            config.setType(configType);
            config.setCreateTime(System.currentTimeMillis());
            config.setUpdateTime(System.currentTimeMillis());
            config.setJsonRule(chargeConfigQuery.getJsonRule());
            insert(config);
        } finally {
            redisService.delete(CacheConstant.CACHE_CHARGE_CONFIG_OPERATE_LIMIT + TenantContextHolder.getTenantId());
        }
        return Pair.of(true, null);
    }

    private boolean checkConfigTypeExists(Integer configType, ChargeConfigQuery chargeConfigQuery, Integer tenantId, Long chargeId) {
        switch (configType) {
            case EleChargeConfig.TYPE_ALL_FRANCHISEE:
                EleChargeConfig config = queryByTenantIdFromDb(tenantId);
                if (Objects.nonNull(config)) {
                    //这里排除自己
                    return !Objects.nonNull(chargeId) || !Objects.equals(config.getId(), chargeId);
                }
                break;
            case EleChargeConfig.TYPE_ALL_STORE:
                EleChargeConfig franchiseeConfig = queryFromDb(chargeConfigQuery.getFranchiseeId(), null, null, EleChargeConfig.TYPE_ALL_STORE);
                if (Objects.nonNull(franchiseeConfig)) {
                    return !Objects.nonNull(chargeId) || !Objects.equals(franchiseeConfig.getId(), chargeId);
                }
                break;
            case EleChargeConfig.TYPE_ALL_CABINET:
                EleChargeConfig storeConfig = queryFromDb(chargeConfigQuery.getFranchiseeId(), chargeConfigQuery.getStoreId(), null, EleChargeConfig.TYPE_ALL_CABINET);
                if (Objects.nonNull(storeConfig)) {
                    return !Objects.nonNull(chargeId) || !Objects.equals(storeConfig.getId(), chargeId);
                }
                break;
            case EleChargeConfig.TYPE_SINGLE_CABINET:
                EleChargeConfig cabinetConfig = queryFromDb(chargeConfigQuery.getFranchiseeId(), chargeConfigQuery.getStoreId(), chargeConfigQuery.getEid(), EleChargeConfig.TYPE_SINGLE_CABINET);
                if (Objects.nonNull(cabinetConfig)) {
                    return !Objects.nonNull(chargeId) || !Objects.equals(cabinetConfig.getId(), chargeId);
                }
                break;
            default:
                return false;
        }
        return false;
    }

    @Override
    public Pair<Boolean, Object> modifyConfig(ChargeConfigQuery chargeConfigQuery) {
        if (!redisService.setNx(CacheConstant.CACHE_CHARGE_CONFIG_OPERATE_LIMIT + TenantContextHolder.getTenantId(), "1", TimeUnit.SECONDS.toMillis(3), false)) {
            return Pair.of(false, "频繁调用");
        }

        try {
            EleChargeConfig config = queryByIdFromDb(chargeConfigQuery.getId());
            if (Objects.isNull(config) || !Objects.equals(config.getTenantId(), TenantContextHolder.getTenantId())) {
                return Pair.of(false, "查询不到相关电量配置");
            }

            if (!checkParamsIllegal(chargeConfigQuery)) {
                return Pair.of(false, "参数不合法");
            }

            if (!config.getName().equalsIgnoreCase(chargeConfigQuery.getName())) {
                Integer existsName = queryExistsName(chargeConfigQuery.getName(), TenantContextHolder.getTenantId());
                if (Objects.nonNull(existsName)) {
                    return Pair.of(false, "电费名称不可以重复");
                }
            }

            //处理价格类型
            Integer configType = checkConfigBelongType(chargeConfigQuery);
            if (checkConfigTypeExists(configType, chargeConfigQuery, TenantContextHolder.getTenantId(), null)) {
                return Pair.of(false, "不能重复创建同种类型的规则");
            }

            //处理价格详情
            Pair<Boolean, String> calcDetailResult = handleChargeConfigDetail(chargeConfigQuery.getJsonRule());
            if (!calcDetailResult.getLeft()) {
                return Pair.of(false, calcDetailResult.getRight());
            }

            EleChargeConfig updateConfig = new EleChargeConfig();
            updateConfig.setId(config.getId());
            updateConfig.setName(chargeConfigQuery.getName());
            updateConfig.setFranchiseeId(Optional.ofNullable(chargeConfigQuery.getFranchiseeId()).orElse(EleChargeConfig.DEFAULT_ALL));
            updateConfig.setStoreId(Optional.ofNullable(chargeConfigQuery.getStoreId()).orElse(EleChargeConfig.DEFAULT_ALL));
            updateConfig.setEid(Optional.ofNullable(chargeConfigQuery.getEid()).orElse(EleChargeConfig.DEFAULT_ALL));
            updateConfig.setType(configType);
            updateConfig.setUpdateTime(System.currentTimeMillis());
            updateConfig.setJsonRule(chargeConfigQuery.getJsonRule());

            update(updateConfig, config);
        } finally {
            redisService.delete(CacheConstant.CACHE_CHARGE_CONFIG_OPERATE_LIMIT + TenantContextHolder.getTenantId());
        }
        return Pair.of(true, null);
    }

    @Override
    public Pair<Boolean, Object> delConfig(Long id) {
        EleChargeConfig config = queryByIdFromDb(id);
        if (Objects.isNull(config) || !Objects.equals(config.getTenantId(), TenantContextHolder.getTenantId())) {
            return Pair.of(false, "查询不到相关电量配置");
        }

        deleteById(id, config);

        return Pair.of(true, null);
    }

    @Override
    public EleChargeConfig queryConfigByCabinetWithLayer(ElectricityCabinet electricityCabinet, Long franchiseeId) {
        //先查询柜机是否有自己的规则
        EleChargeConfig chargeConfig = queryConfigByEidFromCache(franchiseeId, electricityCabinet.getStoreId(), Long.valueOf(electricityCabinet.getId()));
        if (Objects.nonNull(chargeConfig)) {
            return chargeConfig;
        }

        //检查门店
        EleChargeConfig storeConfig = queryConfigByStoreIdFromCache(franchiseeId, electricityCabinet.getStoreId());
        if (Objects.nonNull(storeConfig)) {
            return storeConfig;
        }

        EleChargeConfig franchiseeConfig = queryConfigByFranchiseeIdFromCache(franchiseeId);
        if (Objects.nonNull(franchiseeConfig)) {
            return franchiseeConfig;
        }

        EleChargeConfig tenantConfig = queryConfigByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.nonNull(tenantConfig)) {
            return tenantConfig;
        }

        return null;
    }

    @Override
    public EleChargeConfigCalcDetailDto acquireConfigTypeAndUnitPriceAccrodingTime(EleChargeConfig eleChargeConfig, Long createTime) {
        List<EleChargeConfigCalcDetailDto> eleChargeConfigCalcDetailDtos = JsonUtil.fromJsonArray(eleChargeConfig.getJsonRule(), EleChargeConfigCalcDetailDto.class);
        if (!DataUtil.collectionIsUsable(eleChargeConfigCalcDetailDtos)) {
            return null;
        }

        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(createTime), ZoneId.systemDefault());
        int hour = Integer.parseInt(localDateTime.format(FORMATTER));

        for (EleChargeConfigCalcDetailDto eleChargeConfigCalcDetailDto : eleChargeConfigCalcDetailDtos) {
            if (hour >= eleChargeConfigCalcDetailDto.getStartHour() && hour < eleChargeConfigCalcDetailDto.getEndHour()) {
                return eleChargeConfigCalcDetailDto;
            }
        }

        return null;
    }


    @Override
    public EleChargeConfig queryConfigByStoreIdFromCache(Long franchiseeId, Long storeId) {
        //没有的话就直接返还，不用再经过一次DB
        if (redisService.hasKey(generateNoneCacheKey(EleChargeConfig.TYPE_ALL_CABINET, storeId))) {
            return null;
        }

        EleChargeConfig chargeConfig = redisService.getWithHash(generateCacheKey(EleChargeConfig.TYPE_ALL_CABINET, storeId), EleChargeConfig.class);
        if (Objects.nonNull(chargeConfig)) {
            return chargeConfig;
        }

        EleChargeConfig config = queryFromDb(franchiseeId, storeId, null, EleChargeConfig.TYPE_ALL_CABINET);
        if (Objects.isNull(config)) {
            redisService.saveWithString(generateNoneCacheKey(EleChargeConfig.TYPE_ALL_CABINET, storeId), "1", TimeUnit.DAYS.toMillis(15), false);
            return null;
        }

        redisService.saveWithHash(generateCacheKey(EleChargeConfig.TYPE_ALL_CABINET, storeId), chargeConfig);
        return config;
    }

    @Override
    public EleChargeConfig queryConfigByFranchiseeIdFromCache(Long franchiseeId) {
        //没有的话就直接返还，不用再经过一次DB
        if (redisService.hasKey(generateNoneCacheKey(EleChargeConfig.TYPE_ALL_STORE, franchiseeId))) {
            return null;
        }

        EleChargeConfig chargeConfig = redisService.getWithHash(generateCacheKey(EleChargeConfig.TYPE_ALL_STORE, franchiseeId), EleChargeConfig.class);
        if (Objects.nonNull(chargeConfig)) {
            return chargeConfig;
        }

        EleChargeConfig config = queryFromDb(franchiseeId, null, null, EleChargeConfig.TYPE_ALL_STORE);
        if (Objects.isNull(config)) {
            redisService.saveWithString(generateNoneCacheKey(EleChargeConfig.TYPE_ALL_STORE, franchiseeId), "1", TimeUnit.DAYS.toMillis(15), false);
            return null;
        }

        redisService.saveWithHash(generateCacheKey(EleChargeConfig.TYPE_ALL_STORE, franchiseeId), chargeConfig);
        return config;
    }

    @Override
    public EleChargeConfig queryConfigByTenantIdFromCache(Integer tenantId) {
        //没有的话就直接返还，不用再经过一次DB
        if (redisService.hasKey(generateNoneCacheKey(EleChargeConfig.TYPE_ALL_FRANCHISEE, Long.valueOf(tenantId)))) {
            return null;
        }

        EleChargeConfig chargeConfig = redisService.getWithHash(generateCacheKey(EleChargeConfig.TYPE_ALL_FRANCHISEE, Long.valueOf(tenantId)), EleChargeConfig.class);
        if (Objects.nonNull(chargeConfig)) {
            return chargeConfig;
        }

        EleChargeConfig config = queryByTenantIdFromDb(tenantId);
        if (Objects.isNull(config)) {
            redisService.saveWithString(generateNoneCacheKey(EleChargeConfig.TYPE_ALL_FRANCHISEE, Long.valueOf(tenantId)), "1", TimeUnit.DAYS.toMillis(15), false);
            return null;
        }

        redisService.saveWithHash(generateCacheKey(EleChargeConfig.TYPE_ALL_FRANCHISEE, Long.valueOf(tenantId)), chargeConfig);
        return config;
    }

    @Override
    public EleChargeConfig queryByTenantIdFromDb(Integer tenantId) {
        return this.eleChargeConfigMapper.queryByTenantId(tenantId);
    }

    public EleChargeConfig queryConfigByEidFromCache(Long franchiseeId, Long storeId, Long eid) {
        //没有的话就直接返还，不用再经过一次DB
        if (redisService.hasKey(generateNoneCacheKey(EleChargeConfig.TYPE_SINGLE_CABINET, eid))) {
            return null;
        }

        EleChargeConfig chargeConfig = redisService.getWithHash(generateCacheKey(EleChargeConfig.TYPE_SINGLE_CABINET, eid), EleChargeConfig.class);
        if (Objects.nonNull(chargeConfig)) {
            return chargeConfig;
        }

        EleChargeConfig config = queryFromDb(franchiseeId, storeId, eid, EleChargeConfig.TYPE_SINGLE_CABINET);
        if (Objects.isNull(config)) {
            redisService.saveWithString(generateNoneCacheKey(EleChargeConfig.TYPE_SINGLE_CABINET, eid), "1", TimeUnit.DAYS.toMillis(15), false);
            return null;
        }

        redisService.saveWithHash(generateCacheKey(EleChargeConfig.TYPE_SINGLE_CABINET, eid), chargeConfig);
        return config;
    }


    private Pair<Boolean, String> handleChargeConfigDetail(String jsonRule) {
        List<EleChargeConfigCalcDetailDto> list = JsonUtil.fromJsonArray(jsonRule, EleChargeConfigCalcDetailDto.class);
        if (!DataUtil.collectionIsUsable(list)) {
            return Pair.of(false, "规则明细不合法");
        }

        Collections.sort(list);

        for (EleChargeConfigCalcDetailDto eleChargeConfigCalcDetailDto : list) {
            if (Objects.isNull(eleChargeConfigCalcDetailDto.getType())
                    || Objects.isNull(eleChargeConfigCalcDetailDto.getPrice())
                    || eleChargeConfigCalcDetailDto.getPrice() < 0.0
                    || Objects.isNull(eleChargeConfigCalcDetailDto.getStartHour())
                    || Objects.isNull(eleChargeConfigCalcDetailDto.getEndHour())) {
                return Pair.of(false, "规则明细不合法");
            }
        }

        //如果只有一个元素
        if (list.size() == 1) {
            EleChargeConfigCalcDetailDto eleChargeConfigCalcDetailDto = list.get(0);
            if (eleChargeConfigCalcDetailDto.getEndHour() - eleChargeConfigCalcDetailDto.getStartHour() != 24) {
                return Pair.of(false, "时间区间选择错误，时间区间应该是0-24");
            }
        }

        EleChargeConfigCalcDetailDto startDto = list.get(0);
        EleChargeConfigCalcDetailDto endDto = list.get(list.size() - 1);

        //先判断起始的规则的开始时间和结束规则的结束时间
        if (startDto.getStartHour() != 0 || endDto.getEndHour() != 24) {
            return Pair.of(false, "时间区间选择错误，时间区间应该是0-24");
        }

        int sumHour = 0;
        for (EleChargeConfigCalcDetailDto eleChargeConfigCalcDetailDto : list) {
            sumHour += eleChargeConfigCalcDetailDto.getEndHour() - eleChargeConfigCalcDetailDto.getStartHour();
        }

        if (sumHour != 24) {
            return Pair.of(false, "时间区间选择错误，总时间和应该为24小时");
        }

        return Pair.of(true, null);
    }

    private boolean checkParamsIllegal(ChargeConfigQuery chargeConfigQuery) {
        if (Objects.equals(chargeConfigQuery.getFranchiseeId(), EleChargeConfig.DEFAULT_ALL) && Objects.equals(chargeConfigQuery.getStoreId(), EleChargeConfig.DEFAULT_ALL)) {
            return false;
        }

        if (Objects.equals(chargeConfigQuery.getStoreId(), EleChargeConfig.DEFAULT_ALL) && Objects.equals(chargeConfigQuery.getEid(), EleChargeConfig.DEFAULT_ALL.longValue())) {
            return false;
        }

        if (Objects.equals(chargeConfigQuery.getFranchiseeId(), EleChargeConfig.DEFAULT_ALL) &&
                Objects.equals(chargeConfigQuery.getEid(), EleChargeConfig.DEFAULT_ALL)) {
            return false;
        }

        return true;
    }

    private Integer checkConfigBelongType(ChargeConfigQuery chargeConfigQuery) {
        if (Objects.equals(chargeConfigQuery.getFranchiseeId(), EleChargeConfig.DEFAULT_ALL)) {
            return EleChargeConfig.TYPE_ALL_FRANCHISEE;
        }

        if (Objects.equals(chargeConfigQuery.getStoreId(), EleChargeConfig.DEFAULT_ALL)) {
            return EleChargeConfig.TYPE_ALL_STORE;
        }


        if (Objects.equals(chargeConfigQuery.getEid(), EleChargeConfig.DEFAULT_ALL)) {
            return EleChargeConfig.TYPE_ALL_CABINET;
        }

        return EleChargeConfig.TYPE_SINGLE_CABINET;
    }

    public static void main(String[] args) {
        String jsonRule = "[{\"type\":1,\"startHour\":0,\"endHour\":20,\"price\":5},{\"type\":0,\"startHour\":20,\"endHour\":24,\"price\":1}]";
        Pair<Boolean, String> booleanStringPair = new EleChargeConfigServiceImpl().handleChargeConfigDetail(jsonRule);
        System.out.println(booleanStringPair.getLeft());
    }
}
