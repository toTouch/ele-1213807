package com.xiliulou.electricity.task.merchant;

import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 商户 保护期和状态任务
 */
@Component
@Slf4j
@JobHandler(value = "merchantProtectionStatusTask")
public class MerchantProtectionStatusTask extends IJobHandler {
    
    @Resource
    MerchantJoinRecordService merchantJoinRecordService;
    
    @Override
    public ReturnT<String> execute(String s) {
        try {
            merchantJoinRecordService.handelProtectionStatus();
        } catch (Exception e) {
            log.error("处理失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
