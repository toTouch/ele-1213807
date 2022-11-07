package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.CouponMapper;
import com.xiliulou.electricity.mapper.ElectricityMemberCardMapper;
import com.xiliulou.electricity.mapper.FranchiseeInsuranceMapper;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.query.FranchiseeInsuranceAddAndUpdate;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.InsuranceInstructionService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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

    @Override
    public R add(FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Integer count = baseMapper.queryCount(null, franchiseeInsuranceAddAndUpdate.getInsuranceType(), tenantId,  null, franchiseeInsuranceAddAndUpdate.getName());
        if (count > 0) {
            log.error("ELE ERROR! create insurance fail,there are same insuranceName,insuranceName={}", franchiseeInsuranceAddAndUpdate.getName());
            return R.fail("100304", "保险名称已存在！");
        }

        FranchiseeInsurance franchiseeInsurance=new FranchiseeInsurance();
        BeanUtil.copyProperties(franchiseeInsuranceAddAndUpdate, franchiseeInsurance);

        //填充参数
        franchiseeInsurance.setCreateTime(System.currentTimeMillis());
        franchiseeInsurance.setUpdateTime(System.currentTimeMillis());
        franchiseeInsurance.setStatus(FranchiseeInsurance.STATUS_UN_USABLE);
        franchiseeInsurance.setTenantId(tenantId);
        franchiseeInsurance.setDelFlag(ElectricityMemberCard.DEL_NORMAL);
        Integer insert = baseMapper.insert(franchiseeInsurance);

        InsuranceInstruction insuranceInstruction=new InsuranceInstruction();
        insuranceInstruction.setFranchiseeId(franchiseeInsurance.getFranchiseeId());
        insuranceInstruction.setInsuranceId(franchiseeInsurance.getId());
        insuranceInstruction.setTenantId(tenantId);
        insuranceInstruction.setInstruction(franchiseeInsuranceAddAndUpdate.getInstruction());
        insuranceInstruction.setCreateTime(System.currentTimeMillis());
        insuranceInstruction.setUpdateTime(System.currentTimeMillis());
        insuranceInstructionService.insert(insuranceInstruction);

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

//        Integer count = baseMapper.queryCount(null, franchiseeInsuranceAddAndUpdate.getInsuranceType(), tenantId, null, franchiseeInsuranceAddAndUpdate.getName());
//
//        if (count > 0 && !Objects.equals(oldFranchiseeInsurance.getName(), franchiseeInsuranceAddAndUpdate.getName())) {
//            log.error("ELE ERROR! create insurance fail,there are same insuranceName,insuranceName={}", franchiseeInsuranceAddAndUpdate.getName());
//            return R.fail("100304", "保险名称已存在！");
//        }

        FranchiseeInsurance newFranchiseeInsurance=new FranchiseeInsurance();
        BeanUtil.copyProperties(franchiseeInsuranceAddAndUpdate, newFranchiseeInsurance);
        newFranchiseeInsurance.setUpdateTime(System.currentTimeMillis());
        newFranchiseeInsurance.setTenantId(tenantId);
        Integer update = baseMapper.update(newFranchiseeInsurance);

        InsuranceInstruction insuranceInstruction=new InsuranceInstruction();
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
    public R delete(Integer id) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //判断是否有用户绑定该保险
        List<InsuranceUserInfo> insuranceUserInfoList = insuranceUserInfoService.selectByInsuranceId(id, tenantId);
        if (!CollectionUtils.isEmpty(insuranceUserInfoList)) {
            log.error("ELE ERROR! delete memberCard fail,there are user use memberCard,memberCardId={}", id);
            return R.fail(queryByCache(id), "100100", "删除失败，该套餐已有用户使用！");
        }


        FranchiseeInsurance newFranchiseeInsurance = new FranchiseeInsurance();
        newFranchiseeInsurance.setId(id);
        newFranchiseeInsurance.setDelFlag(ElectricityMemberCard.DEL_DEL);
        newFranchiseeInsurance.setTenantId(tenantId);
        Integer update = baseMapper.update(newFranchiseeInsurance);

        InsuranceInstruction insuranceInstruction=new InsuranceInstruction();
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
        return R.ok(baseMapper.queryCount(status,type,tenantId,franchiseeId,null));
    }

    @Override
    public FranchiseeInsurance queryByCache(Integer id) {
        FranchiseeInsurance franchiseeInsurance = null;
        franchiseeInsurance = redisService.getWithHash(CacheConstant.CACHE_FRANCHISEE_INSURANCE + id, FranchiseeInsurance.class);
        if (Objects.isNull(franchiseeInsurance)) {
            franchiseeInsurance = baseMapper.selectById(id);
            if (Objects.nonNull(franchiseeInsurance)) {
                redisService.saveWithHash(CacheConstant.CACHE_FRANCHISEE_INSURANCE + id, franchiseeInsurance);
            }
        }
        return franchiseeInsurance;
    }
}
