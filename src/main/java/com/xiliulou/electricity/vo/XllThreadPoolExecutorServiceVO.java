package com.xiliulou.electricity.vo;

import lombok.Data;
@Data
public class XllThreadPoolExecutorServiceVO {

    //等待队列数量
    private Long queueSize;

    //终止
    private  Boolean  terminated;

    //关闭
    private  Boolean  shutdown;

    //正在执行任务线程数
    private Integer activeCount;

    //任务执行完成总数
    private Long completedTaskCount;

    //核心线程数
    private Integer corePoolSize;

    //池中存活线程数
    private Integer poolSize;

    //最大线程数
    private Integer maximumPoolSize;

    //池中最大线程数
    private Integer largestPoolSize;

    //任务执行总数
    private Long taskCount;

    private StackTraceElement callerInfo;
}
