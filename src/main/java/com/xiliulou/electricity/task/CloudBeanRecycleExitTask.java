package com.xiliulou.electricity.task;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 站长自主续费退出时候所有用户云豆退出
 * @author maxiaodong
 * @date 2024-01-16-14:10
 */
@Component
@JobHandler(value = "cloudBeanRecycleExitTask")
@Slf4j
public class CloudBeanRecycleExitTask extends IJobHandler {
    
    @Autowired
    private CloudBeanUseRecordService cloudBeanUseRecordService;
    
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        
        log.info("xxl-job cloud bean recycle exit task start");
        try {
            cloudBeanUseRecordService.recycleCloudBeanExitTask();
        } catch (Exception e) {
            log.error("xxl-job cloud bean recycle exit task error", e);
        } finally {
            MDC.clear();
        }
        
        log.info("xxl-job cloud bean recycle exit task end");
        
        return IJobHandler.SUCCESS;
    }
}
