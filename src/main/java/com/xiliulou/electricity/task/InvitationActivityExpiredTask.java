package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 套餐返现活动过期定时任务
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-10-10:55
 */
@Component
@Slf4j
@JobHandler(value = "invitationActivityExpiredTask")
public class InvitationActivityExpiredTask extends IJobHandler{

    @Autowired
    InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;

    //定时任务--参与邀请活动过期
    @Override
    public ReturnT<String> execute(String s)  {
        try {
            invitationActivityJoinHistoryService.handelActivityJoinHistoryExpired();
        } catch (Exception e) {
            log.error("处理失败",e);
        }
        return IJobHandler.SUCCESS;
    }
}
