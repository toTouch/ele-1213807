package com.xiliulou.electricity.task.merchant;

import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author BaoYu
 * @description 商户提现状态查询后更新
 * @date 2024/2/27 22:39
 */

@Component
@Slf4j
@JobHandler(value = "merchantWithdrawApplicationUpdateStatusTask")
public class MerchantWithdrawApplicationUpdateStatusTask extends IJobHandler {

    @Resource
    private MerchantWithdrawApplicationService merchantWithdrawApplicationService;


    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            merchantWithdrawApplicationService.updateMerchantWithdrawStatus();
        } catch (Exception e) {
            log.error("商户提现状态更新处理失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
