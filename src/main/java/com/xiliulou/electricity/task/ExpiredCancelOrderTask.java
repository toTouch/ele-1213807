package com.xiliulou.electricity.task;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName : Expired
 * @Description : 每分钟设置未支付订单为已取消
 * @Author : YG
 * @Date: 2020-06-19 17:18
 */
@Component
@JobHandler(value = "expiredCancelOrderTask")
@Slf4j
public class ExpiredCancelOrderTask extends IJobHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;

    ExecutorService executorService = XllExecutors.newFixedThreadPool(4);

    //处理未支付寄存订单 (每分钟执行一次)
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            //取redis
            Set<String> orderIdList = redisService.getZsetStringByRange("orderId", 0, System.currentTimeMillis());
            if (DataUtil.collectionIsUsable(orderIdList)) {
                redisService.removeZsetRangeByScore("key", 0, System.currentTimeMillis());
                for (String orderId : orderIdList) {
                    //同步去处理
                    executorService.execute(() -> {
                        electricityCabinetOrderService.handlerExpiredCancelOrder(orderId);
                    });
                }
            }
        } catch (Exception e) {
            log.error("处理失败"+e);
        }
        return IJobHandler.SUCCESS;
    }
}
