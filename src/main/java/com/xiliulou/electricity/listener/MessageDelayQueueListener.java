package com.xiliulou.electricity.listener;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.queue.MessageDelayQueueService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * 延时队列监听器
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-05-16:39
 */
@Slf4j
@Component
public class MessageDelayQueueListener implements DisposableBean {
    
    protected volatile Boolean shutdown = Boolean.FALSE;
    protected ExecutorService delayQueueListenerThread = XllThreadPoolExecutors.newFixedThreadPool("DELAY-QUEUE-LISTENER-POOL", 1, "delay-queue-listener-pool-thread");
    
    @Autowired
    private MessageDelayQueueService messageDelayQueueService;
    
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    
    
    @EventListener({WebServerInitializedEvent.class})
    public void pollDelyQueue(){
        
        log.info("DELY QUEUE LISTENER INFO! start poll delay queue message!");
        delayQueueListenerThread.execute(()->{
            while (!shutdown){
                try {
                    //电池满仓提醒
                    electricityCabinetService.sendFullBatteryMessage(messageDelayQueueService.pullMessage(CommonConstant.FULL_BATTERY_DELY_QUEUE));
                } catch (Exception e) {
                    log.error("ELE ERROR! send full battery to MQ error!",e);
                }
            }
        });
        
    }
    
    @Override
    public void destroy() throws Exception {
        this.shutdown = true;
        delayQueueListenerThread.shutdown();
    }
}
