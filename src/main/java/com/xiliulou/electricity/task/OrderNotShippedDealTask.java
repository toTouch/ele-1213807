package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.OrderNotShippedDealService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2024/11/21 9:36
 * @desc 微信支付订单未发货处理
 */
@Component
@JobHandler(value = "orderNotShippedDealTask")
@Slf4j
public class OrderNotShippedDealTask extends IJobHandler {
    @Resource
    private OrderNotShippedDealService orderNotShippedDealService;
    
    @Override
    public ReturnT<String> execute(String s)  {
        try {
            Integer tenantId = null;
            if (ObjectUtils.isNotEmpty(s)) {
                tenantId = Integer.valueOf(s);
            }
            
            orderNotShippedDealService.handleNotShippedOrder(tenantId);
        } catch (Exception e) {
            log.error("order not shipped deal task error",e);
        }
        return IJobHandler.SUCCESS;
    }
}
