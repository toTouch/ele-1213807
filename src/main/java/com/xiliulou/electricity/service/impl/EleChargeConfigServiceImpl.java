package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.entity.EleChargeConfig;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.mapper.EleChargeConfigMapper;
import com.xiliulou.electricity.query.ChargeConfigListQuery;
import com.xiliulou.electricity.service.EleChargeConfigService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.vo.ChargeConfigVo;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
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

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleChargeConfig queryByIdFromDB(Long id) {
        return this.eleChargeConfigMapper.queryById(id);
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
    public Integer update(EleChargeConfig eleChargeConfig) {
        return this.eleChargeConfigMapper.update(eleChargeConfig);

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
        return this.eleChargeConfigMapper.deleteById(id) > 0;
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
            configVo.setFranchiseeName(franchiseeService.queryByIdFromCache(e.getFranchiseeId()).getName());
            configVo.setStoreName(Optional.ofNullable(storeService.queryByIdFromCache(e.getStoreId())).orElse(new Store()).getName());
            configVo.setCupboardName(Optional.ofNullable(electricityCabinetService.queryByIdFromCache(e.getEid())).orElse(new ElectricityCabinet()).getName());
            configVo.setCreateTime(e.getCreateTime());
            return configVo;
        }).collect(Collectors.toList()));
    }
}
