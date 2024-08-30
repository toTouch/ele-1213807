/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/29
 */

package com.xiliulou.electricity.task.profitsharing.base;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.task.profitsharing.AbstractProfitSharingTradeOrderTask;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/29 17:09
 */
@Slf4j
public abstract class AbstractProfitSharingTask<T extends BasePayConfig> extends IJobHandler {
    
    public static final Integer SIZE = 200;
    
    @Resource
    private TenantService tenantService;
    
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        TtlTraceIdSupport.set();
        try {
            AbstractProfitSharingTradeOrderTask.TaskParam taskParam = new AbstractProfitSharingTradeOrderTask.TaskParam();
            if (StringUtils.isNotBlank(param)) {
                taskParam = JsonUtil.fromJson(param, AbstractProfitSharingTradeOrderTask.TaskParam.class);
            }
            
            if (CollectionUtils.isNotEmpty(taskParam.getTenantIds())) {
                // 指定租户
                taskParam.getTenantIds().forEach(tid -> this.executeByTenantId(tid));
                return ReturnT.SUCCESS;
            }
            
            Integer startTenantId = 0;
            
            while (true) {
                // 查询租户
                List<Integer> tenantIds = tenantService.queryIdListByStartId(startTenantId, SIZE);
                
                if (CollectionUtils.isEmpty(tenantIds)) {
                    break;
                }
                startTenantId = tenantIds.get(tenantIds.size() - 1);
                // 处理租户分账
                tenantIds.forEach(tid -> this.executeByTenantId(tid));
            }
            
            return ReturnT.SUCCESS;
        } finally {
            TtlTraceIdSupport.clear();
        }
    }
    
    
    /**
     * 构建支付配置
     *
     * @param tenantFranchiseePayParamMap
     * @param tenantId
     * @param franchiseeIds
     * @author caobotao.cbt
     * @date 2024/8/28 10:38
     */
    protected abstract void queryBuildTenantFranchiseePayParamMap(Map<String, T> tenantFranchiseePayParamMap, Integer tenantId, Set<Long> franchiseeIds);
    
    /**
     * 根据租户id 分批处理
     *
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/8/29 17:10
     */
    protected abstract void executeByTenantId(Integer tenantId);
    
    /**
     * 获取渠道
     *
     * @author caobotao.cbt
     * @date 2024/8/28 09:42
     */
    protected abstract String getChannel();
    
    
}
