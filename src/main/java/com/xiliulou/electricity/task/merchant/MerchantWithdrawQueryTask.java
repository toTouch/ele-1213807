package com.xiliulou.electricity.task.merchant;

import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@JobHandler(value = "merchantWithdrawQueryTask")
public class MerchantWithdrawQueryTask extends IJobHandler {
    @Resource
    private MerchantWithdrawApplicationService merchantWithdrawApplicationService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
        } catch (Exception e) {
            log.error("商户提现状态更新处理失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
