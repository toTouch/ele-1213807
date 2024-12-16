package com.xiliulou.electricity.service.process;

import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.pipeline.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: ExchangeUserInfoAssertProcess
 * @description: 用户信息校验处理器
 * @author: renhang
 * @create: 2024-11-12 14:55
 */
@Service("exchangeUserInfoAssertProcess")
@Slf4j
public class ExchangeUserInfoAssertProcess extends AbstractExchangeCommonHandler implements ExchangeAssertProcess<ExchangeAssertProcessDTO> {
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Override
    public void process(ProcessContext<ExchangeAssertProcessDTO> context) {
        // 用户校验
        UserInfo userInfo = context.getProcessModel().getUserInfo();
        
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("ORDER WARN! user is unUsable,uid={} ", userInfo.getUid());
            breakChain(context, "ELECTRICITY.0024", "用户已被禁用");
            return;
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("ORDER WARN! userinfo is UN AUTH! uid={}", userInfo.getUid());
            breakChain(context, "100206", "用户未审核");
            return;
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("ORDER WARN! not found franchisee,uid={}", userInfo.getUid());
            breakChain(context, "ELECTRICITY.0038", "加盟商不存在");
            return;
        }
        
        context.getProcessModel().getChainObject().setFranchisee(franchisee);
    }
}
