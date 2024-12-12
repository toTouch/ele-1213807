package com.xiliulou.electricity.task.installment;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.electricity.callback.impl.fy.FyInstallmentHandler;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/5 20:07
 */
@Component
@JobHandler(value = "installmentDeductTask")
@AllArgsConstructor
@Slf4j
public class InstallmentDeductTask extends IJobHandler {
    
    private FyInstallmentHandler fyInstallmentHandler;
    
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        try {
            fyInstallmentHandler.dailyInstallmentDeduct();
        } catch (Exception e) {
            log.error("XXL-JOB INSTALLMENT DEDUCT ERROR!", e);
        } finally {
            MDC.clear();
        }
        return IJobHandler.SUCCESS;
    }
}
