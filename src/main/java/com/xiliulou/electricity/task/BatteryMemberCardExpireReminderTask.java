package com.xiliulou.electricity.task;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zgw
 * @date 2022/10/10 14:14
 * @mood
 */
@Component
@JobHandler(value = "batteryMemberCardExpireReminderTask")
@Slf4j
public class BatteryMemberCardExpireReminderTask extends IJobHandler {
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    //电池套餐快过期提醒  每天凌晨一次
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        TtlTraceIdSupport.set();
        try {
            TaskParam taskParam = new TaskParam();
            if (StringUtils.isNotBlank(param)){
                taskParam = JsonUtil.fromJson(param,TaskParam.class);
            }
            
            
            electricityMemberCardOrderService.batteryMemberCardExpireReminder(taskParam);
        } catch (Exception e) {
            log.error("xxl-job电池月卡即将过期提醒处理失败", e);
        } finally {
            TtlTraceIdSupport.clear();
        }
        return IJobHandler.SUCCESS;
    }
    
    
    @Data
    public static class TaskParam {
        
        private List<Integer> tenantIds;
     
        private Integer size =300;
        
    }
}
