package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 云豆回收定时任务
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-27-14:10
 */
@Component
@JobHandler(value = "cloudBeanRecycleTask")
@Slf4j
public class CloudBeanRecycleTask extends IJobHandler {
    
    @Autowired
    private CloudBeanUseRecordService cloudBeanUseRecordService;
    
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            cloudBeanUseRecordService.recycleCloudBeanTask();
        } catch (Exception e) {
            log.error("xxl-job回收云豆处理失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
