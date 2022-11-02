package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.CouponMapper;
import com.xiliulou.electricity.mapper.FranchiseeInsuranceMapper;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 换电柜保险(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@Service("franchiseeInsuranceService")
@Slf4j
public class FranchiseeInsuranceServiceImpl implements FranchiseeInsuranceService {

    @Resource
    FranchiseeInsuranceMapper franchiseeInsuranceMapper;

    @Override
    public R add(FranchiseeInsurance franchiseeInsurance) {
        return null;
    }
}
