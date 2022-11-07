package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.FranchiseeInsuranceMapper;
import com.xiliulou.electricity.mapper.InsuranceUserInfoMapper;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
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

    @Autowired
    UserInfoService userInfoService;

    @Override
    public List<InsuranceUserInfo> selectByInsuranceId(Integer id, Integer tenantId) {
        return insuranceUserInfoMapper.selectList(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getInsuranceId, id).eq(InsuranceUserInfo::getTenantId,tenantId)
                .eq(InsuranceUserInfo::getDelFlag, InsuranceUserInfo.DEL_NORMAL));
    }

    @Override
    public InsuranceUserInfo queryByUid(Long uid,Integer tenantId) {
        return insuranceUserInfoMapper.selectOne(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getUid, uid).eq(InsuranceUserInfo::getTenantId,tenantId)
                .eq(InsuranceUserInfo::getDelFlag, InsuranceUserInfo.DEL_NORMAL));
    }

    @Override
    public R updateInsuranceStatus(Long uid, Integer insuranceStatus) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        InsuranceUserInfo updateInsuranceUserInfo =new InsuranceUserInfo();
        updateInsuranceUserInfo.setIsUse(insuranceStatus);
        updateInsuranceUserInfo.setUid(uid);
        updateInsuranceUserInfo.setTenantId(tenantId);
        updateInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());

        return R.ok(insuranceUserInfoMapper.update(updateInsuranceUserInfo));
    }
}
