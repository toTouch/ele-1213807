package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.FranchiseeInsuranceMapper;
import com.xiliulou.electricity.query.FranchiseeInsuranceAddAndUpdate;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceInstructionService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public R add(FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Integer count = baseMapper.queryCount(null, franchiseeInsuranceAddAndUpdate.getInsuranceType(), tenantId, null, franchiseeInsuranceAddAndUpdate.getName());
        if (count > 0) {
            log.error("ELE ERROR! create insurance fail,there are same insuranceName,insuranceName={}", franchiseeInsuranceAddAndUpdate.getName());
            return R.fail("100304", "保险名称已存在！");
        }

        FranchiseeInsurance franchiseeInsurance = new FranchiseeInsurance();
        BeanUtil.copyProperties(franchiseeInsuranceAddAndUpdate, franchiseeInsurance);

        //填充参数
        franchiseeInsurance.setCreateTime(System.currentTimeMillis());
        franchiseeInsurance.setUpdateTime(System.currentTimeMillis());
        franchiseeInsurance.setStatus(FranchiseeInsurance.STATUS_UN_USABLE);
        franchiseeInsurance.setTenantId(tenantId);
        franchiseeInsurance.setDelFlag(ElectricityMemberCard.DEL_NORMAL);

        InsuranceInstruction insuranceInstruction = new InsuranceInstruction();
        insuranceInstruction.setFranchiseeId(franchiseeInsurance.getFranchiseeId());
        insuranceInstruction.setInsuranceId(franchiseeInsurance.getId());
        insuranceInstruction.setTenantId(tenantId);
        insuranceInstruction.setInstruction(franchiseeInsuranceAddAndUpdate.getInstruction());
        insuranceInstruction.setCreateTime(System.currentTimeMillis());
        insuranceInstruction.setUpdateTime(System.currentTimeMillis());

        Integer insert = null;

        if (Objects.nonNull(franchiseeInsuranceAddAndUpdate.getBatteryTypeList())) {
            for (String batteryType : franchiseeInsuranceAddAndUpdate.getBatteryTypeList()) {
                franchiseeInsurance.setBatteryType(BatteryConstant.acquireBatteryShort(Integer.valueOf(franchiseeInsurance.getBatteryType())));
                int existCount = baseMapper.selectCount(new LambdaQueryWrapper<FranchiseeInsurance>()
                        .eq(FranchiseeInsurance::getTenantId, tenantId)
                        .eq(FranchiseeInsurance::getBatteryType, batteryType)
                        .eq(FranchiseeInsurance::getFranchiseeId, franchiseeInsurance.getFranchiseeId())
                        .eq(FranchiseeInsurance::getDelFlag, FranchiseeInsurance.DEL_NORMAL));
                if (existCount == 0) {
                    insert = baseMapper.insert(franchiseeInsurance);
                    insuranceInstructionService.insert(insuranceInstruction);
                }
            }
        } else {
            insert = baseMapper.insert(franchiseeInsurance);
            insuranceInstructionService.insert(insuranceInstruction);
        }

        DbUtils.dbOperateSuccessThen(insert, () -> {
            return null;
        });

        if (insert > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");

    }

    @Override
    public R update(FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        FranchiseeInsurance oldFranchiseeInsurance = baseMapper.selectOne(new LambdaQueryWrapper<FranchiseeInsurance>().eq(FranchiseeInsurance::getId, franchiseeInsuranceAddAndUpdate.getId()).eq(FranchiseeInsurance::getTenantId, tenantId));

        if (Objects.isNull(oldFranchiseeInsurance)) {
            return R.ok();
        }

        FranchiseeInsurance newFranchiseeInsurance = new FranchiseeInsurance();
        BeanUtil.copyProperties(franchiseeInsuranceAddAndUpdate, newFranchiseeInsurance);
        newFranchiseeInsurance.setUpdateTime(System.currentTimeMillis());
        newFranchiseeInsurance.setTenantId(tenantId);
        if (StringUtils.isNotEmpty(franchiseeInsuranceAddAndUpdate.getBatteryType())) {
            newFranchiseeInsurance.setBatteryType(BatteryConstant.acquireBatteryShort(Integer.valueOf(franchiseeInsuranceAddAndUpdate.getBatteryType())));
        }
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
    public R enableOrDisable(Long id, Integer status) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        FranchiseeInsurance franchiseeInsurance = queryByCache(id.intValue());
        if (Objects.isNull(franchiseeInsurance)) {
            return R.ok();
        }

        if (!Objects.equals(franchiseeInsurance.getTenantId(), tenantId) || Objects.equals(status, franchiseeInsurance.getStatus())) {
            return R.ok();
        }

//        if (Objects.equals(status, FranchiseeInsurance.STATUS_USABLE)) {
//            int count = baseMapper.selectCount(new LambdaQueryWrapper<FranchiseeInsurance>()
//                    .eq(FranchiseeInsurance::getTenantId, tenantId).eq(FranchiseeInsurance::getStatus, FranchiseeInsurance.STATUS_USABLE)
//                    .eq(FranchiseeInsurance::getFranchiseeId, franchiseeInsurance.getFranchiseeId())
//                    .eq(FranchiseeInsurance::getDelFlag, FranchiseeInsurance.DEL_NORMAL)
//                    .notIn(FranchiseeInsurance::getId, id));
//            if (count > 0) {
//                return R.fail("100242", "该加盟商已有启用中的保险，请勿重复添加");
//            }
//        }

        FranchiseeInsurance newFranchiseeInsurance = new FranchiseeInsurance();
        newFranchiseeInsurance.setId(franchiseeInsurance.getId());
        newFranchiseeInsurance.setStatus(status);
        newFranchiseeInsurance.setUpdateTime(System.currentTimeMillis());
        newFranchiseeInsurance.setTenantId(tenantId);
        Integer update = baseMapper.update(newFranchiseeInsurance);

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
    public R delete(Integer id) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //判断是否有用户绑定该保险
        List<InsuranceUserInfo> insuranceUserInfoList = insuranceUserInfoService.selectByInsuranceId(id, tenantId);
        if (!CollectionUtils.isEmpty(insuranceUserInfoList)) {
            log.error("ELE ERROR! delete memberCard fail,there are user use memberCard,memberCardId={}", id);
            return R.fail(queryByCache(id), "100243", "删除失败，该保险已有用户使用！");
        }


        FranchiseeInsurance newFranchiseeInsurance = new FranchiseeInsurance();
        newFranchiseeInsurance.setId(id);
        newFranchiseeInsurance.setDelFlag(ElectricityMemberCard.DEL_DEL);
        newFranchiseeInsurance.setTenantId(tenantId);
        Integer update = baseMapper.update(newFranchiseeInsurance);

        InsuranceInstruction insuranceInstruction = new InsuranceInstruction();
        insuranceInstruction.setInsuranceId(newFranchiseeInsurance.getId());
        insuranceInstruction.setTenantId(tenantId);
        insuranceInstruction.setInsuranceId(id);
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

    @Override
    public R queryList(Long offset, Long size, Integer status, Integer type, Integer tenantId, Long franchiseeId) {
        return R.ok(baseMapper.queryList(offset, size, status, type, tenantId, franchiseeId));
    }

    @Override
    public R queryCount(Integer status, Integer type, Integer tenantId, Long franchiseeId) {
        return R.ok(baseMapper.queryCount(status, type, tenantId, franchiseeId, null));
    }

    @Override
    public FranchiseeInsurance queryByCache(Integer id) {
        FranchiseeInsurance franchiseeInsurance = redisService.getWithHash(CacheConstant.CACHE_FRANCHISEE_INSURANCE + id, FranchiseeInsurance.class);
        if (Objects.isNull(franchiseeInsurance)) {
            franchiseeInsurance = baseMapper.selectById(id);
            if (Objects.nonNull(franchiseeInsurance)) {
                redisService.saveWithHash(CacheConstant.CACHE_FRANCHISEE_INSURANCE + id, franchiseeInsurance);
            }
        }
        return franchiseeInsurance;
    }

    @Override
    public FranchiseeInsurance queryByFranchiseeId(Long franchiseeId,String batteryType,Integer tenantId) {
        return franchiseeInsuranceMapper.queryByFranchiseeIdAndBatteryType(franchiseeId,batteryType,tenantId);
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

        if (Objects.equals(tenantId, franchisee.getTenantId())) {
            return R.ok();
        }

        List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);

        List<Integer> batteryList = new ArrayList<>();

        for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
            int existCount = baseMapper.selectCount(new LambdaQueryWrapper<FranchiseeInsurance>()
                    .eq(FranchiseeInsurance::getTenantId, tenantId)
                    .eq(FranchiseeInsurance::getBatteryType, BatteryConstant.acquireBatteryShort(modelBatteryDeposit.getModel()))
                    .eq(FranchiseeInsurance::getFranchiseeId, franchiseeId)
                    .eq(FranchiseeInsurance::getDelFlag, FranchiseeInsurance.DEL_NORMAL));
            if (existCount == 0) {
                batteryList.add(modelBatteryDeposit.getModel());
            }
        }

        return R.ok(batteryList);
    }
}
