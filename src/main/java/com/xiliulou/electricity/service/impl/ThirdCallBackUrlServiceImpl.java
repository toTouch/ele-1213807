package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ThirdCallBackUrl;
import com.xiliulou.electricity.mapper.ThirdCallBackUrlMapper;
import com.xiliulou.electricity.service.ThirdCallBackUrlService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.web.query.ThirdCallBackUrlRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (ThirdCallBackUrl)表服务实现类
 *
 * @author makejava
 * @since 2021-11-10 15:25:19
 */
@Service("thirdCallBackUrlService")
@Slf4j
public class ThirdCallBackUrlServiceImpl implements ThirdCallBackUrlService {
    @Resource
    private ThirdCallBackUrlMapper thirdCallBackUrlMapper;

    @Autowired
    RedisService redisService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ThirdCallBackUrl queryByIdFromDB(Integer id) {
        return this.thirdCallBackUrlMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ThirdCallBackUrl queryByTenantIdFromCache(Integer id) {
        ThirdCallBackUrl cache = redisService.getWithHash(ElectricityCabinetConstant.CACHE_THIRD_CALL_BACK_URL + id, ThirdCallBackUrl.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }

        ThirdCallBackUrl thirdCallBackUrl = queryByTenantIdFromDB(id);
        if (Objects.isNull(thirdCallBackUrl)) {
            return null;
        }

        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_THIRD_CALL_BACK_URL + id, thirdCallBackUrl);
        return thirdCallBackUrl;
    }

    @Override
    public ThirdCallBackUrl queryByTenantIdFromDB(Integer id) {
        return this.thirdCallBackUrlMapper.queryByTenantId(id);
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<ThirdCallBackUrl> queryAllByLimit(int offset, int limit) {
        return this.thirdCallBackUrlMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param thirdCallBackUrl 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ThirdCallBackUrl insert(ThirdCallBackUrl thirdCallBackUrl) {
        this.thirdCallBackUrlMapper.insertOne(thirdCallBackUrl);
        return thirdCallBackUrl;
    }

    /**
     * 修改数据
     *
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> update(ThirdCallBackUrlRequest thirdCallBackUrlRequest) {
        ThirdCallBackUrl thirdCallBackUrl = queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(thirdCallBackUrl)) {
            return Pair.of(false, "租户没有添加回调地址，无法更新");
        }

        thirdCallBackUrl.setExchangeUrl(thirdCallBackUrlRequest.getExchangeUrl());
        thirdCallBackUrl.setRentUrl(thirdCallBackUrlRequest.getRentUrl());
        thirdCallBackUrl.setReturnUrl(thirdCallBackUrlRequest.getReturnUrl());
        thirdCallBackUrl.setUpdateTime(System.currentTimeMillis());

        update(thirdCallBackUrl);
        return Pair.of(true, null);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ThirdCallBackUrl thirdCallBackUrl) {
        int update = this.thirdCallBackUrlMapper.update(thirdCallBackUrl);
        if (update > 0) {
            redisService.delete(ElectricityCabinetConstant.CACHE_THIRD_CALL_BACK_URL + thirdCallBackUrl.getTenantId());
        }
        return update;

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
        return this.thirdCallBackUrlMapper.deleteById(id) > 0;
    }

    @Override
    public Pair<Boolean, Object> queryThirdCallBackByTenantId() {
        ThirdCallBackUrl thirdCallBackUrl = queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        return Pair.of(true, thirdCallBackUrl);
    }

    @Override
    public Pair<Boolean, Object> save(ThirdCallBackUrlRequest thirdCallBackUrlRequest) {
        ThirdCallBackUrl thirdCallBackUrl = queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.nonNull(thirdCallBackUrl)) {
            return Pair.of(true, null);
        }

        if (!redisService.setNx(ElectricityCabinetConstant.CACHE_TENANT_ID_OPERATE + TenantContextHolder.getTenantId(), "1", 2000L, false)) {
            return Pair.of(false, "操作频繁");
        }

        ThirdCallBackUrl url = ThirdCallBackUrl.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .exchangeUrl(thirdCallBackUrlRequest.getExchangeUrl())
                .rentUrl(thirdCallBackUrlRequest.getRentUrl())
                .returnUrl(thirdCallBackUrlRequest.getReturnUrl())
                .build();
        insert(url);
        return Pair.of(true, null);
    }
}
