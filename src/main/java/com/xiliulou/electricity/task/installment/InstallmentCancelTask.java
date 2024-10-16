package com.xiliulou.electricity.task.installment;

import com.xiliulou.electricity.callback.impl.fy.FyInstallmentHandler;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/9 14:11
 */
@Component
@JobHandler(value = "installmentCancelTask")
@RequiredArgsConstructor
@Slf4j
public class InstallmentCancelTask extends IJobHandler {
    
    private final FyInstallmentHandler fyInstallmentHandler;
    
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
            fyInstallmentHandler.cancelSign();
        } catch (Exception e) {
            log.error("XXL-JOB INSTALLMENT CANCEL ERROR!", e);
        }
        return IJobHandler.SUCCESS;
    }
}
