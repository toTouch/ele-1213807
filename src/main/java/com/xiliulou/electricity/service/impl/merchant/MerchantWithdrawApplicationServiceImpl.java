package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.mapper.merchant.MerchantWithdrawApplicationMapper;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/21 17:50
 */

@Slf4j
@Service("merchantWithdrawApplicationService")
public class MerchantWithdrawApplicationServiceImpl implements MerchantWithdrawApplicationService {
    
    @Resource
    private MerchantWithdrawApplicationMapper merchantWithdrawApplicationMapper;
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private RedisService redisService;
    
    
    @Override
    public Integer saveMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        
        MerchantWithdrawApplication merchantWithdrawApplication = new MerchantWithdrawApplication();
        merchantWithdrawApplication.setAmount(merchantWithdrawApplicationRequest.getAmount());
        merchantWithdrawApplication.setUid(merchantWithdrawApplicationRequest.getMerchantUid());
        merchantWithdrawApplication.setTenantId(merchantWithdrawApplicationRequest.getTenantId());
        
    
        merchantWithdrawApplication.setCreateTime(System.currentTimeMillis());
        merchantWithdrawApplication.setUpdateTime(System.currentTimeMillis());
        
        
        merchantWithdrawApplicationMapper.insertOne(merchantWithdrawApplication);
        
        return null;
    }
    
    @Override
    public Integer updateMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        return null;
    }
    
    @Override
    public Integer removeMerchantWithdrawApplication(Long id) {
        return null;
    }
    
    @Override
    public Integer countMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        return null;
    }
    
    @Override
    public List<MerchantWithdrawApplication> queryMerchantWithdrawApplicationList(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        return null;
    }
    
    @Override
    public MerchantWithdrawApplication queryMerchantWithdrawApplication(Long id) {
        return null;
    }
}
