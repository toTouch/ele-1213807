package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.DivisionAccountConfigMapper;
import com.xiliulou.electricity.query.DivisionAccountConfigQuery;
import com.xiliulou.electricity.query.DivisionAccountConfigStatusQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.DivisionAccountConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (DivisionAccountConfig)表服务实现类
 *
 * @author zzlong
 * @since 2023-04-23 18:00:37
 */
@Service("divisionAccountConfigService")
@Slf4j
public class DivisionAccountConfigServiceImpl implements DivisionAccountConfigService {
    @Resource
    private DivisionAccountConfigMapper divisionAccountConfigMapper;
    @Autowired
    private FranchiseeService franchiseeService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ElectricityMemberCardService memberCardService;
    @Autowired
    private ElectricityCarModelService carModelService;
    @Autowired
    private DivisionAccountBatteryMembercardService divisionAccountBatteryMembercardService;
    @Autowired
    private DivisionAccountCarModelService divisionAccountCarModelService;


    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Slave
    @Override
    public DivisionAccountConfig queryByIdFromDB(Long id) {
        return this.divisionAccountConfigMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DivisionAccountConfig queryByIdFromCache(Long id) {
        DivisionAccountConfig cacheDivisionAccountConfig = redisService.getWithHash(CacheConstant.CACHE_DIVISION_ACCOUNT_CONFIG + id, DivisionAccountConfig.class);
        if (Objects.nonNull(cacheDivisionAccountConfig)) {
            return cacheDivisionAccountConfig;
        }

        DivisionAccountConfig divisionAccountConfig = this.queryByIdFromDB(id);
        if (Objects.isNull(divisionAccountConfig)) {
            return divisionAccountConfig;
        }
        redisService.saveWithHash(CacheConstant.CACHE_DIVISION_ACCOUNT_CONFIG + id, divisionAccountConfig);
        return divisionAccountConfig;
    }

    @Slave
    @Override
    public List<DivisionAccountConfigVO> selectByPage(DivisionAccountConfigQuery query) {
        List<DivisionAccountConfig> divisionAccountConfigs = this.divisionAccountConfigMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(divisionAccountConfigs)) {
            return Collections.emptyList();
        }

        return divisionAccountConfigs.parallelStream().map(item -> {
            DivisionAccountConfigVO divisionAccountConfigVO = new DivisionAccountConfigVO();
            BeanUtils.copyProperties(item, divisionAccountConfigVO);

            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            divisionAccountConfigVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");

            Store store = storeService.queryByIdFromCache(item.getStoreId());
            divisionAccountConfigVO.setStoreName(Objects.nonNull(store) ? store.getName() : "");

            divisionAccountConfigVO.setMembercardNames(assignMembercardName(item));

            return divisionAccountConfigVO;
        }).collect(Collectors.toList());
    }

    @Slave
    @Override
    public Integer selectByPageCount(DivisionAccountConfigQuery query) {
        return this.divisionAccountConfigMapper.selectByPageCount(query);
    }

