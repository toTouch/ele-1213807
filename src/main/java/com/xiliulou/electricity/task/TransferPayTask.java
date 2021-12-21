package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.PayTransferRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@Component
@Slf4j
@JobHandler(value = "transferPayQuery")
public class TransferPayTask {

    @Autowired
    PayTransferRecordService payTransferRecordService;


    /**
     * 提现查询====>修改提现状态 (5分钟执行一次)
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnT<String> transferPayQuery(String param){

        log.info("XXL-JOB---TransferPayTask>>>>>开始执行提现查询>>>>>>>");

        payTransferRecordService.handlerTransferPayQuery();
        return IJobHandler.SUCCESS;

    }
}
