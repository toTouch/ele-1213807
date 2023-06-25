package com.xiliulou.electricity.queue;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import com.xiliulou.electricity.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class BatteryTrackRecordBatchSaveQueueService {

    static final LinkedBlockingQueue<BatteryTrackRecord> QUEUE = new LinkedBlockingQueue<BatteryTrackRecord>(50000);

    static final List<BatteryTrackRecord> TEMP_SAVE_BATTERY_TRACK_RECORD_LIST = new ArrayList<BatteryTrackRecord>();

    volatile boolean shutdown = false;

    protected ExecutorService delayQueueListenerThread = XllThreadPoolExecutors.newFixedThreadPool("BATTERY-TRACK-INSERT-POOL", 1, "battery-track-insert-pool-thread");

    @Autowired
    BatteryTrackRecordService batteryTrackRecordService;


    @PostConstruct
    public void init() {
        delayQueueListenerThread.execute(this::checkQueueAndBatchInsert);
        log.info("Battery Track Record Batch Insert Queue start success.....");
    }

    public void checkQueueAndBatchInsert() {
        long lastSaveTime = System.currentTimeMillis();
        while (!shutdown) {
            BatteryTrackRecord query = null;
            try {
                query = QUEUE.poll(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }

            if (Objects.nonNull(query)) {
                TEMP_SAVE_BATTERY_TRACK_RECORD_LIST.add(query);
            }

            if (TEMP_SAVE_BATTERY_TRACK_RECORD_LIST.size() > 100 || (lastSaveTime < (System.currentTimeMillis() - 30 * 1000) && !TEMP_SAVE_BATTERY_TRACK_RECORD_LIST.isEmpty())) {
                try {
                    batteryTrackRecordService.insertBatch(TEMP_SAVE_BATTERY_TRACK_RECORD_LIST);
                } catch (Exception e) {
                    log.error("BATCH_INSERT ERROR!", e);
                }
                TEMP_SAVE_BATTERY_TRACK_RECORD_LIST.clear();
                lastSaveTime = System.currentTimeMillis();
            }
        }

        if (!TEMP_SAVE_BATTERY_TRACK_RECORD_LIST.isEmpty()) {
            try {
                batteryTrackRecordService.insertBatch(TEMP_SAVE_BATTERY_TRACK_RECORD_LIST);
            } catch (Exception e) {
                log.error("BATCH_INSERT ERROR!", e);
            }
        }
    }

    public void putQueue(BatteryTrackRecord batteryTrackRecord) {
        if (!QUEUE.offer(batteryTrackRecord)) {
            log.error("QUEUE is Over Exceed!");
        }
    }

    public void destroy(){
        this.shutdown = true;
        delayQueueListenerThread.shutdown();
    }

}