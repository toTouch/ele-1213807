package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author maxiaodong
 * @date 2024/4/7 9:34
 * @desc 检测企业云豆消费和充值情况是否相等
 */
@Component
@JobHandler(value = "cloudBeanCheckTask")
@Slf4j
public class CloudBeanCheckTask extends IJobHandler {
    @Autowired
    private CloudBeanUseRecordService cloudBeanUseRecordService;
    
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            cloudBeanUseRecordService.checkCloudBeanTask();
        } catch (Exception e) {
            log.error("cloud bean check task error", e);
        }
        return IJobHandler.SUCCESS;
    }
}