    /**
     * 新增数据
     *
     * @param divisionAccountConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DivisionAccountConfig insert(DivisionAccountConfig divisionAccountConfig) {
        this.divisionAccountConfigMapper.insert(divisionAccountConfig);
        return divisionAccountConfig;
    }

    /**
     * 修改数据
     *
     * @param divisionAccountConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(DivisionAccountConfig divisionAccountConfig) {
        int update = this.divisionAccountConfigMapper.update(divisionAccountConfig);
        return DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_DIVISION_ACCOUNT_CONFIG + divisionAccountConfig.getId());
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
    public Integer deleteById(Long id) {
        int delete = this.divisionAccountConfigMapper.deleteById(id);
        return DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_DIVISION_ACCOUNT_CONFIG + id);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> delete(Long id) {
        DivisionAccountConfig divisionAccountConfig = this.queryByIdFromCache(id);
        if (Objects.isNull(divisionAccountConfig) || !Objects.equals(divisionAccountConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100480", "分帐配置不存在");
        }

        DivisionAccountConfig divisionAccountConfigUpdate = new DivisionAccountConfig();
        divisionAccountConfigUpdate.setId(id);
        divisionAccountConfigUpdate.setDelFlag(DivisionAccountConfig.DEL_DEL);
        divisionAccountConfigUpdate.setUpdateTime(System.currentTimeMillis());
        this.divisionAccountConfigMapper.update(divisionAccountConfigUpdate);

        if (Objects.equals(divisionAccountConfig.getType(), DivisionAccountConfig.TYPE_BATTERY)) {
            divisionAccountBatteryMembercardService.deleteByDivisionAccountId(id);
        }

        if (Objects.equals(divisionAccountConfig.getType(), DivisionAccountConfig.TYPE_CAR)) {
            divisionAccountCarModelService.deleteByDivisionAccountId(id);
        }

        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> modify(DivisionAccountConfigQuery divisionAccountConfigQuery) {
        DivisionAccountConfig divisionAccountConfig = this.queryByIdFromCache(divisionAccountConfigQuery.getId());
        if (Objects.isNull(divisionAccountConfig) || !Objects.equals(divisionAccountConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100480", "分帐配置不存在");
        }

        DivisionAccountConfig divisionAccountConfigUpdate = new DivisionAccountConfig();
        divisionAccountConfigUpdate.setId(divisionAccountConfig.getId());
        divisionAccountConfigUpdate.setName(divisionAccountConfigQuery.getName());
        divisionAccountConfigUpdate.setFranchiseeRate(divisionAccountConfigQuery.getFranchiseeRate());
        divisionAccountConfigUpdate.setOperatorRate(divisionAccountConfigQuery.getOperatorRate());
        divisionAccountConfigUpdate.setStoreRate(divisionAccountConfigQuery.getStoreRate());
        divisionAccountConfigUpdate.setUpdateTime(System.currentTimeMillis());

        this.update(divisionAccountConfigUpdate);
        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> updateStatus(DivisionAccountConfigStatusQuery divisionAccountConfigQuery) {
        DivisionAccountConfig divisionAccountConfig = this.queryByIdFromCache(divisionAccountConfigQuery.getId());
        if (Objects.isNull(divisionAccountConfig) || !Objects.equals(divisionAccountConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100480", "分帐配置不存在");
        }

        if (Objects.equals(divisionAccountConfig.getType(), DivisionAccountConfig.TYPE_BATTERY)) {
            //获取当前租户已启用的分帐套餐
            List<Long> enableMemberCardIds = divisionAccountBatteryMembercardService.selectByTenantId(TenantContextHolder.getTenantId());

            //获取当前分帐配置所绑定的分帐套餐
            List<Long> currentMemberCardIds = divisionAccountBatteryMembercardService.selectByDivisionAccountConfigId(divisionAccountConfig.getId());

            Collection<Long> intersection = CollectionUtils.intersection(enableMemberCardIds, currentMemberCardIds);
            if (CollectionUtils.isNotEmpty(intersection)) {
                return Triple.of(false, "", "分帐套餐不允许重复");
            }
        }

        if (Objects.equals(divisionAccountConfig.getType(), DivisionAccountConfig.TYPE_CAR)) {
            //获取租户已启用的分帐车辆型号
            List<Long> enableCarModels = divisionAccountCarModelService.selectByTenantId(TenantContextHolder.getTenantId());

            //获取当前分帐配置所绑定的分帐车辆型号
            List<Long> currentCarModels = divisionAccountCarModelService.selectByDivisionAccountConfigId(divisionAccountConfig.getId());

            Collection<Long> intersection = CollectionUtils.intersection(enableCarModels, currentCarModels);
            if (CollectionUtils.isNotEmpty(intersection)) {
                return Triple.of(false, "", "分帐车辆型号不允许重复");
            }
        }

        DivisionAccountConfig divisionAccountConfigUpdate = new DivisionAccountConfig();
        divisionAccountConfigUpdate.setId(divisionAccountConfig.getId());
        divisionAccountConfigUpdate.setStatus(divisionAccountConfigQuery.getStatus());
        divisionAccountConfigUpdate.setUpdateTime(System.currentTimeMillis());

        this.update(divisionAccountConfigUpdate);
        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(DivisionAccountConfigQuery query) {
        Franchisee franchisee = franchiseeService.queryByIdFromCache(query.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }

        if (Objects.equals(query.getHierarchy(), DivisionAccountConfig.HIERARCHY_THREE)) {
            Store store = storeService.queryByIdFromCache(query.getStoreId());
            if (Objects.isNull(store)) {
                return Triple.of(false, "ELECTRICITY.0018", "门店不存在");
            }
        }

        //校验套餐
        Triple<Boolean, String, Object> verifyBatteryMemberCardResult = verifyBatteryMemberCard(query);
        if (Boolean.FALSE.equals(verifyBatteryMemberCardResult.getLeft())) {
            return verifyBatteryMemberCardResult;
        }

        //校验车辆型号
        Triple<Boolean, String, Object> verifyCarModelResult = verifyCarModel(query);
        if (Boolean.FALSE.equals(verifyCarModelResult.getLeft())) {
            return verifyCarModelResult;
        }

        DivisionAccountConfig divisionAccountConfig = buildDivisionAccountConfig(query);
        DivisionAccountConfig accountConfig = this.insert(divisionAccountConfig);

        if (Objects.equals(query.getType(), DivisionAccountConfig.TYPE_BATTERY)) {
            List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercardList = buildDivisionAccountBatteryMembercardList(query, accountConfig);
            divisionAccountBatteryMembercardService.batchInsert(divisionAccountBatteryMembercardList);
        }

        if (Objects.equals(query.getType(), DivisionAccountConfig.TYPE_CAR)) {
            List<DivisionAccountCarModel> divisionAccountCarModelList = buildDivisionAccountCarModelList(query, accountConfig);
            divisionAccountCarModelService.batchInsert(divisionAccountCarModelList);
        }

        return Triple.of(true, null, null);
    }

    @Slave
    @Override
    public Triple<Boolean, String, Object> selectInfoById(Long id) {
        DivisionAccountConfig divisionAccountConfig = this.queryByIdFromCache(id);
        if (Objects.isNull(divisionAccountConfig) || !Objects.equals(divisionAccountConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100480", "分帐配置不存在");
        }

        DivisionAccountConfigVO divisionAccountConfigVO = new DivisionAccountConfigVO();
        BeanUtils.copyProperties(divisionAccountConfig, divisionAccountConfigVO);

        Franchisee franchisee = franchiseeService.queryByIdFromCache(divisionAccountConfig.getId());
        divisionAccountConfigVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");

        Store store = storeService.queryByIdFromCache(divisionAccountConfig.getStoreId());
        divisionAccountConfigVO.setStoreName(Objects.nonNull(store) ? store.getName() : "");

        if (Objects.equals(divisionAccountConfig.getType(), DivisionAccountConfig.TYPE_BATTERY)) {
            List<Long> memberCardIds = divisionAccountBatteryMembercardService.selectByDivisionAccountConfigId(divisionAccountConfig.getId());
            if (CollectionUtils.isEmpty(memberCardIds)) {
                return Triple.of(true, null, divisionAccountConfig);
            }

            List<ElectricityMemberCard> memberCards = new ArrayList<>();
            memberCardIds.forEach(item -> memberCards.add(memberCardService.queryByCache(item.intValue())));

            divisionAccountConfigVO.setMemberCards(memberCards);
        }

        if (Objects.equals(divisionAccountConfig.getType(), DivisionAccountConfig.TYPE_CAR)) {
            List<Long> carModelIds = divisionAccountCarModelService.selectByDivisionAccountConfigId(divisionAccountConfig.getId());
            if (CollectionUtils.isEmpty(carModelIds)) {
                return Triple.of(true, null, divisionAccountConfig);
            }

            List<ElectricityCarModel> carModels = new ArrayList<>();
            carModelIds.forEach(item -> carModels.add(carModelService.queryByIdFromCache(item.intValue())));

            divisionAccountConfigVO.setCarModels(carModels);
        }

        return Triple.of(true, null, divisionAccountConfigVO);
    }

    private List<String> assignMembercardName(DivisionAccountConfig item) {

        List<String> list = Lists.newArrayList();

        if (Objects.equals(item.getType(), DivisionAccountConfig.TYPE_BATTERY)) {
            List<Long> membercardIds = divisionAccountBatteryMembercardService.selectByDivisionAccountConfigId(item.getId());
            if (CollectionUtils.isEmpty(membercardIds)) {
                return list;
            }

            membercardIds.forEach(e -> {
                ElectricityMemberCard electricityMemberCard = memberCardService.queryByCache(e.intValue());
                if (Objects.nonNull(electricityMemberCard)) {
                    list.add(electricityMemberCard.getName());
                }
            });
        }

        if (Objects.equals(item.getType(), DivisionAccountConfig.TYPE_CAR)) {
            List<Long> carModelIds = divisionAccountCarModelService.selectByDivisionAccountConfigId(item.getId());
            if (CollectionUtils.isEmpty(carModelIds)) {
                return list;
            }

            carModelIds.forEach(e -> {
                ElectricityCarModel electricityCarModel = carModelService.queryByIdFromCache(e.intValue());
                if (Objects.nonNull(electricityCarModel)) {
                    list.add(electricityCarModel.getName());
                }
            });
        }

        return list;
    }

    private Triple<Boolean, String, Object> verifyBatteryMemberCard(DivisionAccountConfigQuery query) {
        if (!Objects.equals(DivisionAccountConfig.TYPE_BATTERY, query.getType())) {
            return Triple.of(true, null, null);
        }

        //获取当前租户已启用的分帐套餐
        List<Long> enableMemberCardIds = divisionAccountBatteryMembercardService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(query.getIsAll())) {
            if (CollectionUtils.isNotEmpty(enableMemberCardIds)) {
                return Triple.of(false, "", "分帐套餐不允许重复");
            }

            List<ElectricityMemberCard> electricityMemberCards = memberCardService.selectByFranchiseeId(query.getFranchiseeId(), TenantContextHolder.getTenantId());
            if (CollectionUtils.isEmpty(electricityMemberCards)) {
                return Triple.of(false, "100481", "加盟商没有可用换电套餐");
            }

            List<Long> membercardIds = electricityMemberCards.stream().map(item -> item.getId().longValue()).collect(Collectors.toList());
            query.setMembercards(membercardIds);

            return Triple.of(true, null, null);
        }

        for (Long membercard : query.getMembercards()) {
            ElectricityMemberCard electricityMemberCard = memberCardService.queryByCache(membercard.intValue());
            if (Objects.isNull(electricityMemberCard)) {
                return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
            }
        }

        Collection<Long> intersection = CollectionUtils.intersection(enableMemberCardIds, query.getMembercards());
        if (CollectionUtils.isNotEmpty(intersection)) {
            return Triple.of(false, "", "分帐套餐不允许重复");
        }

        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> verifyCarModel(DivisionAccountConfigQuery query) {
        if (!Objects.equals(DivisionAccountConfig.TYPE_CAR, query.getType())) {
            return Triple.of(true, null, null);
        }

        //获取租户已启用的分帐车辆型号
        List<Long> enableCarModels = divisionAccountCarModelService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(query.getIsAll())) {
            if (CollectionUtils.isNotEmpty(enableCarModels)) {
                return Triple.of(false, "", "分帐车辆型号不允许重复");
            }

            ElectricityCarModelQuery carModelQuery = ElectricityCarModelQuery.builder().storeId(query.getStoreId()).delFlag(ElectricityCarModel.DEL_NORMAL).build();
            List<ElectricityCarModel> electricityCarModels = carModelService.selectByQuery(carModelQuery);
            if (CollectionUtils.isEmpty(electricityCarModels)) {
                return Triple.of(false, "100482", "门店没有可用车辆型号");
            }

            List<Long> carModelIds = electricityCarModels.stream().map(item -> item.getId().longValue()).collect(Collectors.toList());
            query.setCarModels(carModelIds);

            return Triple.of(true, null, null);
        }

        for (Long carModel : query.getCarModels()) {
            ElectricityCarModel electricityCarModel = carModelService.queryByIdFromCache(carModel.intValue());
            if (Objects.isNull(electricityCarModel)) {
                return Triple.of(false, "100258", "未找到车辆型号");
            }
        }

        Collection<Long> intersection = CollectionUtils.intersection(query.getCarModels(), enableCarModels);
        if (CollectionUtils.isNotEmpty(intersection)) {
            return Triple.of(false, "", "分帐车辆型号不允许重复");
        }

        return Triple.of(true, null, null);
    }

    private DivisionAccountConfig buildDivisionAccountConfig(DivisionAccountConfigQuery query) {
        DivisionAccountConfig divisionAccountConfig = new DivisionAccountConfig();
        BeanUtils.copyProperties(query, divisionAccountConfig);
        divisionAccountConfig.setCreateTime(System.currentTimeMillis());
        divisionAccountConfig.setUpdateTime(System.currentTimeMillis());
        divisionAccountConfig.setDelFlag(DivisionAccountConfig.DEL_NORMAL);
        divisionAccountConfig.setStatus(DivisionAccountConfig.STATUS_DISABLE);
        divisionAccountConfig.setTenantId(TenantContextHolder.getTenantId());

        return divisionAccountConfig;
    }


    private List<DivisionAccountBatteryMembercard> buildDivisionAccountBatteryMembercardList(DivisionAccountConfigQuery query, DivisionAccountConfig accountConfig) {
        List<DivisionAccountBatteryMembercard> list = Lists.newArrayList();
        for (Long membercard : query.getMembercards()) {
            DivisionAccountBatteryMembercard divisionAccountBatteryMembercard = new DivisionAccountBatteryMembercard();
            divisionAccountBatteryMembercard.setBatteryMembercardId(membercard);
            divisionAccountBatteryMembercard.setDivisionAccountId(accountConfig.getId());
            divisionAccountBatteryMembercard.setTenantId(accountConfig.getTenantId());
            divisionAccountBatteryMembercard.setDelFlag(DivisionAccountBatteryMembercard.DEL_NORMAL);
            divisionAccountBatteryMembercard.setCreateTime(System.currentTimeMillis());
            divisionAccountBatteryMembercard.setUpdateTime(System.currentTimeMillis());

            list.add(divisionAccountBatteryMembercard);
        }

        return list;
    }

    private List<DivisionAccountCarModel> buildDivisionAccountCarModelList(DivisionAccountConfigQuery query, DivisionAccountConfig accountConfig) {
        List<DivisionAccountCarModel> list = Lists.newArrayList();
        for (Long carModel : query.getCarModels()) {
            DivisionAccountCarModel divisionAccountCarModel = new DivisionAccountCarModel();
            divisionAccountCarModel.setCarModelId(carModel);
            divisionAccountCarModel.setDivisionAccountId(accountConfig.getId());
            divisionAccountCarModel.setTenantId(accountConfig.getTenantId());
            divisionAccountCarModel.setDelFlag(DivisionAccountBatteryMembercard.DEL_NORMAL);
            divisionAccountCarModel.setCreateTime(System.currentTimeMillis());
            divisionAccountCarModel.setUpdateTime(System.currentTimeMillis());

            list.add(divisionAccountCarModel);
        }

        return list;
    }

}
