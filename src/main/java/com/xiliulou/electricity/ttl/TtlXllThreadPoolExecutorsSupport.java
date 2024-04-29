package com.xiliulou.electricity.ttl;


import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import lombok.extern.slf4j.Slf4j;

/**
 * ttl线程池包装获取
 *
 * @author caobotao.cbt
 * @date 2024/4/23 16:18
 */
@Slf4j
public class TtlXllThreadPoolExecutorsSupport {
    
    /**
     * description: 对传入 XllThreadPoolExecutorService 进行包装
     *
     * @param executorService
     * @author caobotao.cbt
     * @date 2024/4/23 16:18
     */
    public static TtlXllThreadPoolExecutorServiceWrapper get(XllThreadPoolExecutorService executorService) {
        return new TtlXllThreadPoolExecutorServiceWrapper(executorService);
    }
    
}
