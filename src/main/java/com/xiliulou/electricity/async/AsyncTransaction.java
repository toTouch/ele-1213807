package com.xiliulou.electricity.async;


import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>
 * Description: This class is CompletableAsyncTrans!
 * </p>
 * <p>Project: transaction-testing</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/21
 **/
@Slf4j
@Component
public class AsyncTransaction {
    
    private final DataSourceTransactionManager dataSourceTransactionManager;
    
    public AsyncTransaction(DataSourceTransactionManager dataSourceTransactionManager) {
        this.dataSourceTransactionManager = dataSourceTransactionManager;
    }
    
    /**
     * <p>
     * 线程事务回滚,线程抛出异常,回滚事务,线程中捕获异常会导致事务失效 Description: runAsyncTransactional
     * </p>
     *
     * @param function function 需要在异步中执行的方法
     * @param executor executor 异步线程池
     * @param params   params 异步方法参数
     * @return java.util.concurrent.CompletableFuture<R>
     * <p>Project: AsyncTrans</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/5/21
     */
    public <T, R> CompletableFuture<R> runAsyncTransactional(Function<T, R> function, Executor executor, T params) {
        return runAsyncTransactional(function, executor, params, null);
    }
    
    /**
     * <p>
     * 线程事务回滚,线程抛出异常,回滚事务,线程中捕获异常会导致事务失效 Description: runAsyncTransactional
     * </p>
     *
     * @param function                 function 需要在异步中执行的方法
     * @param executor                 executor 异步线程池
     * @param params                   params 异步方法参数
     * @param afterTransactionalCommit afterTransactionalCommit 异步方法中事务成功提交后执行，触发回滚不执行
     * @return java.util.concurrent.CompletableFuture<R>
     * <p>Project: AsyncTrans</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/5/21
     */
    public <T, R> CompletableFuture<R> runAsyncTransactional(Function<T, R> function, Executor executor, T params, Consumer<R> afterTransactionalCommit) {
        if (function == null || executor == null) {
            log.error("Unable to execute asynchronous method, executor and method cannot be empty.");
            return CompletableFuture.failedFuture(new NullPointerException("function is null"));
        }
        final ThreadLocal<TransactionStatus> transactionStatusThreadLocal = new InheritableThreadLocal<>();
        final AtomicBoolean isCommit = new AtomicBoolean(false);
        return CompletableFuture.supplyAsync(() -> {
            TransactionStatus begin = begin();
            transactionStatusThreadLocal.set(begin);
            R apply = function.apply(params);
            commit(begin);
            isCommit.set(true);
            return apply;
        }, executor).exceptionally(throwable -> {
            TransactionStatus transactionStatus = transactionStatusThreadLocal.get();
            if (transactionStatus != null) {
                rollback(transactionStatus);
                isCommit.set(false);
            }
            log.error("The transaction in this thread will be rolled back due to an exception in the asynchronous thread: ", throwable);
            return null;
        }).thenApply(r -> {
            if (isCommit.get() && afterTransactionalCommit != null) {
                afterTransactionalCommit.accept(r);
            }
            return r;
        });
    }
    
    
    /**
     * <p>
     * 多线程任务编排统一事务回滚,有一个任务抛出异常，其余线程事务全部回滚 Description: runAsyncTransactional
     * </p>
     *
     * @param runnables runnables 多个多线程任务
     * @param executor  executor 异步线程池
     *                  <p>Project: AsyncTrans</p>
     *                  <p>Copyright: Copyright (c) 2024</p>
     *                  <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/5/21
     */
    public void runAsyncTransactional(LinkedBlockingQueue<Runnable> runnables, Executor executor) {
        if (runnables.isEmpty()) {
            return;
        }
        
        final AtomicBoolean flag = new AtomicBoolean(false);
        final CountDownLatch childLatch = new CountDownLatch(runnables.size());
        final CountDownLatch parentLatch = new CountDownLatch(1);
        List<CompletableFuture<TransactionStatus>> futures = new ArrayList<>();
        
        while (runnables.peek() != null) {
            Runnable poll = runnables.poll();
            
            CompletableFuture<TransactionStatus> future = CompletableFuture.supplyAsync(() -> {
                TransactionStatus begin = begin();
                poll.run();
                return begin;
            }, executor).whenComplete((res, throwable) -> {
                try {
                    if (throwable != null) {
                        flag.set(true);
                    }
                    
                    childLatch.countDown();
                    parentLatch.await();
                    
                    if (flag.get()) {
                        rollback(res);
                        log.error("Thread task execution failed, all rolled back");
                        return;
                    }
                    commit(res);
                } catch (InterruptedException e) {
                    childLatch.countDown();
                    rollback(res);
                    log.error("Exception occurred, transaction rollback:", throwable);
                }
            });
            futures.add(future);
        }
        try {
            childLatch.await();
            parentLatch.countDown();
        } catch (Exception e) {
            try {
                if (!futures.isEmpty()) {
                    for (CompletableFuture<TransactionStatus> future : futures) {
                        TransactionStatus status = future.get();
                        if (!status.isCompleted()) {
                            rollback(status);
                        }
                    }
                }
            } catch (Exception e1) {
                log.error("Regression failed, program interruption during rollback process:", e1);
            }
            
        }
    }
    
    /**
     * 开启事务
     */
    private TransactionStatus begin() {
        return dataSourceTransactionManager.getTransaction(new DefaultTransactionAttribute());
    }
    
    /**
     * 提交事务
     */
    private void commit(TransactionStatus transactionStatus) {
        dataSourceTransactionManager.commit(transactionStatus);
    }
    
    /**
     * 回滚事务
     */
    private void rollback(TransactionStatus transactionStatus) {
        dataSourceTransactionManager.rollback(transactionStatus);
    }
}
