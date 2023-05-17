package com.xiliulou.electricity.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.EleDivisionAccountOperationRecordDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.DivisionAccountConfigMapper;
import com.xiliulou.electricity.query.DivisionAccountConfigQuery;
import com.xiliulou.electricity.query.DivisionAccountConfigStatusQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.DivisionAccountConfigRefVO;
import com.xiliulou.electricity.vo.DivisionAccountConfigVO;
import com.xiliulou.electricity.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    private DivisionAccountOperationRecordService divisionAccountOperationRecordService;

    @Slave
    @Override
    public List<SearchVo> configSearch(Long size, Long offset, String name, Integer tenantId) {
        List<SearchVo> searchVos = divisionAccountConfigMapper.configSearch(size, offset, name, tenantId);
        if (CollectionUtils.isEmpty(searchVos)) {
            return Collections.emptyList();
        }

        return searchVos;
    }

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

    @Slave
    @Override
    public Integer selectDivisionAccountConfigExit(String name, Integer tenantId) {
        return this.divisionAccountConfigMapper.selectDivisionAccountConfigExit(name, tenantId);
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
        this.update(divisionAccountConfigUpdate);

        divisionAccountBatteryMembercardService.deleteByDivisionAccountId(id);

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> modify(DivisionAccountConfigQuery divisionAccountConfigQuery) {

        DivisionAccountConfig accountConfigByName = this.divisionAccountConfigMapper.selectDivisionAccountConfigByName(divisionAccountConfigQuery.getName(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(accountConfigByName) && !Objects.equals(accountConfigByName.getId(), divisionAccountConfigQuery.getId())) {
            return Triple.of(false, "", "分帐配置名称已存在");
        }

        DivisionAccountConfig divisionAccountConfig = this.queryByIdFromCache(divisionAccountConfigQuery.getId());
        if (Objects.isNull(divisionAccountConfig) || !Objects.equals(divisionAccountConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100480", "分帐配置不存在");
        }

        //校验
        Triple<Boolean, String, Object> verifyBatteryMembercardResult = verifyBatteryMembercardParams(divisionAccountConfigQuery);
        if (Boolean.FALSE.equals(verifyBatteryMembercardResult.getLeft())) {
            return verifyBatteryMembercardResult;
        }

        Triple<Boolean, String, Object> verifyCarModelResult = verifyCarModelParams(divisionAccountConfigQuery);
        if (Boolean.FALSE.equals(verifyCarModelResult.getLeft())) {
            return verifyCarModelResult;
        }

        //删除原来的配置
        divisionAccountBatteryMembercardService.deleteByDivisionAccountId(divisionAccountConfigQuery.getId());

        List<EleDivisionAccountOperationRecordDTO> divisionAccountOperationRecordList =Lists.newArrayList();

        //保存
        if (Objects.equals(divisionAccountConfigQuery.getType(), DivisionAccountConfig.TYPE_BATTERY)) {
            List<DivisionAccountBatteryMembercard> divisionAccountRefIdList = buildDivisionAccountBatteryMembercardList(divisionAccountConfigQuery, divisionAccountConfig);
            divisionAccountBatteryMembercardService.batchInsert(divisionAccountRefIdList);
            divisionAccountOperationRecordList = buildDivisionAccountOperationRecordMembercardList(divisionAccountConfigQuery);
        }

        if (Objects.equals(divisionAccountConfigQuery.getType(), DivisionAccountConfig.TYPE_CAR)) {
            List<DivisionAccountBatteryMembercard> divisionAccountRefIdList = buildDivisionAccountCarModelList(divisionAccountConfigQuery, divisionAccountConfig);
            divisionAccountBatteryMembercardService.batchInsert(divisionAccountRefIdList);
            divisionAccountOperationRecordList = bulidEleDivisionAccountOperationRecordList(divisionAccountConfigQuery);
        }

        DivisionAccountConfig divisionAccountConfigUpdate = new DivisionAccountConfig();
        divisionAccountConfigUpdate.setId(divisionAccountConfig.getId());
        divisionAccountConfigUpdate.setName(divisionAccountConfigQuery.getName());
        divisionAccountConfigUpdate.setFranchiseeRate(divisionAccountConfigQuery.getFranchiseeRate());
        divisionAccountConfigUpdate.setOperatorRate(divisionAccountConfigQuery.getOperatorRate());
        divisionAccountConfigUpdate.setStoreRate(divisionAccountConfigQuery.getStoreRate());
        divisionAccountConfigUpdate.setOperatorRateOther(divisionAccountConfigQuery.getOperatorRateOther());
        divisionAccountConfigUpdate.setFranchiseeRateOther(divisionAccountConfigQuery.getFranchiseeRateOther());
        divisionAccountConfigUpdate.setUpdateTime(System.currentTimeMillis());

        this.update(divisionAccountConfigUpdate);

        DivisionAccountOperationRecord divisionAccountOperationRecord = new DivisionAccountOperationRecord();
        divisionAccountOperationRecord.setName(divisionAccountConfigQuery.getName())
                .setDivisionAccountId(divisionAccountConfigQuery.getId().intValue())
                .setCabinetOperatorRate(divisionAccountConfigQuery.getOperatorRate())
                .setCabinetFranchiseeRate(divisionAccountConfigQuery.getFranchiseeRate())
                .setCabinetStoreRate(divisionAccountConfigQuery.getStoreRate())
                .setNonCabOperatorRate(divisionAccountConfigQuery.getOperatorRateOther())
                .setNonCabFranchiseeRate(divisionAccountConfigQuery.getFranchiseeRateOther())
                .setUid(SecurityUtils.getUid())
                .setTenantId(divisionAccountConfig.getTenantId())
                .setCreateTime(System.currentTimeMillis())
                .setUpdateTime(System.currentTimeMillis())
                .setAccountMemberCard(JsonUtil.toJson(divisionAccountOperationRecordList));
        divisionAccountOperationRecordService.insert(divisionAccountOperationRecord);
        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> updateStatus(DivisionAccountConfigStatusQuery divisionAccountConfigQuery) {
        DivisionAccountConfig divisionAccountConfig = this.queryByIdFromCache(divisionAccountConfigQuery.getId());
        if (Objects.isNull(divisionAccountConfig) || !Objects.equals(divisionAccountConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100480", "分帐配置不存在");
        }

        if (DivisionAccountConfig.STATUS_ENABLE.equals(divisionAccountConfigQuery.getStatus())) {
            if (DivisionAccountConfig.HIERARCHY_TWO.equals(divisionAccountConfig.getHierarchy())) {
                //已启用的分帐配置
                List<DivisionAccountConfigRefVO> divisionAccountConfigRefVOS = divisionAccountConfigMapper.selectDivisionAccountConfigDetail(null, divisionAccountConfig.getType(), null, divisionAccountConfig.getFranchiseeId(), divisionAccountConfig.getTenantId());
                if (CollectionUtils.isNotEmpty(divisionAccountConfigRefVOS)) {
                    List<Long> enableRefIds = divisionAccountConfigRefVOS.stream().map(DivisionAccountConfigRefVO::getRefId).collect(Collectors.toList());

                    //当前分帐配置绑定的套餐
                    List<Long> currentRefIds = divisionAccountBatteryMembercardService.selectByDivisionAccountConfigId(divisionAccountConfig.getId());
                    if (CollectionUtils.isNotEmpty(CollectionUtils.intersection(enableRefIds, currentRefIds))) {
                        return Triple.of(false, "", "套餐分帐配置已存在");
                    }
                }
            } else {
                //已启用的分帐配置
                List<DivisionAccountConfigRefVO> divisionAccountConfigRefVOS = divisionAccountConfigMapper.selectDivisionAccountConfigDetail(null, divisionAccountConfig.getType(), divisionAccountConfig.getStoreId(), divisionAccountConfig.getFranchiseeId(), divisionAccountConfig.getTenantId());
                if (CollectionUtils.isNotEmpty(divisionAccountConfigRefVOS)) {
                    List<Long> enableRefIds = divisionAccountConfigRefVOS.stream().map(DivisionAccountConfigRefVO::getRefId).collect(Collectors.toList());

                    //当前分帐配置绑定的套餐
                    List<Long> currentRefIds = divisionAccountBatteryMembercardService.selectByDivisionAccountConfigId(divisionAccountConfig.getId());
                    if (CollectionUtils.isNotEmpty(CollectionUtils.intersection(enableRefIds, currentRefIds))) {
                        return Triple.of(false, "", "套餐分帐配置已存在");
                    }
                }
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
        query.setTenantId(TenantContextHolder.getTenantId());
        Integer exitResult = this.divisionAccountConfigMapper.selectDivisionAccountConfigExit(query.getName(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(exitResult)) {
            return Triple.of(false, "", "分帐配置名称已存在");
        }

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

        Triple<Boolean, String, Object> verifyBatteryMembercardResult = verifyBatteryMembercardParams(query);
        if (Boolean.FALSE.equals(verifyBatteryMembercardResult.getLeft())) {
            return verifyBatteryMembercardResult;
        }

        Triple<Boolean, String, Object> verifyCarModelResult = verifyCarModelParams(query);
        if (Boolean.FALSE.equals(verifyCarModelResult.getLeft())) {
            return verifyCarModelResult;
        }

        DivisionAccountConfig divisionAccountConfig = buildDivisionAccountConfig(query);
        DivisionAccountConfig accountConfig = this.insert(divisionAccountConfig);

        if (Objects.equals(query.getType(), DivisionAccountConfig.TYPE_BATTERY)) {
            List<DivisionAccountBatteryMembercard> divisionAccountRefIdList = buildDivisionAccountBatteryMembercardList(query, accountConfig);
            divisionAccountBatteryMembercardService.batchInsert(divisionAccountRefIdList);
        }

        if (Objects.equals(query.getType(), DivisionAccountConfig.TYPE_CAR)) {
            List<DivisionAccountBatteryMembercard> divisionAccountRefIdList = buildDivisionAccountCarModelList(query, accountConfig);
            divisionAccountBatteryMembercardService.batchInsert(divisionAccountRefIdList);
        }

        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> verifyBatteryMembercardParams(DivisionAccountConfigQuery query) {
        if (CollectionUtils.isEmpty(query.getMembercards())) {
            return Triple.of(true, null, null);
        }

        for (Long membercard : query.getMembercards()) {
            ElectricityMemberCard electricityMemberCard = memberCardService.queryByCache(membercard.intValue());
            if (Objects.isNull(electricityMemberCard)) {
                return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
            }
        }

        //已启用的分帐配置
        List<DivisionAccountConfigRefVO> divisionAccountConfigRefVOS = divisionAccountConfigMapper.selectDivisionAccountConfigDetail(null, query.getType(), null, query.getFranchiseeId(), TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(divisionAccountConfigRefVOS)) {
            return Triple.of(true, null, null);
        }

        //已启用套餐
        List<Long> enableRefIds = divisionAccountConfigRefVOS.stream().map(DivisionAccountConfigRefVO::getRefId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(CollectionUtils.intersection(enableRefIds, query.getMembercards()))) {
            return Triple.of(false, "", "套餐分帐配置已存在");
        }

        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> verifyCarModelParams(DivisionAccountConfigQuery query) {
        if (CollectionUtils.isEmpty(query.getCarModels())) {
            return Triple.of(true, null, null);
        }

        List<Store> stores = storeService.selectByFranchiseeId(query.getFranchiseeId());
        if (CollectionUtils.isEmpty(stores)) {
            return Triple.of(false, "", "加盟商下没有可用门店");
        }

        List<Long> storeIds = stores.stream().map(Store::getId).collect(Collectors.toList());
        List<Long> carModelIds = carModelService.selectByStoreIds(storeIds);
        if (CollectionUtils.isEmpty(carModelIds)) {
            return Triple.of(false, "", "加盟商所属门店下没有可用车辆型号");
        }

        for (Long carModelId : carModelIds) {
            ElectricityCarModel electricityCarModel = carModelService.queryByIdFromCache(carModelId.intValue());
            if (Objects.isNull(electricityCarModel)) {
                return Triple.of(false, "", "车辆型号不存在");
            }
        }

        //已启用的分帐配置
        List<DivisionAccountConfigRefVO> divisionAccountConfigRefVOS = divisionAccountConfigMapper.selectDivisionAccountConfigDetail(null, query.getType(), null, query.getFranchiseeId(), TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(divisionAccountConfigRefVOS)) {
            return Triple.of(true, null, null);
        }

        //已启用套餐
        List<Long> enableRefIds = divisionAccountConfigRefVOS.stream().map(DivisionAccountConfigRefVO::getRefId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(CollectionUtils.intersection(enableRefIds, query.getCarModels()))) {
            return Triple.of(false, "", "车辆型号分帐配置已存在");
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

        Franchisee franchisee = franchiseeService.queryByIdFromCache(divisionAccountConfig.getFranchiseeId());
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

            divisionAccountConfigVO.setMemberCardList(memberCards);
        }

        if (Objects.equals(divisionAccountConfig.getType(), DivisionAccountConfig.TYPE_CAR)) {
            List<Long> carModelIds = divisionAccountBatteryMembercardService.selectByDivisionAccountConfigId(divisionAccountConfig.getId());
            if (CollectionUtils.isEmpty(carModelIds)) {
                return Triple.of(true, null, divisionAccountConfig);
            }

            List<ElectricityCarModel> carModels = new ArrayList<>();
            carModelIds.forEach(item -> carModels.add(carModelService.queryByIdFromCache(item.intValue())));

            divisionAccountConfigVO.setCarModelList(carModels);
        }

        return Triple.of(true, null, divisionAccountConfigVO);
    }

    @Slave
    @Override
    public List<DivisionAccountConfigRefVO> selectDivisionAccountConfigRefInfo(DivisionAccountConfigQuery query) {
        return this.divisionAccountConfigMapper.selectDivisionAccountConfigRefInfo(query);
    }

    @Slave
    @Override
    public DivisionAccountConfigRefVO selectDivisionConfigByRefId(Long membercardId, Long storeId, Long franchinseeId, Integer tenantId) {
        return this.divisionAccountConfigMapper.selectDivisionConfigByRefId(membercardId, storeId, franchinseeId, tenantId);
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
            List<Long> carModelIds = divisionAccountBatteryMembercardService.selectByDivisionAccountConfigId(item.getId());
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
            divisionAccountBatteryMembercard.setRefId(membercard);
            divisionAccountBatteryMembercard.setDivisionAccountId(accountConfig.getId());
            divisionAccountBatteryMembercard.setTenantId(accountConfig.getTenantId());
            divisionAccountBatteryMembercard.setDelFlag(DivisionAccountBatteryMembercard.DEL_NORMAL);
            divisionAccountBatteryMembercard.setCreateTime(System.currentTimeMillis());
            divisionAccountBatteryMembercard.setUpdateTime(System.currentTimeMillis());

            list.add(divisionAccountBatteryMembercard);
        }

        return list;
    }

    private List<DivisionAccountBatteryMembercard> buildDivisionAccountCarModelList(DivisionAccountConfigQuery query, DivisionAccountConfig accountConfig) {
        List<DivisionAccountBatteryMembercard> list = Lists.newArrayList();
        for (Long carModel : query.getCarModels()) {
            DivisionAccountBatteryMembercard divisionAccountBatteryMembercard = new DivisionAccountBatteryMembercard();
            divisionAccountBatteryMembercard.setRefId(carModel);
            divisionAccountBatteryMembercard.setDivisionAccountId(accountConfig.getId());
            divisionAccountBatteryMembercard.setTenantId(accountConfig.getTenantId());
            divisionAccountBatteryMembercard.setDelFlag(DivisionAccountBatteryMembercard.DEL_NORMAL);
            divisionAccountBatteryMembercard.setCreateTime(System.currentTimeMillis());
            divisionAccountBatteryMembercard.setUpdateTime(System.currentTimeMillis());

            list.add(divisionAccountBatteryMembercard);
        }

        return list;
    }

    private List<EleDivisionAccountOperationRecordDTO> bulidEleDivisionAccountOperationRecordList(DivisionAccountConfigQuery query) {
        List<EleDivisionAccountOperationRecordDTO> list = Lists.newArrayList();
        for (Long carModel : query.getCarModels()) {
            EleDivisionAccountOperationRecordDTO eleDivisionAccountOperationRecordDTO = new EleDivisionAccountOperationRecordDTO();

            ElectricityCarModel electricityCarModel = carModelService.queryByIdFromCache(carModel.intValue());
            eleDivisionAccountOperationRecordDTO.setId(carModel.intValue());
            eleDivisionAccountOperationRecordDTO.setName(Objects.nonNull(electricityCarModel)?electricityCarModel.getName():"");

            list.add(eleDivisionAccountOperationRecordDTO);
        }
        return list;
    }

    private List<EleDivisionAccountOperationRecordDTO> buildDivisionAccountOperationRecordMembercardList(DivisionAccountConfigQuery query){
        List<EleDivisionAccountOperationRecordDTO> list = Lists.newArrayList();
        for(Long membercard : query.getMembercards()){
            EleDivisionAccountOperationRecordDTO eleDivisionAccountOperationRecordDTO = new EleDivisionAccountOperationRecordDTO();
            ElectricityMemberCard electricityMemberCard = memberCardService.queryByCache(membercard.intValue());
            eleDivisionAccountOperationRecordDTO.setId(membercard.intValue());
            if (Objects.nonNull(electricityMemberCard)){
                eleDivisionAccountOperationRecordDTO.setName(electricityMemberCard.getName());
            }
            list.add(eleDivisionAccountOperationRecordDTO);
        }
        return list;
    }

}
