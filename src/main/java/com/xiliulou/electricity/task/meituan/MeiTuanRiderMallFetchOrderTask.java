package com.xiliulou.electricity.task.meituan;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author HeYafeng
 * @description 定时从美团骑手商城拉取订单
 * @date 2024/8/29 09:34:25
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
        long startTime = System.currentTimeMillis();
        
        try {
            // 默认最近1天
            int recentDay = StringUtils.isBlank(s) ? 1 : Integer.parseInt(s);
            // 美团只支持时间跨度不超过3天订单数据的拉取
            int limitDay = 3;
            recentDay = Math.min(recentDay, limitDay);
            
            log.info("MeiTuanRiderMallFetchOrderTask start! sessionId={}, startTime={}", sessionId, startTime);
            
            meiTuanRiderMallOrderService.handelFetchOrderTask(sessionId, startTime, recentDay);
        } catch (Exception e) {
            log.error("MeiTuanRiderMallFetchOrderTask error! sessionId={}", sessionId, e);
        }
        return IJobHandler.SUCCESS;
    }
}
