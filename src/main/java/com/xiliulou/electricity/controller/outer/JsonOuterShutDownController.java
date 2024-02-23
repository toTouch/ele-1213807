package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.listener.MessageDelayQueueListener;
import com.xiliulou.electricity.queue.BatteryTrackRecordBatchSaveQueueService;
import com.xiliulou.electricity.service.monitor.ThreadPoolMonitorComponent;
import com.xiliulou.electricity.utils.WebUtils;
import com.xiliulou.hwiiot.service.HwIotService;
import com.xiliulou.hwiiot.subscribe.KafkaConsumerService;
import com.xiliulou.iot.mns.MnsSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : eclair
 * @date : 2021/9/7 1:46 下午
 */
@RestController
@Slf4j
public class JsonOuterShutDownController {
    @Autowired
    MessageDelayQueueListener messageDelayQueueListener;
    @Autowired
    ThreadPoolMonitorComponent threadPoolMonitorComponent;
    @Autowired
    BatteryTrackRecordBatchSaveQueueService batteryTrackRecordBatchSaveQueueService;

    @Autowired
    KafkaConsumerService kafkaConsumerService;
    @PostMapping("/outer/server/shutdown")
    public R shutDown(HttpServletRequest request) throws Exception {
        String ip = WebUtils.getIP(request);
        log.info(ip);
        if ("127.0.0.1".equalsIgnoreCase(ip) || "localhost".equalsIgnoreCase(ip)) {
            kafkaConsumerService.stop();
            messageDelayQueueListener.destroy();
//            threadPoolMonitorComponent.shutdown();
            batteryTrackRecordBatchSaveQueueService.destroy();
            return R.ok();
        }
        return R.fail("SYSTEM.0007", ip);
    }

}
