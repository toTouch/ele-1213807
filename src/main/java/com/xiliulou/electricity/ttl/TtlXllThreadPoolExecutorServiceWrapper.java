package com.xiliulou.electricity.ttl;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * description: ttl线程池包装
 *
 * @author caobotao.cbt
 * @date 2024/4/23 10:15
 */
@Slf4j
public class TtlXllThreadPoolExecutorServiceWrapper implements ExecutorService {
    
    private XllThreadPoolExecutorService executorService;
    
    public TtlXllThreadPoolExecutorServiceWrapper(XllThreadPoolExecutorService executorService) {
        this.executorService = executorService;
    }
    
    @Override
    public void execute(Runnable command) {
        executorService.execute(TtlRunnable.get(command));
    }
    
    @Override
    public void shutdown() {
        executorService.shutdown();
    }
    
    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }
    
    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }
    
    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }
    
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }
    
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(TtlCallable.get(task));
    }
    
    
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(TtlRunnable.get(task), result);
    }
    
    
    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(TtlRunnable.get(task));
    }
    
    
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(TtlCallable.gets(tasks));
    }
    
    
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(TtlCallable.gets(tasks), timeout, unit);
    }
    
    
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(TtlCallable.gets(tasks));
    }
    
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(TtlCallable.gets(tasks), timeout, unit);
    }
}
