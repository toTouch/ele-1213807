package com.xiliulou.electricity.task;
import com.xiliulou.electricity.service.UserCouponService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName : userCouponExpired
 * @Description : 定时任务--每小时处理一次过期优惠券
 * @Author : lxc
 * @Date: 2021-04-20
 */
@Component
@JobHandler(value = "userCouponExpiredTask")
@Slf4j
public class UserCouponExpiredTask extends IJobHandler {
    @Autowired
    UserCouponService userCouponService;

    //定时任务--优惠券过期
    @Override
    public ReturnT<String> execute(String s)  {
        try {
            userCouponService.handelUserCouponExpired();
        } catch (Exception e) {
            log.error("处理失败"+e);
        }
        return IJobHandler.SUCCESS;
    }
}
