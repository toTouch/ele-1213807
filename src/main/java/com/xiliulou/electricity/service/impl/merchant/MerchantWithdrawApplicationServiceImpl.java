package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.mapper.merchant.MerchantWithdrawApplicationMapper;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

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
    public Triple<Boolean, String, Object> saveMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {

        //限频
        Boolean getLockSuccess = redisService.setNx(CacheConstant.CACHE_WITHDRAW_USER_UID + merchantWithdrawApplicationRequest.getMerchantUid(), "1", 5L, false);
        if (!getLockSuccess) {
            return Triple.of(false, null, "操作频繁,请稍后再试");
        }

        //查询商户余额表，是否存在商户账户

        //检查余额表中的余额是否满足提现金额

        //单次提现金额需要满足限制条件， 不小于2元，不大于20000？ 待确定

        //查询银行卡信息，检查银行卡是否存在，并且检查该银行卡是否支持转账

        //计算手续费

        //插入提现表

        //TODO 扣除商户账户余额表中的余额，并创建提现记录。 该步操作是否应该放在审核成功之后？





        MerchantWithdrawApplication merchantWithdrawApplication = new MerchantWithdrawApplication();
        merchantWithdrawApplication.setAmount(merchantWithdrawApplicationRequest.getAmount());
        merchantWithdrawApplication.setUid(merchantWithdrawApplicationRequest.getMerchantUid());
        merchantWithdrawApplication.setTenantId(merchantWithdrawApplicationRequest.getTenantId());
        
    
        merchantWithdrawApplication.setCreateTime(System.currentTimeMillis());
        merchantWithdrawApplication.setUpdateTime(System.currentTimeMillis());
        
        
        Integer result = merchantWithdrawApplicationMapper.insertOne(merchantWithdrawApplication);
        
        return  Triple.of(true, null, result);
    }

    public Triple<Boolean, String, Object> approveWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {

        //检查提现审核参数状态，是否为待审核状态
        MerchantWithdrawApplication merchantWithdrawApplication = merchantWithdrawApplicationMapper.selectById(merchantWithdrawApplicationRequest.getId());
        if(Objects.isNull(merchantWithdrawApplication)){
            return Triple.of(false, "", "提现申请不存在");
        }

        //检查提现状态是否为审核中的状态

        //检查入参中的状态是否为同意或者拒绝状态，若为其他状态，则提示错误。

        //若为同意提现，则修改提现状态为已审核，并且修改提现记录表中的提现状态为已审核。

        //若为拒绝提现，则修改提现状态为已拒绝，并且修改提现记录表中的提现状态为已拒绝。

        //扣除商户账户余额表中的余额，并创建提现记录，

        //判断需要线下提现或者线上提现。

        return null;
    }
    
    @Override
    public Triple<Boolean, String, Object> updateMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
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
