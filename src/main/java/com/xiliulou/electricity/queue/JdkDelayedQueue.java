package com.xiliulou.electricity.queue;
import com.xiliulou.electricity.entity.DelayedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class JdkDelayedQueue {
    DelayQueue<DelayedMessage> queue = new DelayQueue<>();

    ExecutorService executorService = Executors.newFixedThreadPool(4);
    ExecutorService startThreadService = Executors.newSingleThreadExecutor();
    private volatile boolean shutdown = false;

    @PostConstruct
    public void init() {
        log.info("初始化redis的延迟队列");
        startThreadService.execute(() -> {
            try {
                while (!shutdown) {
                    DelayedMessage message = queue.take();
                    executorService.execute(() -> {
                        log.info("ORDER CANCEL , queue receiver a meg={}", message);
                       //TODO 执行定时任务
                    });
                }
            } catch (InterruptedException e2) {
                log.info("ORDER CANCEL ! JDK DELAYED FINISH ==========");
            } catch (Exception e) {
                log.error("ORDER CANCEL ! JDK DELAYED ERROR!", e);
            }
        });
    }


    public boolean addQueue(DelayedMessage message) {
        return queue.offer(message);
    }

    public boolean shutdown() {
        shutdown = true;
        executorService.shutdown();
        return true;
    }

    public Integer workingTaskNum() {
        return queue.size();
    }
}
