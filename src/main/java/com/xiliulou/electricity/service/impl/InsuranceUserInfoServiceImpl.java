package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.mapper.FranchiseeInsuranceMapper;
import com.xiliulou.electricity.mapper.InsuranceUserInfoMapper;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜保险用户绑定(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@Service("insuranceUserInfoService")
@Slf4j
public class InsuranceUserInfoServiceImpl extends ServiceImpl<InsuranceUserInfoMapper, InsuranceUserInfo> implements InsuranceUserInfoService {

    @Resource
    InsuranceUserInfoMapper insuranceUserInfoMapper;

    @Override
    public List<InsuranceUserInfo> selectByInsuranceId(Integer id, Integer tenantId) {
        return insuranceUserInfoMapper.selectList(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getInsuranceId, id).eq(InsuranceUserInfo::getTenantId,tenantId)
                .eq(InsuranceUserInfo::getDelFlag, InsuranceUserInfo.DEL_NORMAL));
    }
}
