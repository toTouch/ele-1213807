/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/26
 */

package com.xiliulou.electricity.task.profitsharing;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.pay.base.enums.ChannelEnum;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/26 16:33
 */
@Slf4j
@Component
@JobHandler(value = "profitSharingOrderTask")
public class ProfitSharingOrderTask extends AbstractProfitSharingTradeOrderTask {
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    
    @Override
    protected String getChannel() {
        return ChannelEnum.WECHAT.getCode();
    }
    
    
    /**
     * 查询构建支付配置
     *
     * @param tenantFranchiseePayParamMap
     * @param tenantId
     * @param franchiseeIds
     * @author caobotao.cbt
     * @date 2024/8/27 11:06
     */
    @Override
    protected void queryBuildTenantFranchiseePayParamMap(Map<String, BasePayConfig> tenantFranchiseePayParamMap, Integer tenantId, Set<Long> franchiseeIds) {
        try {
            Set<Long> needQueryFranchiseeIds = new HashSet<>();
            franchiseeIds.forEach(franchiseeId -> {
                String payParamMapKey = getPayParamMapKey(tenantId, franchiseeId);
                if (!tenantFranchiseePayParamMap.containsKey(payParamMapKey)) {
                    needQueryFranchiseeIds.add(franchiseeId);
                }
            });
            
            if (CollectionUtils.isEmpty(needQueryFranchiseeIds)) {
                return;
            }
            
            List<WechatPayParamsDetails> wechatPayParamsDetailsList = wechatPayParamsBizService.queryListPreciseCacheByTenantIdAndFranchiseeIds(tenantId, needQueryFranchiseeIds,
                    Collections.singleton(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG_AND_RECEIVER_CONFIG));
            
            Map<Long, WechatPayParamsDetails> franchiseePayParamsMap = Optional.ofNullable(wechatPayParamsDetailsList).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.toMap(WechatPayParamsDetails::getFranchiseeId, Function.identity(), (k1, k2) -> k1));
            
            needQueryFranchiseeIds.forEach(franchiseeId -> tenantFranchiseePayParamMap.put(getPayParamMapKey(tenantId, franchiseeId), franchiseePayParamsMap.get(franchiseeId)));
            
        } catch (Exception e) {
            log.info("ProfitSharingOrderTask.queryBuildTenantFranchiseePayParamMap Exception:", e);
        }
        
    }
}
