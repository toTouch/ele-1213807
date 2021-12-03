package com.xiliulou.electricity.task;
import com.xiliulou.electricity.service.JoinShareMoneyActivityRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName : joinShareActivityExpired
 * @Description : 定时任务--每小时处理一次过期优惠券
 * @Author : lxc
 * @Date: 2021-04-20
 */
@Component
@JobHandler(value = "joinShareActivityExpiredTask")
@Slf4j
public class JoinShareMoneyActivityExpiredTask extends IJobHandler {
    @Autowired
    JoinShareMoneyActivityRecordService joinShareMoneyActivityRecordService;

    //定时任务--参与邀请活动过期
    @Override
    public ReturnT<String> execute(String s)  {
        try {
            joinShareMoneyActivityRecordService.handelJoinShareMoneyActivityExpired();
        } catch (Exception e) {
            log.error("处理失败",e);
        }
        return IJobHandler.SUCCESS;
    }
}
