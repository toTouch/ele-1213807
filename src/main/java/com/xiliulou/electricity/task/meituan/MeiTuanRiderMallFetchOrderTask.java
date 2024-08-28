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
 * 商户 有效期 状态任务
 */
@Component
@Slf4j
@JobHandler(value = "meiTuanRiderMallFetchOrderTask")
public class MeiTuanRiderMallFetchOrderTask extends IJobHandler {
    
    @Resource
    MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    @Override
    public ReturnT<String> execute(String s) {
        String sessionId = UuidUtils.generateUuid();
        try {
            meiTuanRiderMallOrderService.handelFetchOrders(sessionId);
        } catch (Exception e) {
            log.error("MeiTuanRiderMallFetchOrderTask error! sessionId={}", sessionId, e);
        }
        return IJobHandler.SUCCESS;
    }
}
