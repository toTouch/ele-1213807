/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/30
 */

package com.xiliulou.electricity.task.profitsharing.support;

import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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
 * @date 2024/8/30 09:51
 */
@Slf4j
@Service
public class PayParamsQuerySupport {
    
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    
    /**
     * 查询并且构建支付配置
     *
     * @param tenantFranchiseePayParamMap
     * @param tenantId
     * @param franchiseeIds
     * @author caobotao.cbt
     * @date 2024/9/4 09:12
     */
    public void queryBuildTenantFranchiseePayParamMap(Map<String, WechatPayParamsDetails> tenantFranchiseePayParamMap, Integer tenantId, Set<Long> franchiseeIds) {
        try {
            // 判定 tenantFranchiseePayParamMap 是否已存在配置，不存在 添加到 needQueryFranchiseeIds中
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
            
            // 查询支付配置
            List<WechatPayParamsDetails> wechatPayParamsDetailsList = wechatPayParamsBizService.queryListPreciseCacheByTenantIdAndFranchiseeIds(tenantId, needQueryFranchiseeIds,
                    Collections.singleton(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG_AND_RECEIVER_CONFIG));
            // 根据加盟商分组
            Map<Long, WechatPayParamsDetails> franchiseePayParamsMap = Optional.ofNullable(wechatPayParamsDetailsList).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.toMap(WechatPayParamsDetails::getFranchiseeId, Function.identity(), (k1, k2) -> k1));
            
            // 添加到 tenantFranchiseePayParamMap 中
            needQueryFranchiseeIds.forEach(franchiseeId -> tenantFranchiseePayParamMap.put(getPayParamMapKey(tenantId, franchiseeId), franchiseePayParamsMap.get(franchiseeId)));
            
        } catch (Exception e) {
            log.info("PayParamsQuerySupport.queryBuildTenantFranchiseePayParamMap Exception:", e);
        }
        
    }
    
    /**
     * 支付配置key组装
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/9/4 09:10
     */
    public String getPayParamMapKey(Integer tenantId, Long franchiseeId) {
        return tenantId + "_" + franchiseeId;
    }
}
