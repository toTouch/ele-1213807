package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.EleDivisionAccountOperationRecordDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.mapper.DivisionAccountConfigMapper;
import com.xiliulou.electricity.query.DivisionAccountConfigQuery;
import com.xiliulou.electricity.query.DivisionAccountConfigStatusQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    @Autowired
    private CarRentalPackageService carRentalPackageService;

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

            divisionAccountConfigVO.setMembercardNames(getMemberCardNameForDA(item));

            divisionAccountConfigVO.setBatteryPackages(getMemberCardVOListByConfigIdAndType(item.getId(), DivisionAccountBatteryMembercard.TYPE_BATTERY));
            divisionAccountConfigVO.setCarRentalPackages(getMemberCardVOListByConfigIdAndType(item.getId(), DivisionAccountBatteryMembercard.TYPE_CAR_RENTAL));
            divisionAccountConfigVO.setCarWithBatteryPackages(getMemberCardVOListByConfigIdAndType(item.getId(), DivisionAccountBatteryMembercard.TYPE_CAR_BATTERY));

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

    @Deprecated
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

        //删除原来的配置
        divisionAccountBatteryMembercardService.deleteByDivisionAccountId(divisionAccountConfigQuery.getId());

        //校验
        Triple<Boolean, String, Object> verifyBatteryMembercardResult = verifyBatteryMembercardParams(divisionAccountConfigQuery);
        if (Boolean.FALSE.equals(verifyBatteryMembercardResult.getLeft())) {
//            return verifyBatteryMembercardResult;
            throw new CustomBusinessException((String) verifyBatteryMembercardResult.getRight());
        }

        Triple<Boolean, String, Object> verifyCarModelResult = verifyCarModelParams(divisionAccountConfigQuery);
        if (Boolean.FALSE.equals(verifyCarModelResult.getLeft())) {
//            return verifyCarModelResult;
            throw new CustomBusinessException((String) verifyCarModelResult.getRight());
        }


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

    /**
     * 修改分账及套餐设置
     * @param query
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> modifyDivisionAccountWithPackage(DivisionAccountConfigQuery query) {
        DivisionAccountConfig accountConfigByName = this.divisionAccountConfigMapper.selectDivisionAccountConfigByName(query.getName(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(accountConfigByName) && !Objects.equals(accountConfigByName.getId(), query.getId())) {
            return Triple.of(false, "", "分帐配置名称已存在");
        }

        DivisionAccountConfig divisionAccountConfig = this.queryByIdFromCache(query.getId());
        if (Objects.isNull(divisionAccountConfig) || !Objects.equals(divisionAccountConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100480", "分帐配置不存在");
        }

        Triple<Boolean, String, Object> verifyBatteryDivisionAccountResult = verifyBatteryDivisionAccountParams(query);
        if (Boolean.FALSE.equals(verifyBatteryDivisionAccountResult.getLeft())) {
            return verifyBatteryDivisionAccountResult;
        }

        //删除原来的配置
        divisionAccountBatteryMembercardService.deleteByDivisionAccountId(query.getId());

        List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercardList = buildNewDABatteryMembercardList(query, divisionAccountConfig);
        divisionAccountBatteryMembercardService.batchInsertMemberCards(divisionAccountBatteryMembercardList);

        List<EleDivisionAccountOperationRecordDTO> divisionAccountOperationRecordList = buildDAOperationRecordForMemberCards(query);

        DivisionAccountConfig divisionAccountConfigUpdate = new DivisionAccountConfig();
        divisionAccountConfigUpdate.setId(divisionAccountConfig.getId());
        divisionAccountConfigUpdate.setName(query.getName());
        divisionAccountConfigUpdate.setType(divisionAccountConfig.getType());
        divisionAccountConfigUpdate.setFranchiseeRate(query.getFranchiseeRate());
        divisionAccountConfigUpdate.setOperatorRate(query.getOperatorRate());
        divisionAccountConfigUpdate.setStoreRate(query.getStoreRate());
        divisionAccountConfigUpdate.setOperatorRateOther(query.getOperatorRateOther());
        divisionAccountConfigUpdate.setFranchiseeRateOther(query.getFranchiseeRateOther());
        divisionAccountConfigUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(divisionAccountConfigUpdate);

        DivisionAccountOperationRecord daOperationRecord = buildDAOperationRecord(query, divisionAccountConfig.getTenantId());
        daOperationRecord.setAccountMemberCard(JsonUtil.toJson(divisionAccountOperationRecordList));
        divisionAccountOperationRecordService.insert(daOperationRecord);

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> updateDAStatus(DivisionAccountConfigStatusQuery divisionAccountConfigQuery){
        DivisionAccountConfig divisionAccountConfig = this.queryByIdFromCache(divisionAccountConfigQuery.getId());
        if (Objects.isNull(divisionAccountConfig) || !Objects.equals(divisionAccountConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100480", "分帐配置不存在");
        }
        log.error("update DA Status flow start: request parameters = {}", JsonUtil.toJson(divisionAccountConfigQuery));
        //若选择启用的分账设置中套餐信息，在之前已启用的配置中存在，则不允许启用当前设置。
        if (DivisionAccountConfig.STATUS_ENABLE.equals(divisionAccountConfigQuery.getStatus())) {
            //1. 查询出当前加盟商下所有的已启用的分账套餐信息。
            //2. 根据分账id 查询分账套餐记录表中关联的套餐信息。
            //3. 比对1中套餐信息是否在2中的结果中存在，比较条件为套餐refId和type. 如果这两个条件均满足，则判断为存在。
            List<DivisionAccountConfigRefVO> divisionAccountConfigRefVOS = null;

            //TODO 待移除 3.0版本中查询套餐是否已经被启用时，不再区分二级或者三级分账。只要当前待启用的分账设置中已有的套餐包含在已经启用的套餐中，则禁止启用。
            /*if (DivisionAccountConfig.HIERARCHY_TWO.equals(divisionAccountConfig.getHierarchy())) {
                //已启用的二级分帐配置
                divisionAccountConfigRefVOS = divisionAccountConfigMapper.selectDivisionAccountConfigWithPackage(null,null, divisionAccountConfig.getFranchiseeId(), divisionAccountConfig.getTenantId());
            } else {
                //已启用的三级分帐配置
                divisionAccountConfigRefVOS = divisionAccountConfigMapper.selectDivisionAccountConfigWithPackage(null, divisionAccountConfig.getStoreId(), divisionAccountConfig.getFranchiseeId(), divisionAccountConfig.getTenantId());
            }*/

            //查询当前运营商下已经启用的分账配置及套餐信息
            divisionAccountConfigRefVOS = divisionAccountConfigMapper.selectDivisionAccountConfigWithPackage(null,null, divisionAccountConfig.getFranchiseeId(), divisionAccountConfig.getTenantId());

            Triple<Boolean, String, Object> checkResult = checkIsExistDAPackages(divisionAccountConfigRefVOS, divisionAccountConfig);
            if(Boolean.FALSE.equals(checkResult.getLeft())){
                return checkResult;
            }
        }

        DivisionAccountConfig divisionAccountConfigUpdate = new DivisionAccountConfig();
        divisionAccountConfigUpdate.setId(divisionAccountConfig.getId());
        divisionAccountConfigUpdate.setStatus(divisionAccountConfigQuery.getStatus());
        divisionAccountConfigUpdate.setUpdateTime(System.currentTimeMillis());

        this.update(divisionAccountConfigUpdate);
        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> checkIsExistDAPackages(List<DivisionAccountConfigRefVO> divisionAccountConfigRefVOS, DivisionAccountConfig divisionAccountConfig){
        if(CollectionUtils.isEmpty(divisionAccountConfigRefVOS)){
            return Triple.of(true, "", null);
        }

        //当前分帐配置绑定的套餐
        List<DivisionAccountBatteryMemberCardVO> divisionAccountBatteryMembercards = divisionAccountBatteryMembercardService.selectMemberCardsByDAConfigIdAndHierarchy(divisionAccountConfig.getId(), divisionAccountConfig.getHierarchy());
        //TODO 排查问题，完成后需要删除
        log.error("check the da status is enable: enable da config info = {}", JsonUtil.toJson(divisionAccountConfigRefVOS));
        log.error("current da config packages: current package info = {}", JsonUtil.toJson(divisionAccountBatteryMembercards));
        for(DivisionAccountConfigRefVO divisionAccountConfigRefVO : divisionAccountConfigRefVOS){
            for(DivisionAccountBatteryMemberCardVO divisionAccountBatteryMembercard : divisionAccountBatteryMembercards){
                //检查设置套餐是否在之前启用的设置套餐中存在
                log.error("Already enable da package info: old package info = {}, current package info = {}", JsonUtil.toJson(divisionAccountConfigRefVO), JsonUtil.toJson(divisionAccountBatteryMembercard));
                if(divisionAccountConfigRefVO.getRefId().equals(divisionAccountBatteryMembercard.getRefId())
                        && divisionAccountConfigRefVO.getPackageType().equals(divisionAccountBatteryMembercard.getType())){
                    log.error("Already used da config package: old division account config id = {}, current config id = {}", divisionAccountConfigRefVO.getId(), divisionAccountConfig.getId());
                    return Triple.of(false, "", "套餐分帐配置已存在");
                }
            }
        }

        return Triple.of(true, "", null);
    }

    @Deprecated
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

    @Deprecated
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

    /**
     * 新增分账及套餐设置
     * @param query
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> saveDivisionAccountWithPackage(DivisionAccountConfigQuery query) {

        query.setTenantId(TenantContextHolder.getTenantId());
        query.setType(BigDecimal.ZERO.intValue());

        //检查是否有选择（换电,租车,车电一体）套餐信息
        if(CollectionUtils.isEmpty(query.getBatteryPackages())
                && CollectionUtils.isEmpty(query.getCarRentalPackages())
                && CollectionUtils.isEmpty(query.getCarWithBatteryPackages())){
            return Triple.of(false, "000201", "请选择套餐信息");
        }

        //检查分账名称是否已存在
        Integer exitResult = this.divisionAccountConfigMapper.selectDivisionAccountConfigExit(query.getName(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(exitResult)) {
            return Triple.of(false, "", "分帐配置名称已存在");
        }

        //检查加盟商是否存在
        Franchisee franchisee = franchiseeService.queryByIdFromCache(query.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }

        //检查门店是否存在
        if (Objects.equals(query.getHierarchy(), DivisionAccountConfig.HIERARCHY_THREE)) {
            Store store = storeService.queryByIdFromCache(query.getStoreId());
            if (Objects.isNull(store)) {
                return Triple.of(false, "ELECTRICITY.0018", "门店不存在");
            }
        }

        //进行套餐内容验证
        Triple<Boolean, String, Object> verifyBatteryDivisionAccountResult = verifyBatteryDivisionAccountParams(query);
        if (Boolean.FALSE.equals(verifyBatteryDivisionAccountResult.getLeft())) {
            return verifyBatteryDivisionAccountResult;
        }

        //按照套餐类型来进行分类创建，新的变更会统一将选中的业务类型和套餐同时插入到套餐记录表中
        DivisionAccountConfig divisionAccountConfig = buildDivisionAccountConfig(query);
        DivisionAccountConfig accountConfig = this.insert(divisionAccountConfig);

        List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercardList = buildNewDABatteryMembercardList(query, accountConfig);
        divisionAccountBatteryMembercardService.batchInsertMemberCards(divisionAccountBatteryMembercardList);

        return Triple.of(true, null, null);
    }

    /**
     * 校验分账设置时参数信息及套餐是否可用
     * @param query
     * @return
     */
    private Triple<Boolean, String, Object> verifyBatteryDivisionAccountParams(DivisionAccountConfigQuery query){
        //检查换电套餐， 以及租车，车电一体套餐是否均可用
        //1.检查换电套餐及分账配置是否存在
        if(CollectionUtils.isNotEmpty(query.getBatteryPackages())){
            //因换电存在老的流程，所以需要检查是否存在之前设置过的套餐信息。
            Triple<Boolean, String, Object> verifyBatteryMembercardResult = verifyBatteryMembercardParams(query);
            if (Boolean.FALSE.equals(verifyBatteryMembercardResult.getLeft())) {
                log.error("旧换电" + verifyBatteryMembercardResult.getRight());
                return verifyBatteryMembercardResult;
            }

            //3.0新流程的检查方式
            for(Long memberCardId : query.getBatteryPackages()){
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(memberCardId);
                if (Objects.isNull(batteryMemberCard)) {
                    return Triple.of(false, "000202", "换电套餐不存在");
                }
            }

            Triple<Boolean, String, Object> existPackagesResult = isExistDAPackages(DivisionAccountBatteryMembercard.TYPE_BATTERY, query.getBatteryPackages(), query.getFranchiseeId(), query.getTenantId());
            if (Boolean.FALSE.equals(existPackagesResult.getLeft())) {
                log.error("换电" + existPackagesResult.getRight());
                return existPackagesResult;
            }
        }

        //2.检查租车套餐及分账配置是否存在
        if(CollectionUtils.isNotEmpty(query.getCarRentalPackages())){
            for(Long memberCardId : query.getCarRentalPackages()){
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(memberCardId);
                if (Objects.isNull(carRentalPackagePO)) {
                    return Triple.of(false, "000203", "租车套餐不存在");
                }
            }

            Triple<Boolean, String, Object> existPackagesResult = isExistDAPackages(DivisionAccountBatteryMembercard.TYPE_CAR_RENTAL, query.getCarRentalPackages(), query.getFranchiseeId(), query.getTenantId());
            if (Boolean.FALSE.equals(existPackagesResult.getLeft())) {
                log.error("租车" + existPackagesResult.getRight());
                return existPackagesResult;
            }

        }

        //3.检查车电一体套餐及分账配置是否存在
        if(CollectionUtils.isNotEmpty(query.getCarWithBatteryPackages()))
        for(Long memberCardId : query.getCarWithBatteryPackages()){
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(memberCardId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000204", "车电一体套餐不存在");
            }

            Triple<Boolean, String, Object> existPackagesResult = isExistDAPackages(DivisionAccountBatteryMembercard.TYPE_CAR_BATTERY, query.getCarWithBatteryPackages(), query.getFranchiseeId(), query.getTenantId());
            if (Boolean.FALSE.equals(existPackagesResult.getLeft())) {
                log.error("车一体" + existPackagesResult.getRight());
                return existPackagesResult;
            }
        }

        return Triple.of(true, null, null);

    }

    private Triple<Boolean, String, Object> isExistDAPackages(Integer type, List<Long> packages, Long franchiseeId, Integer tenantId){
        //检查之前是否有启用分账配置
        List<DivisionAccountConfigRefVO> divisionAccountConfigRefVOS = divisionAccountConfigMapper.selectDivisionAccountConfigWithPackage(type, null, franchiseeId, tenantId);

        log.info("division account config package: {}",  JsonUtil.toJson(divisionAccountConfigRefVOS));
        if (CollectionUtils.isEmpty(divisionAccountConfigRefVOS)) {
            return Triple.of(true, null, null);
        }

        //检查是否存在已配置过的套餐信息
        List<Long> enableRefIds = divisionAccountConfigRefVOS.stream().map(DivisionAccountConfigRefVO::getRefId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(CollectionUtils.intersection(enableRefIds, packages))) {
            return Triple.of(false, "", "套餐分帐配置已存在");
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
        List<DivisionAccountConfigRefVO> divisionAccountConfigRefVOS = divisionAccountConfigMapper.selectDivisionAccountConfigDetail(null, DivisionAccountBatteryMembercard.TYPE_BATTERY, null, query.getFranchiseeId(), TenantContextHolder.getTenantId());
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

        //divisionAccountConfigVO.setMemberCardList(getMemberCardVOListByDA(divisionAccountConfig));

        divisionAccountConfigVO.setBatteryPackages(getMemberCardVOListByConfigIdAndType(divisionAccountConfig.getId(), DivisionAccountBatteryMembercard.TYPE_BATTERY));
        divisionAccountConfigVO.setCarRentalPackages(getMemberCardVOListByConfigIdAndType(divisionAccountConfig.getId(), DivisionAccountBatteryMembercard.TYPE_CAR_RENTAL));
        divisionAccountConfigVO.setCarWithBatteryPackages(getMemberCardVOListByConfigIdAndType(divisionAccountConfig.getId(), DivisionAccountBatteryMembercard.TYPE_CAR_BATTERY));

        /*if (Objects.equals(divisionAccountConfig.getType(), DivisionAccountConfig.TYPE_BATTERY)) {
            List<Long> memberCardIds = divisionAccountBatteryMembercardService.selectByDivisionAccountConfigId(divisionAccountConfig.getId());
            if (CollectionUtils.isEmpty(memberCardIds)) {
                return Triple.of(true, null, divisionAccountConfig);
            }

            List<ElectricityMemberCard> memberCards = new ArrayList<>();
            memberCardIds.forEach(item -> memberCards.add(memberCardService.queryByCache(item.intValue())));

//            divisionAccountConfigVO.setMemberCardList(memberCards);
        }

        if (Objects.equals(divisionAccountConfig.getType(), DivisionAccountConfig.TYPE_CAR)) {
            List<Long> carModelIds = divisionAccountBatteryMembercardService.selectByDivisionAccountConfigId(divisionAccountConfig.getId());
            if (CollectionUtils.isEmpty(carModelIds)) {
                return Triple.of(true, null, divisionAccountConfig);
            }

            List<ElectricityCarModel> carModels = new ArrayList<>();
            carModelIds.forEach(item -> carModels.add(carModelService.queryByIdFromCache(item.intValue())));

            divisionAccountConfigVO.setCarModelList(carModels);
        }*/

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

    /**
     * 根据分账配置Id, 获取对应的套餐名称
     * @param item
     * @return
     */
    private List<String> getMemberCardNameForDA(DivisionAccountConfig item) {
        List<String> list = Lists.newArrayList();
        List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercards = divisionAccountBatteryMembercardService.selectMemberCardsByDAConfigId(item.getId());
        if (CollectionUtils.isEmpty(divisionAccountBatteryMembercards)) {
            return list;
        }
        for(DivisionAccountBatteryMembercard accountBatteryMembercard : divisionAccountBatteryMembercards){
            Integer type = accountBatteryMembercard.getType();
            if(DivisionAccountBatteryMembercard.TYPE_BATTERY.equals(type)){
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(accountBatteryMembercard.getRefId());
                if (Objects.nonNull(batteryMemberCard)) {
                    list.add(batteryMemberCard.getName());
                }
            }else{
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(accountBatteryMembercard.getRefId());
                if (Objects.nonNull(carRentalPackagePO)) {
                    list.add(carRentalPackagePO.getName());
                }
            }
        }
       return list;
    }

    /**
     * 根据分账配置属性和套餐类型获取对应的套餐信息
     * @param daConfigId
     * @param packageType
     * @return
     */
    @Override
    public List<BatteryMemberCardVO> getMemberCardVOListByConfigIdAndType(Long daConfigId, Integer packageType) {
        List<BatteryMemberCardVO> list = Lists.newArrayList();
        List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercards = divisionAccountBatteryMembercardService.selectMemberCardsByDAConfigIdAndType(daConfigId, packageType);
        if (CollectionUtils.isEmpty(divisionAccountBatteryMembercards)) {
            return list;
        }

        if(DivisionAccountBatteryMembercard.TYPE_BATTERY.equals(packageType)) {
            //获取换电套餐信息
            for(DivisionAccountBatteryMembercard accountBatteryMembercard : divisionAccountBatteryMembercards) {
                BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(accountBatteryMembercard.getRefId());
                batteryMemberCardVO.setId(batteryMemberCard.getId().longValue());
                batteryMemberCardVO.setName(batteryMemberCard.getName());
                list.add(batteryMemberCardVO);
            }
        }else{
            //获取租车或车电一体的套餐信息
            for(DivisionAccountBatteryMembercard accountBatteryMembercard : divisionAccountBatteryMembercards) {
                BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(accountBatteryMembercard.getRefId());
                batteryMemberCardVO.setId(carRentalPackagePO.getId());
                batteryMemberCardVO.setName(carRentalPackagePO.getName());
                list.add(batteryMemberCardVO);
            }
        }
        return list;
    }

    /**
     * 根据分账ID 获取关联的套餐信息，包括换电，租车，车店一体套餐
     * @param daConfigId
     * @return
     */
    private List<BatteryMemberCardVO> getMemberCardVOListByDA(Long daConfigId) {
        List<BatteryMemberCardVO> list = Lists.newArrayList();
        List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercards = divisionAccountBatteryMembercardService.selectMemberCardsByDAConfigId(daConfigId);
        if (CollectionUtils.isEmpty(divisionAccountBatteryMembercards)) {
            return list;
        }
        for(DivisionAccountBatteryMembercard accountBatteryMembercard : divisionAccountBatteryMembercards){
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            batteryMemberCardVO.setId(accountBatteryMembercard.getRefId());
            Integer type = accountBatteryMembercard.getType();
            if(DivisionAccountBatteryMembercard.TYPE_BATTERY.equals(type)){
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(accountBatteryMembercard.getRefId());
                if (Objects.nonNull(batteryMemberCard)) {
                    batteryMemberCardVO.setName(batteryMemberCard.getName());
                    list.add(batteryMemberCardVO);
                }
            }else{
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(accountBatteryMembercard.getRefId());
                if (Objects.nonNull(carRentalPackagePO)) {
                    batteryMemberCardVO.setName(carRentalPackagePO.getName());
                    list.add(batteryMemberCardVO);
                }
            }
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

    /**
     * 创建分账设置时对应的套餐信息
     * @param query
     * @param accountConfig
     * @return
     */
    private List<DivisionAccountBatteryMembercard> buildNewDABatteryMembercardList(DivisionAccountConfigQuery query, DivisionAccountConfig accountConfig){
        List<DivisionAccountBatteryMembercard> membercardList = Lists.newArrayList();

        List<Long> electricityPackages = query.getBatteryPackages();
        if(CollectionUtils.isNotEmpty(electricityPackages)){
            List<DivisionAccountBatteryMembercard> carElectricityPackagesCards = buildDABatteryMemberCards(electricityPackages, DivisionAccountBatteryMembercard.TYPE_BATTERY, accountConfig.getId(), accountConfig.getTenantId());
            membercardList.addAll(carElectricityPackagesCards);
        }

        List<Long> carRentalPackages = query.getCarRentalPackages();
        if(CollectionUtils.isNotEmpty(carRentalPackages)){
            List<DivisionAccountBatteryMembercard> carElectricityPackagesCards = buildDABatteryMemberCards(carRentalPackages, DivisionAccountBatteryMembercard.TYPE_CAR_RENTAL, accountConfig.getId(), accountConfig.getTenantId());
            membercardList.addAll(carElectricityPackagesCards);
        }

        List<Long> carElectricityPackages = query.getCarWithBatteryPackages();
        if(CollectionUtils.isNotEmpty(carElectricityPackages)){
            List<DivisionAccountBatteryMembercard> carElectricityPackagesCards = buildDABatteryMemberCards(carElectricityPackages, DivisionAccountBatteryMembercard.TYPE_CAR_BATTERY, accountConfig.getId(), accountConfig.getTenantId());
            membercardList.addAll(carElectricityPackagesCards);
        }

        return membercardList;
    }

    /**
     * 根据已选择套餐ids， 创建对应的分账套餐集合信息
     * @param packages
     * @param packageType
     * @param divisionAccountId
     * @param tenantId
     * @return
     */
    private List<DivisionAccountBatteryMembercard> buildDABatteryMemberCards(List<Long> packages, Integer packageType, Long divisionAccountId, Integer tenantId){
        List<DivisionAccountBatteryMembercard> membercardList = Lists.newArrayList();
        if(CollectionUtils.isNotEmpty(packages)){
            for(Long membercard : packages){
                DivisionAccountBatteryMembercard divisionAccountBatteryMembercard = new DivisionAccountBatteryMembercard();
                divisionAccountBatteryMembercard.setRefId(membercard);
                divisionAccountBatteryMembercard.setType(packageType);
                divisionAccountBatteryMembercard.setDivisionAccountId(divisionAccountId);
                divisionAccountBatteryMembercard.setTenantId(tenantId);
                divisionAccountBatteryMembercard.setDelFlag(DivisionAccountBatteryMembercard.DEL_NORMAL);
                divisionAccountBatteryMembercard.setCreateTime(System.currentTimeMillis());
                divisionAccountBatteryMembercard.setUpdateTime(System.currentTimeMillis());
                membercardList.add(divisionAccountBatteryMembercard);
            }
        }
        return membercardList;
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

    /**
     * 创建分账时套餐设置的操作记录
     * @param query
     * @return
     */
    private List<EleDivisionAccountOperationRecordDTO> buildDAOperationRecordForMemberCards(DivisionAccountConfigQuery query){
        List<EleDivisionAccountOperationRecordDTO> list = Lists.newArrayList();

        List<Long> electricityPackages = query.getBatteryPackages();
        if(CollectionUtils.isNotEmpty(electricityPackages)){
            for(Long memberCardId : electricityPackages){
                EleDivisionAccountOperationRecordDTO eleDivisionAccountOperationRecordDTO = new EleDivisionAccountOperationRecordDTO();
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(memberCardId);
                eleDivisionAccountOperationRecordDTO.setId(memberCardId.intValue());
                eleDivisionAccountOperationRecordDTO.setType(DivisionAccountBatteryMembercard.TYPE_BATTERY);
                if (Objects.nonNull(batteryMemberCard)){
                    eleDivisionAccountOperationRecordDTO.setName(batteryMemberCard.getName());
                }
                list.add(eleDivisionAccountOperationRecordDTO);
            }
        }

        List<Long> carRentalPackages = query.getCarRentalPackages();
        if(CollectionUtils.isNotEmpty(carRentalPackages)){
            for(Long memberCardId : carRentalPackages){
                EleDivisionAccountOperationRecordDTO eleDivisionAccountOperationRecordDTO = new EleDivisionAccountOperationRecordDTO();
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(memberCardId);
                eleDivisionAccountOperationRecordDTO.setId(memberCardId.intValue());
                eleDivisionAccountOperationRecordDTO.setType(DivisionAccountBatteryMembercard.TYPE_CAR_RENTAL);
                if (Objects.nonNull(carRentalPackagePO)){
                    eleDivisionAccountOperationRecordDTO.setName(carRentalPackagePO.getName());
                }
                list.add(eleDivisionAccountOperationRecordDTO);
            }
        }

        List<Long> carElectricityPackages = query.getCarWithBatteryPackages();
        if(CollectionUtils.isNotEmpty(carElectricityPackages)){
            for(Long memberCardId : carElectricityPackages){
                EleDivisionAccountOperationRecordDTO eleDivisionAccountOperationRecordDTO = new EleDivisionAccountOperationRecordDTO();
                CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(memberCardId);
                eleDivisionAccountOperationRecordDTO.setId(memberCardId.intValue());
                eleDivisionAccountOperationRecordDTO.setType(DivisionAccountBatteryMembercard.TYPE_CAR_BATTERY);
                if (Objects.nonNull(carRentalPackagePO)){
                    eleDivisionAccountOperationRecordDTO.setName(carRentalPackagePO.getName());
                }
                list.add(eleDivisionAccountOperationRecordDTO);
            }
        }

        return list;
    }

    /**
     * 创建分账设置操作记录
     * @param query
     * @return
     */
    private  DivisionAccountOperationRecord buildDAOperationRecord(DivisionAccountConfigQuery query, Integer tenantId){
        DivisionAccountOperationRecord divisionAccountOperationRecord = new DivisionAccountOperationRecord();

        divisionAccountOperationRecord.setName(query.getName())
                .setDivisionAccountId(query.getId().intValue())
                .setCabinetOperatorRate(query.getOperatorRate())
                .setCabinetFranchiseeRate(query.getFranchiseeRate())
                .setCabinetStoreRate(query.getStoreRate())
                .setNonCabOperatorRate(query.getOperatorRateOther())
                .setNonCabFranchiseeRate(query.getFranchiseeRateOther())
                .setTenantId(tenantId)
                .setUid(SecurityUtils.getUid())
                .setCreateTime(System.currentTimeMillis())
                .setUpdateTime(System.currentTimeMillis());

        return divisionAccountOperationRecord;
    }

}
