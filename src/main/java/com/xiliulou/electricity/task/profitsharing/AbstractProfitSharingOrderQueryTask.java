/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/29
 */

package com.xiliulou.electricity.task.profitsharing;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderDetailService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/29 17:04
 */
@Slf4j
public abstract class AbstractProfitSharingOrderQueryTask<T extends BasePayConfig> extends AbstractProfitSharingTask<T> {
    
    @Resource
    private ProfitSharingOrderService profitSharingOrderService;
    
    @Resource
    private ProfitSharingOrderDetailService profitSharingOrderDetailService;
    
    /**
     * 根据租户处理
     *
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/8/26 16:44
     */
    @Override
    protected void executeByTenantId(Integer tenantId) {
        
        Long startId = 0L;
        
        while (true) {
            profitSharingOrderService.queryListByParam();
        }
        
        
    }
}
