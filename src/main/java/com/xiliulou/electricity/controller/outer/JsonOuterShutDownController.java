package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.listener.MessageDelayQueueListener;
import com.xiliulou.electricity.service.monitor.ThreadPoolMonitorComponent;
import com.xiliulou.electricity.utils.WebUtils;
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
    MnsSubscriber mnsSubscriber;
    @Autowired
    MessageDelayQueueListener messageDelayQueueListener;
    @Autowired
    ThreadPoolMonitorComponent threadPoolMonitorComponent;

    @PostMapping("/outer/server/shutdown")
    public R shutDown(HttpServletRequest request) throws Exception {
        String ip = WebUtils.getIP(request);
        log.info(ip);
        if ("127.0.0.1".equalsIgnoreCase(ip) || "localhost".equalsIgnoreCase(ip)) {
            mnsSubscriber.destroy();
            messageDelayQueueListener.destroy();
//            threadPoolMonitorComponent.shutdown();
            return R.ok();
        }
        return R.fail("SYSTEM.0007", ip);
    }

}
