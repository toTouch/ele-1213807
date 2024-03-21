package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.FranchiseeInsuranceMapper;
import com.xiliulou.electricity.query.FranchiseeInsuranceIdsRequest;
import com.xiliulou.electricity.query.FranchiseeInsuranceAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeInsuranceQuery;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FranchiseeInsuranceVo;
import com.xiliulou.electricity.vo.insurance.FranchiseeInsuranceOrderIdsVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 换电柜保险(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@Service("franchiseeInsuranceService")
@Slf4j
public class FranchiseeInsuranceServiceImpl extends ServiceImpl<FranchiseeInsuranceMapper, FranchiseeInsurance> implements FranchiseeInsuranceService {

    @Resource
    FranchiseeInsuranceMapper franchiseeInsuranceMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;

    @Autowired
    InsuranceInstructionService insuranceInstructionService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    BatteryModelService batteryModelService;

    @Autowired
    CityService cityService;

    @Autowired
    StoreService storeService;

    @Autowired
    ElectricityCarModelService carModelService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    UserBatteryTypeService userBatteryTypeService;

    @Autowired
    CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Autowired
    private UserDataScopeService userDataScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R add(FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {

        Integer tenantId = TenantContextHolder.getTenantId();

        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeInsuranceAddAndUpdate.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(tenantId, franchisee.getTenantId())) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商！");
        }

        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) && !Objects.equals(franchiseeInsuranceAddAndUpdate.getInsuranceType(), FranchiseeInsurance.INSURANCE_TYPE_CAR)
                && StringUtils.isBlank(franchiseeInsuranceAddAndUpdate.getSimpleBatteryType()) && BigDecimal.ZERO.compareTo(franchiseeInsuranceAddAndUpdate.getForehead()) <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数！");
        }

        Integer count = baseMapper.queryCount(null, franchiseeInsuranceAddAndUpdate.getInsuranceType(), tenantId, null, franchiseeInsuranceAddAndUpdate.getName());
        if (count > 0) {
            return R.fail("100304", "保险名称已存在！");
        }

        FranchiseeInsurance franchiseeInsurance = new FranchiseeInsurance();
        BeanUtil.copyProperties(franchiseeInsuranceAddAndUpdate, franchiseeInsurance);

        //填充参数
        franchiseeInsurance.setCid(franchisee.getCid());
        franchiseeInsurance.setCreateTime(System.currentTimeMillis());
        franchiseeInsurance.setUpdateTime(System.currentTimeMillis());
        franchiseeInsurance.setStatus(FranchiseeInsurance.STATUS_UN_USABLE);
        franchiseeInsurance.setTenantId(tenantId);
        franchiseeInsurance.setDelFlag(FranchiseeInsurance.DEL_NORMAL);

        InsuranceInstruction insuranceInstruction = new InsuranceInstruction();
        insuranceInstruction.setFranchiseeId(franchiseeInsurance.getFranchiseeId());
        insuranceInstruction.setTenantId(tenantId);
        insuranceInstruction.setInstruction(franchiseeInsuranceAddAndUpdate.getInstruction());
        insuranceInstruction.setCreateTime(System.currentTimeMillis());
        insuranceInstruction.setUpdateTime(System.currentTimeMillis());

        Integer insert = baseMapper.insert(franchiseeInsurance);
        insuranceInstruction.setInsuranceId(franchiseeInsurance.getId());
        insuranceInstructionService.insert(insuranceInstruction);

        if (insert > 0) {
            return R.ok();
        }

        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R update(FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {

        Integer tenantId = TenantContextHolder.getTenantId();

        FranchiseeInsurance oldFranchiseeInsurance = this.queryByIdFromCache(franchiseeInsuranceAddAndUpdate.getId());
        if (Objects.isNull(oldFranchiseeInsurance) || !Objects.equals(tenantId, oldFranchiseeInsurance.getTenantId())) {
            return R.ok();
        }

        if (!Objects.equals(oldFranchiseeInsurance.getName() ,franchiseeInsuranceAddAndUpdate.getName() ) && baseMapper.queryCount(null, franchiseeInsuranceAddAndUpdate.getInsuranceType(), tenantId, null, franchiseeInsuranceAddAndUpdate.getName()) > 0) {
            return R.fail("100304", "保险名称已存在！");
        }

        if (BigDecimal.ZERO.compareTo(franchiseeInsuranceAddAndUpdate.getForehead()) >= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数！");
        }

        FranchiseeInsurance newFranchiseeInsurance = new FranchiseeInsurance();
        BeanUtil.copyProperties(franchiseeInsuranceAddAndUpdate, newFranchiseeInsurance);
        newFranchiseeInsurance.setUpdateTime(System.currentTimeMillis());
        newFranchiseeInsurance.setTenantId(tenantId);

        Integer update = baseMapper.update(newFranchiseeInsurance);

        InsuranceInstruction insuranceInstruction = new InsuranceInstruction();
        insuranceInstruction.setInsuranceId(newFranchiseeInsurance.getId());
        insuranceInstruction.setTenantId(tenantId);
        insuranceInstruction.setInstruction(franchiseeInsuranceAddAndUpdate.getInstruction());
        insuranceInstruction.setUpdateTime(System.currentTimeMillis());
        insuranceInstructionService.update(insuranceInstruction);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //先删再改
            redisService.delete(CacheConstant.CACHE_FRANCHISEE_INSURANCE + newFranchiseeInsurance.getId());
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R enableOrDisable(Long id, Integer status) {

        Integer tenantId = TenantContextHolder.getTenantId();

        FranchiseeInsurance franchiseeInsurance = queryByIdFromCache(id.intValue());
        if (Objects.isNull(franchiseeInsurance) || !Objects.equals(franchiseeInsurance.getTenantId(), tenantId)) {
            return R.ok();
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeInsurance.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("enableOrDisable INSURANCE ERROR! franchisee is null！ franchiseeId={}", franchiseeInsurance.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        if (Objects.equals(status, FranchiseeInsurance.STATUS_USABLE)) {
            FranchiseeInsuranceAddAndUpdate insuranceAddAndUpdate = new FranchiseeInsuranceAddAndUpdate();
            insuranceAddAndUpdate.setFranchiseeId(franchiseeInsurance.getFranchiseeId());
            insuranceAddAndUpdate.setInsuranceType(franchiseeInsurance.getInsuranceType());

            Integer result = null;
            if(Objects.equals(franchiseeInsurance.getInsuranceType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY)){
                insuranceAddAndUpdate.setSimpleBatteryType(franchiseeInsurance.getSimpleBatteryType());
            }else if(Objects.equals(franchiseeInsurance.getInsuranceType(), FranchiseeInsurance.INSURANCE_TYPE_CAR)){
                insuranceAddAndUpdate.setCarModelId(franchiseeInsurance.getCarModelId());
            }else if(Objects.equals(franchiseeInsurance.getInsuranceType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY_CAR)){
                insuranceAddAndUpdate.setSimpleBatteryType(franchiseeInsurance.getSimpleBatteryType());
                insuranceAddAndUpdate.setCarModelId(franchiseeInsurance.getCarModelId());
            }

            if(Objects.nonNull(checkInsuranceExist(insuranceAddAndUpdate))){
                return R.fail("100280", "存在相同类型已启用的保险");
            }
        }
/*

        if (Objects.equals(franchisee.getModelType(),Franchisee.OLD_MODEL_TYPE)) {
            if (Objects.equals(status, FranchiseeInsurance.STATUS_USABLE)) {
                int count = baseMapper.selectCount(new LambdaQueryWrapper<FranchiseeInsurance>()
                        .eq(FranchiseeInsurance::getTenantId, tenantId).eq(FranchiseeInsurance::getStatus, FranchiseeInsurance.STATUS_USABLE)
                        .eq(FranchiseeInsurance::getFranchiseeId, franchiseeInsurance.getFranchiseeId())
                        .eq(FranchiseeInsurance::getDelFlag, FranchiseeInsurance.DEL_NORMAL)
                        .notIn(FranchiseeInsurance::getId, id));
                if (count > 0) {
                    return R.fail("100242", "该加盟商已有启用中的保险，请勿重复添加");
                }
            }
        }else {
            int count = baseMapper.selectCount(new LambdaQueryWrapper<FranchiseeInsurance>()
                    .eq(FranchiseeInsurance::getBatteryType, franchiseeInsurance.getBatteryType()).eq(FranchiseeInsurance::getStatus, FranchiseeInsurance.STATUS_USABLE)
                    .eq(FranchiseeInsurance::getTenantId, tenantId)
                    .eq(FranchiseeInsurance::getFranchiseeId, franchiseeInsurance.getFranchiseeId())
                    .eq(FranchiseeInsurance::getDelFlag, FranchiseeInsurance.DEL_NORMAL)
                    .notIn(FranchiseeInsurance::getId, id));
            if (count > 0) {
                return R.fail("100251", "该型号已有启用中的保险，请勿重复添加");
            }
        }
*/

        FranchiseeInsurance newFranchiseeInsurance = new FranchiseeInsurance();
        newFranchiseeInsurance.setId(franchiseeInsurance.getId());
        newFranchiseeInsurance.setStatus(status);
        newFranchiseeInsurance.setUpdateTime(System.currentTimeMillis());
        newFranchiseeInsurance.setTenantId(tenantId);
        Integer update = baseMapper.update(newFranchiseeInsurance);

        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_FRANCHISEE_INSURANCE + newFranchiseeInsurance.getId());
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    private Integer checkInsuranceExist(FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {
        return baseMapper.checkInsuranceExist(franchiseeInsuranceAddAndUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R delete(Integer id) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //判断是否有用户绑定该保险
        List<InsuranceUserInfo> insuranceUserInfoList = insuranceUserInfoService.selectByInsuranceId(id, tenantId);
        if (!CollectionUtils.isEmpty(insuranceUserInfoList)) {
            log.error("ELE ERROR! delete memberCard fail,there are user use memberCard,memberCardId={}", id);
            return R.fail(queryByIdFromCache(id), "100243", "删除失败，该保险已有用户使用！");
        }


        FranchiseeInsurance newFranchiseeInsurance = new FranchiseeInsurance();
        newFranchiseeInsurance.setId(id);
        newFranchiseeInsurance.setDelFlag(ElectricityMemberCard.DEL_DEL);
        newFranchiseeInsurance.setTenantId(tenantId);
        Integer update = baseMapper.update(newFranchiseeInsurance);

        InsuranceInstruction insuranceInstruction = new InsuranceInstruction();
        insuranceInstruction.setInsuranceId(newFranchiseeInsurance.getId());
        insuranceInstruction.setDelFlag(InsuranceInstruction.DEL_DEL);
        insuranceInstruction.setUpdateTime(System.currentTimeMillis());
        insuranceInstructionService.update(insuranceInstruction);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.delete(CacheConstant.CACHE_FRANCHISEE_INSURANCE + newFranchiseeInsurance.getId());
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Slave
    @Override
    public R queryList(Long offset, Long size, Integer status, Integer type, Integer tenantId, Long franchiseeId) {
        List<FranchiseeInsuranceVo> franchiseeInsuranceVoList = baseMapper.queryList(offset, size, status, type, tenantId, franchiseeId);
        return R.ok(franchiseeInsuranceVoList);
    }

    @Slave
    @Override
    public List<FranchiseeInsuranceVo> selectByPage(FranchiseeInsuranceQuery query) {
        List<FranchiseeInsuranceVo> franchiseeInsuranceVoList = baseMapper.selectByPage(query);
        if(CollectionUtils.isEmpty(franchiseeInsuranceVoList)){
            return Collections.emptyList();
        }

        return franchiseeInsuranceVoList.parallelStream().peek(item->{
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            item.setFranchiseeName(Objects.isNull(franchisee)?"":franchisee.getName());

            if (Objects.nonNull(item.getStoreId())) {
                Store store = storeService.queryByIdFromCache(item.getStoreId());
                item.setStoreName(Objects.nonNull(store)?store.getName():"");
            }

            if(Objects.nonNull(item.getCarModelId())){
                ElectricityCarModel electricityCarModel = carModelService.queryByIdFromCache(item.getCarModelId().intValue());
                item.setCarModelName(Objects.nonNull(electricityCarModel)?electricityCarModel.getName():"");
            }
        }).collect(Collectors.toList());
    }

    @Slave
    @Override
    public Integer selectPageCount(FranchiseeInsuranceQuery query) {
        return baseMapper.selectPageCount(query);
    }

    @Slave
    @Override
    public R queryCount(Integer status, Integer type, Integer tenantId, Long franchiseeId) {
        return R.ok(baseMapper.queryCount(status, type, tenantId, franchiseeId, null));
    }

    @Override
    public FranchiseeInsurance queryByIdFromCache(Integer id) {
        FranchiseeInsurance franchiseeInsuranceCache = redisService.getWithHash(CacheConstant.CACHE_FRANCHISEE_INSURANCE + id, FranchiseeInsurance.class);
        if (Objects.nonNull(franchiseeInsuranceCache)) {
            return franchiseeInsuranceCache;
        }

        FranchiseeInsurance franchiseeInsurance = baseMapper.selectById(id);
        if (Objects.isNull(franchiseeInsurance)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_FRANCHISEE_INSURANCE + id, franchiseeInsurance);

        return franchiseeInsurance;
    }

    @Override
    public FranchiseeInsurance queryByFranchiseeId(Long franchiseeId, String batteryType, Integer tenantId) {
        return franchiseeInsuranceMapper.queryByFranchiseeIdAndBatteryType(franchiseeId, batteryType, tenantId);
    }

    @Override
    public FranchiseeInsurance queryByFranchiseeIdAndSimpleBatteryType(Long franchiseeId, String batteryType, Integer tenantId) {
        return franchiseeInsuranceMapper.queryByFranchiseeIdAndSimpleBatteryType(franchiseeId, batteryType, tenantId);
    }

    @Override
    public FranchiseeInsurance selectById(Integer insuranceId) {
        return this.baseMapper.selectById(insuranceId);
    }

    @Override
    public R queryCanAddInsuranceBatteryType(Long franchiseeId) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("ELE edit insurance query battery model ERROR! franchisee is null,franchiseeId={}", franchiseeId);
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        if (!Objects.equals(tenantId, franchisee.getTenantId())) {
            return R.ok();
        }

        List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);

        List<Integer> batteryList = new ArrayList<>();

        for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
            int existCount = baseMapper.selectCount(new LambdaQueryWrapper<FranchiseeInsurance>()
                    .eq(FranchiseeInsurance::getTenantId, tenantId)
                    .eq(FranchiseeInsurance::getBatteryType, batteryModelService.acquireBatteryShort(modelBatteryDeposit.getModel(), tenantId))
                    .eq(FranchiseeInsurance::getFranchiseeId, franchiseeId)
                    .eq(FranchiseeInsurance::getDelFlag, FranchiseeInsurance.DEL_NORMAL));
            if (existCount == 0) {
                batteryList.add(modelBatteryDeposit.getModel());
            }
        }

        return R.ok(batteryList);
    }

    @Override
    public List<FranchiseeInsurance> selectByFranchiseeId(Long franchiseeId, Integer tenantId) {
        return this.baseMapper.selectList(new LambdaQueryWrapper<FranchiseeInsurance>().eq(FranchiseeInsurance::getFranchiseeId, franchiseeId)
                .eq(FranchiseeInsurance::getDelFlag, FranchiseeInsurance.DEL_NORMAL)
                .eq(FranchiseeInsurance::getTenantId, tenantId));
    }

    @Override
    public void moveInsurance(FranchiseeMoveInfo franchiseeMoveInfo, Franchisee newFranchisee) {
        List<FranchiseeInsurance> oldFranchiseeInsurances = this.selectByFranchiseeId(franchiseeMoveInfo.getFromFranchiseeId(), TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(oldFranchiseeInsurances)) {
            return;
        }

        oldFranchiseeInsurances.stream().peek(item -> {
            item.setId(null);
            item.setName(item.getName() + "(迁)");
            item.setFranchiseeId(newFranchisee.getId());
            item.setBatteryType(batteryModelService.acquireBatteryShort(franchiseeMoveInfo.getBatteryModel(),TenantContextHolder.getTenantId()));
            item.setCreateTime(System.currentTimeMillis());
            item.setUpdateTime(System.currentTimeMillis());
        }).collect(Collectors.toList());

        List<FranchiseeInsurance> tempFranchiseeInsuranceList = new ArrayList<>();

        //新加盟商下的保险
        List<FranchiseeInsurance> newFranchiseeInsurances = this.selectByFranchiseeId(franchiseeMoveInfo.getToFranchiseeId(), TenantContextHolder.getTenantId());
        if (!CollectionUtils.isEmpty(newFranchiseeInsurances)) {
            //判断新加盟商是否已经有了旧加盟商下相同类型的保险
            for (FranchiseeInsurance oldFranchiseeInsurance : oldFranchiseeInsurances) {
                for (FranchiseeInsurance newFranchiseeInsurance : newFranchiseeInsurances) {
                    if (Objects.equals(oldFranchiseeInsurance.getCid(), newFranchiseeInsurance.getCid())
                            && Objects.equals(oldFranchiseeInsurance.getValidDays(), newFranchiseeInsurance.getValidDays())
                            && Objects.equals(oldFranchiseeInsurance.getInsuranceType(), newFranchiseeInsurance.getInsuranceType())
                            && Objects.equals(oldFranchiseeInsurance.getIsConstraint(), newFranchiseeInsurance.getIsConstraint())
                            && Objects.equals(oldFranchiseeInsurance.getBatteryType(), newFranchiseeInsurance.getBatteryType())
                            && Objects.equals(oldFranchiseeInsurance.getFranchiseeId(), newFranchiseeInsurance.getFranchiseeId())
                            && oldFranchiseeInsurance.getPremium().compareTo(newFranchiseeInsurance.getPremium()) == 0
                            && oldFranchiseeInsurance.getForehead().compareTo(newFranchiseeInsurance.getForehead()) == 0
                    ) {
                        tempFranchiseeInsuranceList.add(oldFranchiseeInsurance);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(tempFranchiseeInsuranceList)) {
            oldFranchiseeInsurances.removeAll(tempFranchiseeInsuranceList);
        }

        if (CollectionUtils.isEmpty(oldFranchiseeInsurances)) {
            return;
        }
        
        //新加盟商下保险+旧加盟商下可复制到新加盟商下的保险
        List<FranchiseeInsurance> totalFranchiseeInsurances=new ArrayList<>();
        totalFranchiseeInsurances.addAll(newFranchiseeInsurances);
        totalFranchiseeInsurances.addAll(oldFranchiseeInsurances);
        
        //校验新加盟商下是否有相同型号启用的保险
        Map<String, Long> collectMap = totalFranchiseeInsurances.stream()
                .filter(item -> Objects.equals(item.getStatus(), FranchiseeInsurance.STATUS_USABLE))
                .collect(Collectors.groupingBy(FranchiseeInsurance::getBatteryType, Collectors.counting()));
        if(!CollectionUtils.isEmpty(collectMap)){
            collectMap.forEach((k,v)->{
                if(v>NumberConstant.ONE_L){
                    throw new CustomBusinessException("旧加盟商与新加盟商下存在相同型号已启用的保险");
                }
            });
        }
    
        this.baseMapper.batchInsert(oldFranchiseeInsurances);

    }

    @Slave
    @Override
    public R selectInsuranceListByCondition( Integer status, Integer type, Integer tenantId, Long franchiseeId, String batterType) {
        List<FranchiseeInsuranceVo> franchiseeInsuranceVos = franchiseeInsuranceMapper.queryInsuranceList(status, type, tenantId, franchiseeId, batterType);
        if(CollectionUtils.isEmpty(franchiseeInsuranceVos)){
            return R.ok(franchiseeInsuranceVos);
        }
        franchiseeInsuranceVos.parallelStream().forEach(vo ->{
            City city = cityService.queryByIdFromDB(vo.getCid());
            if (Objects.nonNull(city) && Objects.nonNull(city.getName())) {
                vo.setCityName(city.getName());
            }
        });
        return R.ok(franchiseeInsuranceVos);
    }

    @Override
    public FranchiseeInsurance selectByFranchiseeIdAndType(Long franchiseeId, int insuranceTypeBattery, String batteryV) {
        return franchiseeInsuranceMapper.selectByFranchiseeIdAndType(franchiseeId,insuranceTypeBattery,batteryV);
    }

    @Override
    public FranchiseeInsurance selectInsuranceByType(FranchiseeInsuranceQuery query) {
        return franchiseeInsuranceMapper.selectInsuranceByType(query);
    }

    @Override
    public Triple<Boolean, String, Object> selectInsuranceByUid(Long uid, Integer type) {

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }

        Long storeId = null;

        Long carModelId=null;

        String simpleBatteryType = null;

        if (Objects.equals(type, FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
            simpleBatteryType = userBatteryTypeService.selectUserSimpleBatteryType(uid);
        } else if (Objects.equals(type, FranchiseeInsurance.INSURANCE_TYPE_CAR)) {
            if (Objects.nonNull(userInfo.getStoreId()) && !Objects.equals(userInfo.getStoreId(), NumberConstant.ZERO_L)) {
                storeId = userInfo.getStoreId();
            }

            //获取用户绑定的车辆型号
            Integer carModelByUid = carRentalPackageMemberTermBizService.queryCarModelByUid(userInfo.getTenantId(), uid);
            if (Objects.isNull(carModelByUid)) {
                return Triple.of(false, "100005", "未找到车辆型号");
            }

            carModelId=carModelByUid.longValue();
        } else if (Objects.equals(type, FranchiseeInsurance.INSURANCE_TYPE_BATTERY_CAR)) {
            simpleBatteryType = userBatteryTypeService.selectUserSimpleBatteryType(uid);

            if (Objects.nonNull(userInfo.getStoreId()) && !Objects.equals(userInfo.getStoreId(), NumberConstant.ZERO_L)) {
                storeId = userInfo.getStoreId();
            }

            //获取用户绑定的车辆型号
            Integer carModelByUid = carRentalPackageMemberTermBizService.queryCarModelByUid(userInfo.getTenantId(), uid);
            if (Objects.isNull(carModelByUid)) {
                return Triple.of(false, "100005", "未找到车辆型号");
            }

            carModelId=carModelByUid.longValue();
        }

        FranchiseeInsuranceQuery query = FranchiseeInsuranceQuery.builder()
                .tenantId(userInfo.getTenantId())
                .simpleBatteryType(simpleBatteryType)
                .storeId(storeId)
                .carModelId(carModelId)
                .franchiseeId(userInfo.getFranchiseeId())
                .insuranceType(type)
                .status(FranchiseeInsurance.STATUS_USABLE).build();

        FranchiseeInsurance franchiseeInsurance = selectInsuranceByType(query);
        if(Objects.isNull(franchiseeInsurance)){
            return Triple.of(true,null,null);
        }

        FranchiseeInsuranceVo franchiseeInsuranceVo = new FranchiseeInsuranceVo();
        BeanUtils.copyProperties(franchiseeInsurance , franchiseeInsuranceVo);

        City city = cityService.queryByIdFromDB(franchiseeInsurance.getCid());
        franchiseeInsuranceVo.setCityName(Objects.isNull(city)?"":city.getName());

        return Triple.of(true,null,franchiseeInsuranceVo);
    }
    
    @Override
    @Slave
    public List<FranchiseeInsuranceOrderIdsVo> queryInsuranceIds(FranchiseeInsuranceIdsRequest franchiseeInsuranceIdsRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)) {
                return Collections.EMPTY_LIST;
            }
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return Collections.EMPTY_LIST;
            }
        }
        FranchiseeInsuranceQuery insuranceOrderQuery = FranchiseeInsuranceQuery.builder()
                .size(franchiseeInsuranceIdsRequest.getSize())
                .offset(franchiseeInsuranceIdsRequest.getOffset())
                .name(franchiseeInsuranceIdsRequest.getName())
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .type(franchiseeInsuranceIdsRequest.getType())
                .build();
        return franchiseeInsuranceMapper.selectOrderIds(insuranceOrderQuery);
    }
}
