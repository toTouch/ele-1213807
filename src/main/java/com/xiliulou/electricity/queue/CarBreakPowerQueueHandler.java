package com.xiliulou.electricity.queue;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.query.CarMemberCardExpireBreakPowerQuery;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
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
    
    static final List<Long> TEMP_SAVE_CAR_IDS_LIST = new ArrayList<>();
    
    @Autowired
    private Jt808RetrofitService jt808RetrofitService;
    
    @Autowired
    private ElectricityCarService electricityCarService;
    
    public void putQueue(CarMemberCardExpireBreakPowerQuery queue) {
        if (!QUEUE.offer(queue)) {
            log.error("QUEUE is Over Exceed!");
        }
    }
    
    @PostConstruct
    public void init() {
        startService.execute(this::checkQueueAndBreakPower);
    }
    
    private void checkQueueAndBreakPower() {
        log.info("Break Powe Queue start success.....");
        
        long lastSaveTime = System.currentTimeMillis();
        
        while (!shutdown) {
            CarMemberCardExpireBreakPowerQuery query = null;
            try {
                query = QUEUE.poll(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
    
            if (Objects.isNull(query)) {
                continue;
            }
    
            R<Jt808DeviceInfoVo> result = jt808RetrofitService.controlDevice(
                    new Jt808DeviceControlRequest(IdUtil.randomUUID(), query.getSn(), ElectricityCar.TYPE_LOCK));
            if (result.isSuccess()) {
                TEMP_SAVE_CAR_IDS_LIST.add(query.getCid());
            } else {
                log.error("Jt808 error! controlDevice error! carSN={},result={}", query.getSn(), result);
            }
    
            if (TEMP_SAVE_CAR_IDS_LIST.size() > 30 || (lastSaveTime < (System.currentTimeMillis() - 30 * 1000)
                    && !TEMP_SAVE_CAR_IDS_LIST.isEmpty())) {
                List<Long> tempIds = new ArrayList<>(TEMP_SAVE_CAR_IDS_LIST);
                try {
                    electricityCarService.updateLockTypeByIds(tempIds, ElectricityCar.TYPE_LOCK);
                } catch (Exception e) {
                    log.error("BATCH_INSERT ERROR!", e);
                }
                TEMP_SAVE_CAR_IDS_LIST.removeAll(tempIds);
                lastSaveTime = System.currentTimeMillis();
            }
        }
        
        if (!TEMP_SAVE_CAR_IDS_LIST.isEmpty()) {
            try {
                electricityCarService.updateLockTypeByIds(TEMP_SAVE_CAR_IDS_LIST, ElectricityCar.TYPE_LOCK);
            } catch (Exception e) {
                log.error("BATCH_INSERT ERROR!", e);
            }
        }
    }
    
    @Override
    public void destroy() {
        shutdown = true;
    }
}
