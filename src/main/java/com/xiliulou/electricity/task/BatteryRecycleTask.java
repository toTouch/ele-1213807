package com.xiliulou.electricity.task;

import cn.hutool.core.lang.UUID;
import com.xiliulou.electricity.service.batteryRecycle.BatteryRecycleBizService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2024/10/30 19:17
 * @desc
 */
@Component
@JobHandler(value = "batteryRecycleTask")
@Slf4j
public class BatteryRecycleTask extends IJobHandler {
    @Resource
    private BatteryRecycleBizService batteryRecycleBizService;
    
    @Override
    public ReturnT<String> execute(String s) {
        try {
            Integer tenantId = null;
            if (StringUtils.isNotEmpty(s)) {
                tenantId = Integer.parseInt(s);
            }
    
            batteryRecycleBizService.doBatteryRecycle(tenantId);
        } catch (Exception e) {
            log.error("data-batch batteryRecycleTask error", e);
        }
        
        return IJobHandler.SUCCESS;
    }
}
