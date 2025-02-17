package com.xiliulou.electricity.task.merchant;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationBizService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@JobHandler(value = "merchantWithdrawSendTask")
public class MerchantWithdrawSendTask extends IJobHandler {
    @Resource
    private MerchantWithdrawApplicationBizService merchantWithdrawApplicationBizService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());

        try {
            Integer tenantId = null;
            if (ObjectUtils.isNotEmpty(s)) {
                tenantId = Integer.valueOf(s);
            }

            merchantWithdrawApplicationBizService.handleSendMerchantWithdrawProcess(tenantId);
        } catch (Exception e) {
            log.error("merchant withdraw send task error!", e);
        } finally {
            MDC.clear();
        }

        return IJobHandler.SUCCESS;
    }
}
