package com.xiliulou.electricity.task.meituan;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author HeYafeng
 * @description 定时与美团同步订单状态
 * @date 2024/8/29 09:34:25
 */
@Component
@Slf4j
@JobHandler(value = "meiTuanRiderMallSyncOrderStatusTask")
public class MeiTuanRiderMallSyncOrderStatusTask extends IJobHandler {
    
    @Resource
    MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    @Override
    public ReturnT<String> execute(String s) {
        String sessionId = UuidUtils.generateUuid();
        long startTime = System.currentTimeMillis();
        log.info("MeiTuanRiderMallSyncOrderStatusTask start! sessionId={}, startTime={}", sessionId, startTime);
        try {
            meiTuanRiderMallOrderService.handelSyncOrderStatusTask(sessionId, startTime);
        } catch (Exception e) {
            log.error("MeiTuanRiderMallSyncOrderStatusTask error! sessionId={}", sessionId, e);
        }
        return IJobHandler.SUCCESS;
    }
}
