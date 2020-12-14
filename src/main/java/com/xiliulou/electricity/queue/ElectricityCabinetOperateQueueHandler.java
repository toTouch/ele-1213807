package com.xiliulou.electricity.queue;

import com.xiliulou.electricity.entity.OperateResultDto;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: lxc
 * @Date: 2020/12/3 08:31
 * @Description:
 */

@Service
@Slf4j
public class ElectricityCabinetOperateQueueHandler {

    ExecutorService executorService = Executors.newFixedThreadPool(20);
    ExecutorService startService = Executors.newFixedThreadPool(1);
    ExecutorService TerminalStartService = Executors.newFixedThreadPool(1);

    private volatile boolean shutdown = false;
    private final LinkedBlockingQueue<OperateResultDto> queue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<HardwareCommandQuery> TerminalQueue = new LinkedBlockingQueue<>();

    @EventListener({WebServerInitializedEvent.class})
    public void startHandleElectricityCabinetOperate() {
        initElectricityCabinetOperate();
        initElectricityCabinetTerminalOperate();
    }

    private void initElectricityCabinetOperate() {
        log.info("初始化换电柜操作响应处理器");
        startService.execute(() -> {
            while (!shutdown) {
                OperateResultDto operateResultDto = null;
                try {
                    operateResultDto = queue.take();
                    log.info(" QUEUE get a message ={}", operateResultDto);

                    OperateResultDto finalLockerOperDTO = operateResultDto;
                    executorService.execute(() -> {
                        handleOrderAfterOperated(finalLockerOperDTO);
                    });

                } catch (Exception e) {
                    log.error("ELECTRICITY CABINET OPERATE QUEUE ERROR! ", e);
                }

            }
        });
    }

    private void initElectricityCabinetTerminalOperate() {
        log.info("初始化换电柜終端操作响应处理器");
        TerminalStartService.execute(() -> {

            while (!shutdown) {
                HardwareCommandQuery commandQuery = null;
                try {
                    commandQuery = TerminalQueue.take();
                    log.info(" QUEUE get a message ={}", commandQuery);

                    HardwareCommandQuery finalCommandQuery = commandQuery;
                    executorService.execute(() -> {
                        handleOrderAfterTerminalOperated(finalCommandQuery);
                    });

                } catch (Exception e) {
                    log.error("ELECTRICITY CABINET TERMINAL OPERATE QUEUE ERROR! ", e);
                }

            }
        });

    }

    /**
     * 接收云端命令后的操作
     *
     * @param commandQuery
     */
    private void handleOrderAfterTerminalOperated(HardwareCommandQuery commandQuery) {

        if (commandQuery.getCommand().contains("replace_update_old")) {
            //换电命令第一步 换旧电池
            replaceOldBattery(commandQuery);
        } else if (commandQuery.getCommand().contains("replace_update_new")) {
            //换电命令第二步 ,换新电池
            replaceNewBattery(commandQuery);
        }

    }

    private void replaceNewBattery(HardwareCommandQuery commandQuery) {
        OperateResultDto operateResultDto = new OperateResultDto();
        operateResultDto.setSessionId(commandQuery.getSessionId());
        operateResultDto.setResult(true);
        try {
            Thread.sleep(4000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        putQueue(operateResultDto);


    }

    private void replaceOldBattery(HardwareCommandQuery commandQuery) {
        Random random = new Random();

        Integer flowNumber = random.nextInt(4) + 1;
        OperateResultDto operateResultDto = new OperateResultDto();
        operateResultDto.setSessionId(commandQuery.getSessionId());
        operateResultDto.setResult(true);
//        operateResultDto.setOperateFlowNum();
        try {
            Thread.sleep(4000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        putQueue(operateResultDto);

    }


    /**
     * 接收到响应的操作信息
     *
     * @param finalLockerOperDTO
     */
    private void handleOrderAfterOperated(OperateResultDto finalLockerOperDTO) {

    }


    public void shutdown() {
        shutdown = true;
        executorService.shutdown();
    }

    public void putQueue(OperateResultDto operateResultDto) {
        try {
            queue.put(operateResultDto);
        } catch (InterruptedException e) {
            log.error("LOCKER OPERATE QUEUE ERROR!", e);
        }
    }

    public void putTerminalQueue(HardwareCommandQuery hardwareCommandQuery) {
        try {
            TerminalQueue.put(hardwareCommandQuery);
        } catch (InterruptedException e) {
            log.error(" OPERATE TERMINAL QUEUE ERROR!", e);
        }
    }
}
