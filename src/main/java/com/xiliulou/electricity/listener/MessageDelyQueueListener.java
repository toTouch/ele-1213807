package com.xiliulou.electricity.listener;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.Message;
import com.xiliulou.electricity.queue.MessageDelyQueueService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 延时队列监听器
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-05-16:39
 */
@Slf4j
@Component
public class MessageDelyQueueListener implements DisposableBean {
    
    protected volatile boolean shutdown = false;
    
    @Autowired
    private MessageDelyQueueService messageDelyQueueService;
    
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    
    protected ExecutorService delyQueueListenerThread = XllThreadPoolExecutors.newFixedThreadPool("DELY-QUEUE-LISTENER-POOL", 1, "dely-queue-listener-pool-thread");
    
    
    @EventListener({WebServerInitializedEvent.class})
    public void pollDelyQueue(){
        
        log.info("DELY QUEUE LISTENER INFO! start poll delay queue message!");
        delyQueueListenerThread.execute(()->{
            while (!shutdown){
                //发送满仓提醒
                electricityCabinetService.sendFullBatteryMessage(messageDelyQueueService.pullMessage(CommonConstant.FULL_BATTERY_DELY_QUEUE));
            }
        });
        
    }
    
    @Override
    public void destroy() throws Exception {
        this.shutdown = true;
        delyQueueListenerThread.shutdown();
    }
}
