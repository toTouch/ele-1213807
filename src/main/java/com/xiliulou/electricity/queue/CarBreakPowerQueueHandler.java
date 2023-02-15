package com.xiliulou.electricity.queue;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CarMemberCardExpireBreakPowerQuery;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.web.query.CarControlRequest;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.BeanEntry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zgw
 * @date 2023/2/15 13:31
 * @mood
 */
@Service
@Slf4j
public class CarBreakPowerQueueHandler implements DisposableBean {
    
    private volatile boolean shutdown = false;
    
    private static final LinkedBlockingQueue<CarMemberCardExpireBreakPowerQuery> QUEUE = new LinkedBlockingQueue<>();
    
    private ExecutorService startService = XllThreadPoolExecutors
            .newFixedThreadPool("carBreakPowerQueueStart", 1, "CAR_BREAK_POWER_QUEUE_START");
    
    @Autowired
    Jt808RetrofitService jt808RetrofitService;
    
    public void putQueue(CarMemberCardExpireBreakPowerQuery queue) {
        if (!QUEUE.offer(queue)) {
            log.error("QUEUE is Over Exceed!");
        }
    }
    
    @PostConstruct
    public void init() {
        startService.execute(this::checkQueueAndBreakPower);
    }
    
    public void checkQueueAndBreakPower() {
        log.info("Break Powe Queue start success.....");
        while (!shutdown) {
            CarMemberCardExpireBreakPowerQuery query = null;
            try {
                query = QUEUE.poll(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
            
            if (Objects.isNull(query) || StrUtil.isEmpty(query.getSn())) {
                continue;
            }
            
            R<Jt808DeviceInfoVo> result = jt808RetrofitService.controlDevice(
                    new Jt808DeviceControlRequest(IdUtil.randomUUID(), query.getSn(), CarControlRequest.TYPE_LOCK));
            if (!result.isSuccess()) {
                log.error("Jt808 error! controlDevice error! carSN={},result={}", query.getSn(), result);
            }
        }
    }
    
    @Override
    public void destroy() {
        shutdown = true;
    }
}
