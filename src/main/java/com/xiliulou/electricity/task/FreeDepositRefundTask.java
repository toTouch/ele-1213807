package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 免押退款查询解冻结果
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-03-01-18:29
 */
@Component
@Slf4j
@JobHandler(value = "freeDepositRefundTask")
public class FreeDepositRefundTask extends IJobHandler {

    @Autowired
    FreeDepositOrderService freeDepositOrderService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            freeDepositOrderService.handleFreeDepositRefundOrder();
        } catch (Exception e) {
            log.error("ELE ERROR!handle free deposit refund order error!", e);
            return IJobHandler.FAIL;
        }

        return IJobHandler.SUCCESS;
    }
}
