package com.xiliulou.electricity.queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author: lxc
 * @Date: 2020/12/3 08:31
 * @Description:
 */
//@Service
@Slf4j
public class ElectricityCabinetOperateQueueHandler {

	ExecutorService executorService = Executors.newFixedThreadPool(20);
	ExecutorService startService = Executors.newFixedThreadPool(1);
	private volatile boolean shutdown = false;

	@EventListener({WebServerInitializedEvent.class})
	public void startHandleElectricityCabinetOperate() {
		initElectricityCabinetOperate();
	}

	private void initElectricityCabinetOperate() {
		log.info("初始化寄存柜操作响应处理器");
		startService.execute(() -> {
			while (!shutdown) {
				try {
					executorService.execute(() -> {
					});

				} catch (Exception e) {
					log.error("ELECTRICITY CABINET OPERATE QUEUE ERROR! ", e);
				}

			}
		});
	}


	public void shutdown() {
		shutdown = true;
		executorService.shutdown();
	}
}
